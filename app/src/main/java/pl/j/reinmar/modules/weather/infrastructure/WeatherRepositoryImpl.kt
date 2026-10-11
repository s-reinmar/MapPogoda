package pl.j.reinmar.modules.weather.infrastructure

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pl.j.reinmar.modules.weather.infrastructure.api.OpenWeatherApi
import pl.j.reinmar.modules.weather.infrastructure.api.dto.CurrentWeatherDto
import pl.j.reinmar.modules.weather.infrastructure.api.dto.ForecastDto
import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.model.ForecastItem
import pl.j.reinmar.modules.weather.model.WeatherCondition
import pl.j.reinmar.modules.weather.model.WeatherData
import pl.j.reinmar.modules.weather.model.WeatherException
import pl.j.reinmar.modules.weather.model.WeatherForecast
import pl.j.reinmar.modules.weather.repository.IWeatherRepository
import retrofit2.HttpException
import java.io.IOException

/**
 * Produkcyjna implementacja repozytorium pogodowego [IWeatherRepository].
 *
 * Klasa odpowiada za komunikację z OpenWeatherMap API przez klienta Retrofit,
 * bezpieczne mapowanie surowych obiektów transferu danych (DTO) na czyste encje
 * domenowe oraz translację wyjątków sieciowych i HTTP na dedykowane wyjątki [WeatherException].
 *
 * @property api Interfejs Retrofit [OpenWeatherApi].
 * @property apiKeyProvider Funkcja dostarczająca aktualny klucz API OpenWeather.
 * @property ioDispatcher Dyspozytor wątków dla operacji We/Wy (domyślnie [Dispatchers.IO]).
 */
