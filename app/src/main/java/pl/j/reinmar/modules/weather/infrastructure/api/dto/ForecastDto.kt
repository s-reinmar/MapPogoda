package pl.j.reinmar.modules.weather.infrastructure.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Obiekt transferu danych (DTO) dla odpowiedzi prognozy 5-dniowej z OpenWeather API.
 *
 * @property cod Kod statusu odpowiedzi serwera.
 * @property message Wiadomość lub licznik zwrócony przez serwer.
 * @property cnt Liczba zwróconych rekordów w liście.
 * @property list Lista slotów prognozy (domyślnie co 3 godziny).
 * @property city Dane o mieście, dla którego wyliczono prognozę.
 */
@JsonClass(generateAdapter = true)
data class ForecastDto(
    @Json(name = "cod") val cod: String? = null,
    @Json(name = "message") val message: Any? = null,
    @Json(name = "cnt") val cnt: Int? = null,
    @Json(name = "list") val list: List<ForecastItemDto>? = null,
    @Json(name = "city") val city: CityDto? = null
)

/**
 * DTO dla pojedynczego 3-godzinnego punktu prognozy pogody.
 *
 * @property dt Czas punktu prognozy (Unix timestamp).
 * @property main Wskaźniki meteorologiczne (temperatura, ciśnienie, wilgotność).
 * @property weather Lista opisów zjawisk atmosferycznych.
 * @property clouds Informacje o zachmurzeniu.
 * @property wind Parametry wiatru.
 * @property pop Prawdopodobieństwo wystąpienia opadów (wartość od 0.0 do 1.0).
 * @property dtTxt Sformatowany czas w formacie UTC (np. "2026-10-10 15:00:00").
 */
@JsonClass(generateAdapter = true)
data class ForecastItemDto(
    @Json(name = "dt") val dt: Long? = null,
    @Json(name = "main") val main: MainDto? = null,
    @Json(name = "weather") val weather: List<WeatherDescriptionDto>? = null,
    @Json(name = "clouds") val clouds: CloudsDto? = null,
    @Json(name = "wind") val wind: WindDto? = null,
    @Json(name = "pop") val pop: Double? = null,
    @Json(name = "dt_txt") val dtTxt: String? = null
)

/**
 * DTO reprezentujące dane miasta w odpowiedzi prognozy.
 *
 * @property id Identyfikator miasta w bazie OpenWeather.
 * @property name Nazwa miasta.
 * @property coord Współrzędne geograficzne.
 * @property country Kod kraju.
 * @property population Populacja miasta.
 * @property timezone Przesunięcie strefy czasowej w sekundach.
 */
@JsonClass(generateAdapter = true)
data class CityDto(
    @Json(name = "id") val id: Long? = null,
    @Json(name = "name") val name: String? = null,
    @Json(name = "coord") val coord: CoordDto? = null,
    @Json(name = "country") val country: String? = null,
    @Json(name = "population") val population: Long? = null,
    @Json(name = "timezone") val timezone: Long? = null
)
