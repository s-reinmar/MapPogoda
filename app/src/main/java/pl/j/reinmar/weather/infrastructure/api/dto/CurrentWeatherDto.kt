package pl.j.reinmar.modules.weather.infrastructure.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Obiekt transferu danych (DTO) dla odpowiedzi aktualnej pogody z OpenWeather API.
 *
 * @property coord Współrzędne geograficzne punktu pomiarowego.
 * @property weather Lista opisów zjawisk atmosferycznych.
 * @property main Główne wskaźniki meteorologiczne (temperatura, ciśnienie, wilgotność).
 * @property wind Parametry wiatru.
 * @property clouds Informacje o zachmurzeniu.
 * @property dt Czas pomiaru w formacie Unix timestamp.
 * @property sys Dodatkowe informacje systemowe (kod kraju, wschód/zachód słońca).
 * @property name Nazwa miejscowości zwrócona przez API.
 */
@JsonClass(generateAdapter = true)
data class CurrentWeatherDto(
    @Json(name = "coord") val coord: CoordDto? = null,
    @Json(name = "weather") val weather: List<WeatherDescriptionDto>? = null,
    @Json(name = "main") val main: MainDto? = null,
    @Json(name = "wind") val wind: WindDto? = null,
    @Json(name = "clouds") val clouds: CloudsDto? = null,
    @Json(name = "dt") val dt: Long? = null,
    @Json(name = "sys") val sys: SysDto? = null,
    @Json(name = "name") val name: String? = null
)

/**
 * DTO dla współrzędnych geograficznych w formacie OpenWeather.
 *
 * @property lon Długość geograficzna.
 * @property lat Szerokość geograficzna.
 */
@JsonClass(generateAdapter = true)
data class CoordDto(
    @Json(name = "lon") val lon: Double? = null,
    @Json(name = "lat") val lat: Double? = null
)

/**
 * DTO z opisem zjawiska pogodowego i kodem ikony.
 *
 * @property id Identyfikator stanu pogody.
 * @property main Główna kategoria zjawiska.
 * @property description Szczegółowy opis zjawiska.
 * @property icon Kod ikony pogodowej (np. "10d").
 */
@JsonClass(generateAdapter = true)
data class WeatherDescriptionDto(
    @Json(name = "id") val id: Int? = null,
    @Json(name = "main") val main: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "icon") val icon: String? = null
)

/**
 * DTO z głównymi parametrami termicznymi i ciśnieniowymi.
 *
 * @property temp Aktualna temperatura w stopniach Celsjusza (przy units=metric).
 * @property feelsLike Temperatura odczuwalna.
 * @property tempMin Temperatura minimalna.
 * @property tempMax Temperatura maksymalna.
 * @property pressure Ciśnienie atmosferyczne w hPa.
 * @property humidity Wilgotność powietrza w procentach.
 */
@JsonClass(generateAdapter = true)
data class MainDto(
    @Json(name = "temp") val temp: Double? = null,
    @Json(name = "feels_like") val feelsLike: Double? = null,
    @Json(name = "temp_min") val tempMin: Double? = null,
    @Json(name = "temp_max") val tempMax: Double? = null,
    @Json(name = "pressure") val pressure: Int? = null,
    @Json(name = "humidity") val humidity: Int? = null
)

/**
 * DTO z informacjami o wietrze.
 *
 * @property speed Prędkość wiatru w m/s.
 * @property deg Kierunek wiatru w stopniach.
 */
@JsonClass(generateAdapter = true)
data class WindDto(
    @Json(name = "speed") val speed: Double? = null,
    @Json(name = "deg") val deg: Int? = null
)

/**
 * DTO ze wskaźnikiem zachmurzenia.
 *
 * @property all Procent zachmurzenia (0-100).
 */
@JsonClass(generateAdapter = true)
data class CloudsDto(
    @Json(name = "all") val all: Int? = null
)

/**
 * DTO z informacjami o kraju i strefie czasowej.
 *
 * @property country Dwuliterowy kod kraju (np. "PL").
 * @property sunrise Czas wschodu słońca (Unix timestamp).
 * @property sunset Czas zachodu słońca (Unix timestamp).
 */
@JsonClass(generateAdapter = true)
data class SysDto(
    @Json(name = "country") val country: String? = null,
    @Json(name = "sunrise") val sunrise: Long? = null,
    @Json(name = "sunset") val sunset: Long? = null
)
