package pl.j.reinmar.modules.weather.infrastructure

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import pl.j.reinmar.modules.weather.infrastructure.api.OpenWeatherApi
import pl.j.reinmar.modules.weather.infrastructure.api.dto.CoordDto
import pl.j.reinmar.modules.weather.infrastructure.api.dto.CurrentWeatherDto
import pl.j.reinmar.modules.weather.infrastructure.api.dto.ForecastDto
import pl.j.reinmar.modules.weather.infrastructure.api.dto.MainDto
import pl.j.reinmar.modules.weather.infrastructure.api.dto.SysDto
import pl.j.reinmar.modules.weather.infrastructure.api.dto.WeatherDescriptionDto
import pl.j.reinmar.modules.weather.infrastructure.api.dto.WindDto
import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.model.WeatherCondition
import pl.j.reinmar.modules.weather.model.WeatherException
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

/**
 * Zestaw testów jednostkowych dla implementacji repozytorium [WeatherRepositoryImpl].
 *
 * Pokrywa ścieżki sukcesu (TC-01), translację błędów sieciowych (TC-03) oraz błędów API (TC-04).
 */
class WeatherRepositoryImplTest {

    private val testDispatcher = StandardTestDispatcher()

    /**
     * Weryfikuje, czy brak lub pusty klucz API skutkuje błędem InvalidApiKey.
     */
    @Test
    fun `brak prawidlowego klucza API skutkuje wyjatkiem InvalidApiKey`() = runTest(testDispatcher) {
        val fakeApi = FakeOpenWeatherApi()
        val repository = WeatherRepositoryImpl(
            api = fakeApi,
            apiKeyProvider = { "" },
            ioDispatcher = testDispatcher
        )

        val result = repository.getCurrentWeather(Coordinates.WARSAW)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is WeatherException.InvalidApiKey)
    }

    /**
     * Weryfikuje poprawne mapowanie danych pogodowych z DTO na encję domenową przy sukcesie API.
     */
    @Test
    fun `poprawna odpowiedz API jest prawidlowo mapowana na obiekt domenowy WeatherData`() = runTest(testDispatcher) {
        val fakeApi = FakeOpenWeatherApi().apply {
            mockCurrentWeather = CurrentWeatherDto(
                name = "Warszawa",
                sys = SysDto(country = "PL"),
                coord = CoordDto(lat = 52.2297, lon = 21.0122),
                main = MainDto(temp = 22.5, feelsLike = 21.0, tempMin = 18.0, tempMax = 24.0, pressure = 1015, humidity = 60),
                weather = listOf(WeatherDescriptionDto(id = 800, main = "Clear", description = "bezchmurnie", icon = "01d")),
                wind = WindDto(speed = 4.5, deg = 90),
                dt = 1700000000L
            )
        }

        val repository = WeatherRepositoryImpl(
            api = fakeApi,
            apiKeyProvider = { "valid_test_api_key_12345" },
            ioDispatcher = testDispatcher
        )

        val result = repository.getCurrentWeather(Coordinates.WARSAW)
        assertTrue(result.isSuccess)

        val data = result.getOrThrow()
        assertEquals("Warszawa", data.locationName)
        assertEquals("PL", data.countryCode)
        assertEquals(22.5, data.temperatureCelsius, 0.001)
        assertEquals(WeatherCondition.CLEAR_SKY, data.condition)
        assertEquals("Bezchmurnie", data.description)
        assertEquals(1015, data.pressureHpa)
        assertEquals(60, data.humidityPercent)
    }

    /**
     * Weryfikuje translację błędu sieciowego IOException na domenowy NetworkUnavailable.
     */
    @Test
    fun `blad sieci IOException jest tlumaczony na WeatherException NetworkUnavailable`() = runTest(testDispatcher) {
        val fakeApi = FakeOpenWeatherApi().apply {
            throwException = IOException("No internet connection")
        }

        val repository = WeatherRepositoryImpl(
            api = fakeApi,
            apiKeyProvider = { "valid_key" },
            ioDispatcher = testDispatcher
        )

        val result = repository.getCurrentWeather(Coordinates.WARSAW)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is WeatherException.NetworkUnavailable)
    }

    /**
     * Weryfikuje translację błędu HTTP 401 na domenowy InvalidApiKey.
     */
    @Test
    fun `blad serwera HTTP 401 jest tlumaczony na InvalidApiKey`() = runTest(testDispatcher) {
        val response = Response.error<CurrentWeatherDto>(
            401,
            "{\"message\": \"Invalid API key\"}".toResponseBody("application/json".toMediaTypeOrNull())
        )
        val fakeApi = FakeOpenWeatherApi().apply {
            throwException = HttpException(response)
        }

        val repository = WeatherRepositoryImpl(
            api = fakeApi,
            apiKeyProvider = { "valid_format_key" },
            ioDispatcher = testDispatcher
        )

        val result = repository.getCurrentWeather(Coordinates.WARSAW)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is WeatherException.InvalidApiKey)
    }

    /**
     * Weryfikuje translację błędu HTTP 404 na domenowy CityNotFound.
     */
    @Test
    fun `blad serwera HTTP 404 jest tlumaczony na CityNotFound`() = runTest(testDispatcher) {
        val response = Response.error<CurrentWeatherDto>(
            404,
            "{\"message\": \"city not found\"}".toResponseBody("application/json".toMediaTypeOrNull())
        )
        val fakeApi = FakeOpenWeatherApi().apply {
            throwException = HttpException(response)
        }

        val repository = WeatherRepositoryImpl(
            api = fakeApi,
            apiKeyProvider = { "valid_format_key" },
            ioDispatcher = testDispatcher
        )

        val result = repository.searchWeatherByCity("NieznanaLokalizacja123")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is WeatherException.CityNotFound)
    }

    /**
     * Atrapa (Fake) interfejsu [OpenWeatherApi] na potrzeby testów jednostkowych.
     */
    private class FakeOpenWeatherApi : OpenWeatherApi {
        var mockCurrentWeather: CurrentWeatherDto = CurrentWeatherDto()
        var mockForecast: ForecastDto = ForecastDto()
        var throwException: Throwable? = null

        override suspend fun getCurrentWeather(
            latitude: Double,
            longitude: Double,
            apiKey: String,
            units: String,
            lang: String
        ): CurrentWeatherDto {
            throwException?.let { throw it }
            return mockCurrentWeather
        }

        override suspend fun getCurrentWeatherByCity(
            query: String,
            apiKey: String,
            units: String,
            lang: String
        ): CurrentWeatherDto {
            throwException?.let { throw it }
            return mockCurrentWeather
        }

        override suspend fun getForecast(
            latitude: Double,
            longitude: Double,
            apiKey: String,
            units: String,
            lang: String
        ): ForecastDto {
            throwException?.let { throw it }
            return mockForecast
        }
    }
}