class WeatherRepositoryImpl(
    private val api: OpenWeatherApi,
    private val apiKeyProvider: () -> String,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : IWeatherRepository {

    /**
     * Pobiera aktualne dane pogodowe dla wskazanych współrzędnych geograficznych.
     *
     * @param coordinates Poprawne współrzędne geograficzne punktu pomiarowego.
     * @return Wynik [Result] z obiektem [WeatherData] lub błędem [WeatherException].
     */
    override suspend fun getCurrentWeather(coordinates: Coordinates): Result<WeatherData> =
        withContext(ioDispatcher) {
            val key = getValidatedApiKey()
            if (key.isFailure) {
                return@withContext Result.failure(key.exceptionOrNull() ?: WeatherException.InvalidApiKey())
            }

            try {
                val dto = api.getCurrentWeather(
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude,
                    apiKey = key.getOrThrow()
                )
                Result.success(mapCurrentWeatherDtoToDomain(dto, fallbackCoordinates = coordinates))
            } catch (t: Throwable) {
                Result.failure(mapThrowableToDomain(t))
            }
        }

    /**
     * Pobiera 5-dniową prognozę pogody dla wskazanych współrzędnych geograficznych.
     *
     * @param coordinates Poprawne współrzędne geograficzne punktu pomiarowego.
     * @return Wynik [Result] z agregatem [WeatherForecast] lub błędem [WeatherException].
     */
    override suspend fun getForecast(coordinates: Coordinates): Result<WeatherForecast> =
        withContext(ioDispatcher) {
            val key = getValidatedApiKey()
            if (key.isFailure) {
                return@withContext Result.failure(key.exceptionOrNull() ?: WeatherException.InvalidApiKey())
            }

            try {
                val dto = api.getForecast(
                    latitude = coordinates.latitude,
                    longitude = coordinates.longitude,
                    apiKey = key.getOrThrow()
                )
                Result.success(mapForecastDtoToDomain(dto, fallbackCoordinates = coordinates))
            } catch (t: Throwable) {
                Result.failure(mapThrowableToDomain(t))
            }
        }

    /**
     * Wyszukuje dane pogodowe dla podanej nazwy miasta.
     *
     * @param cityName Nazwa miejscowości (np. "Warszawa").
     * @return Wynik [Result] z danymi [WeatherData] lub błędem [WeatherException].
     */
    override suspend fun searchWeatherByCity(cityName: String): Result<WeatherData> =
        withContext(ioDispatcher) {
            val key = getValidatedApiKey()
            if (key.isFailure) {
                return@withContext Result.failure(key.exceptionOrNull() ?: WeatherException.InvalidApiKey())
            }

            val query = cityName.trim()
            if (query.isBlank()) {
                return@withContext Result.failure(WeatherException.CityNotFound(""))
            }

            try {
                val dto = api.getCurrentWeatherByCity(
                    query = query,
                    apiKey = key.getOrThrow()
                )
                val lat = dto.coord?.lat ?: Coordinates.WARSAW.latitude
                val lon = dto.coord?.lon ?: Coordinates.WARSAW.longitude
                Result.success(mapCurrentWeatherDtoToDomain(dto, fallbackCoordinates = Coordinates(lat, lon)))
            } catch (t: Throwable) {
                Result.failure(mapThrowableToDomain(t, queryCity = cityName))
            }
        }

    /**
     * Waliduje klucz API dostarczony przez [apiKeyProvider].
     *
     * @return [Result] z kluczem w przypadku powodzenia lub [WeatherException.InvalidApiKey] jeśli klucz jest pusty lub stanowi placeholder.
     */
    private fun getValidatedApiKey(): Result<String> {
        val rawKey = apiKeyProvider().trim()
        if (rawKey.isBlank() || rawKey == "YOUR_OPENWEATHER_API_KEY" || rawKey == "dummy_openweather_key") {
            return Result.failure(WeatherException.InvalidApiKey())
        }
        return Result.success(rawKey)
    }

    /**
     * Mapuje surowy obiekt [CurrentWeatherDto] na domenową encję [WeatherData].
     *
     * @param dto Odpowiedź z serwera OpenWeather.
     * @param fallbackCoordinates Domyślne współrzędne w przypadku braku współrzędnych w odpowiedzi.
     * @return Kompletna encja [WeatherData].
     */
    private fun mapCurrentWeatherDtoToDomain(
        dto: CurrentWeatherDto,
        fallbackCoordinates: Coordinates
    ): WeatherData {
        val weatherItem = dto.weather?.firstOrNull()
        val condition = WeatherCondition.fromOpenWeatherId(weatherItem?.id ?: 800)
        val description = weatherItem?.description
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            ?: condition.displayName

        val lat = dto.coord?.lat ?: fallbackCoordinates.latitude
        val lon = dto.coord?.lon ?: fallbackCoordinates.longitude
        val coordinates = Coordinates(lat, lon)

        return WeatherData(
            locationName = dto.name ?: "Nieznana lokalizacja",
            countryCode = dto.sys?.country ?: "",
            coordinates = coordinates,
            temperatureCelsius = dto.main?.temp ?: 0.0,
            feelsLikeCelsius = dto.main?.feelsLike ?: dto.main?.temp ?: 0.0,
            tempMinCelsius = dto.main?.tempMin ?: dto.main?.temp ?: 0.0,
            tempMaxCelsius = dto.main?.tempMax ?: dto.main?.temp ?: 0.0,
            condition = condition,
            description = description,
            humidityPercent = dto.main?.humidity ?: 0,
            pressureHpa = dto.main?.pressure ?: 1013,
            windSpeedMps = dto.wind?.speed ?: 0.0,
            windDirectionDegrees = dto.wind?.deg ?: 0,
            cloudinessPercent = dto.clouds?.all ?: 0,
            timestampSeconds = dto.dt ?: (System.currentTimeMillis() / 1000L),
            iconCode = weatherItem?.icon ?: "01d"
        )
    }

    /**
     * Mapuje surowy obiekt [ForecastDto] na domenowy agregat [WeatherForecast].
     *
     * @param dto Odpowiedź prognozy z serwera OpenWeather.
     * @param fallbackCoordinates Domyślne współrzędne punktu prognozy.
     * @return Agregat [WeatherForecast].
     */
    private fun mapForecastDtoToDomain(
        dto: ForecastDto,
        fallbackCoordinates: Coordinates
    ): WeatherForecast {
        val lat = dto.city?.coord?.lat ?: fallbackCoordinates.latitude
        val lon = dto.city?.coord?.lon ?: fallbackCoordinates.longitude
        val coordinates = Coordinates(lat, lon)

        val items = dto.list?.mapNotNull { itemDto ->
            val timestamp = itemDto.dt ?: return@mapNotNull null
            val weatherDesc = itemDto.weather?.firstOrNull()
            val condition = WeatherCondition.fromOpenWeatherId(weatherDesc?.id ?: 800)
            val description = weatherDesc?.description
                ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                ?: condition.displayName

            ForecastItem(
                timestampSeconds = timestamp,
                temperatureCelsius = itemDto.main?.temp ?: 0.0,
                tempMinCelsius = itemDto.main?.tempMin ?: itemDto.main?.temp ?: 0.0,
                tempMaxCelsius = itemDto.main?.tempMax ?: itemDto.main?.temp ?: 0.0,
                condition = condition,
                description = description,
                precipitationProbability = itemDto.pop ?: 0.0,
                windSpeedMps = itemDto.wind?.speed ?: 0.0,
                iconCode = weatherDesc?.icon ?: "01d"
            )
        } ?: emptyList()

        return WeatherForecast(
            locationName = dto.city?.name ?: "Nieznana lokalizacja",
            countryCode = dto.city?.country ?: "",
            coordinates = coordinates,
            items = items
        )
    }

    /**
     * Tłumaczy wyjątki sieciowe i biblioteczne na domenowe wyjątki [WeatherException].
     *
     * @param throwable Źródłowy wyjątek.
     * @param queryCity Opcjonalna nazwa miasta dla błędu 404.
     * @return Odpowiedni domenowy [WeatherException].
     */
    private fun mapThrowableToDomain(throwable: Throwable, queryCity: String? = null): WeatherException {
        return when (throwable) {
            is HttpException -> {
                when (throwable.code()) {
                    401, 403 -> WeatherException.InvalidApiKey()
                    404 -> WeatherException.CityNotFound(queryCity ?: "nieznana")
                    429 -> WeatherException.ApiRateLimitExceeded()
                    in 500..599 -> WeatherException.ServerSideError(throwable.code())
                    else -> WeatherException.GenericError("Błąd serwera HTTP: ${throwable.code()}", throwable)
                }
            }
            is IOException -> WeatherException.NetworkUnavailable(throwable)
            is WeatherException -> throwable
            else -> WeatherException.GenericError(throwable.message ?: "Wystąpił nieoczekiwany błąd", throwable)
        }
    }
}
