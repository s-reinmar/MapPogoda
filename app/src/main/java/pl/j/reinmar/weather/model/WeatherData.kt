package pl.j.reinmar.modules.weather.model

import java.util.Locale

/**
 * Główny model domenowy reprezentujący aktualny stan pogody w danej lokalizacji.
 *
 * Klasa jest niemutowalna i hermetyzuje wszystkie odczyty meteorologiczne
 * wraz z regułami formatowania i prezentacji.
 *
 * @property locationName Nazwa miejscowości lub regionu.
 * @property countryCode Kod kraju (np. "PL").
 * @property coordinates Współrzędne geograficzne punktu pomiarowego.
 * @property temperatureCelsius Temperatura powietrza w stopniach Celsjusza.
 * @property feelsLikeCelsius Temperatura odczuwalna w stopniach Celsjusza.
 * @property tempMinCelsius Minimalna prognozowana temperatura dzienna.
 * @property tempMaxCelsius Maksymalna prognozowana temperatura dzienna.
 * @property condition Główny stan warunków atmosferycznych.
 * @property description Szczegółowy opis zjawiska w języku polskim (np. "umiarkowane opady deszczu").
 * @property humidityPercent Wilgotność względna powietrza w procentach (0 - 100).
 * @property pressureHpa Ciśnienie atmosferyczne w hektopaskalach (hPa).
 * @property windSpeedMps Prędkość wiatru w metrach na sekundę (m/s).
 * @property windDirectionDegrees Kierunek wiatru w stopniach (0 - 360).
 * @property cloudinessPercent Stopień zachmurzenia nieba w procentach (0 - 100).
 * @property timestampSeconds Czas pobrania odczytu w sekundach (Unix epoch).
 * @property iconCode Kod ikony pogodowej z API (np. "10d").
 */
data class WeatherData(
    val locationName: String,
    val countryCode: String,
    val coordinates: Coordinates,
    val temperatureCelsius: Double,
    val feelsLikeCelsius: Double,
    val tempMinCelsius: Double,
    val tempMaxCelsius: Double,
    val condition: WeatherCondition,
    val description: String,
    val humidityPercent: Int,
    val pressureHpa: Int,
    val windSpeedMps: Double,
    val windDirectionDegrees: Int,
    val cloudinessPercent: Int,
    val timestampSeconds: Long,
    val iconCode: String
) {
    /**
     * Zwraca zaokrągloną temperaturę w stopniach Celsjusza w formacie np. "21°C".
     *
     * @return Sformatowana temperatura.
     */
    fun formattedTemperature(): String = "${Math.round(temperatureCelsius)}°C"

    /**
     * Zwraca zaokrągloną temperaturę odczuwalną w formacie np. "Odczuwalna 20°C".
     *
     * @return Sformatowany tekst z temperaturą odczuwalną.
     */
    fun formattedFeelsLike(): String = "Odczuwalna ${Math.round(feelsLikeCelsius)}°C"

    /**
     * Zwraca prędkość wiatru przeliczoną na km/h.
     *
     * @return Prędkość wiatru w km/h.
     */
    fun windSpeedKmh(): Double = windSpeedMps * 3.6

    /**
     * Zwraca sformatowaną prędkość wiatru w kilometrach na godzinę (np. "18.5 km/h").
     *
     * @return Czytelna prędkość wiatru.
     */
    fun formattedWindSpeed(): String = String.format(Locale.US, "%.1f km/h", windSpeedKmh())

    /**
     * Zwraca kierunek kardynalny wiatru w języku polskim (N, NE, E, SE, S, SW, W, NW).
     *
     * @return Skrót kierunku geograficznego wiatru.
     */
    fun windDirectionCardinal(): String {
        val directions = arrayOf("N", "NE", "E", "SE", "S", "SW", "W", "NW")
        val index = Math.round(((windDirectionDegrees % 360) / 45.0)).toInt() % 8
        return directions[index]
    }

    /**
     * Sprawdza, czy temperatura wskazuje na przymrozek (poniżej zera).
     *
     * @return `true` jeśli temperatura jest mniejsza niż 0.0°C.
     */
    fun isFreezing(): Boolean = temperatureCelsius < 0.0

    /**
     * Pełna nazwa lokalizacji wraz z kodem kraju.
     *
     * @return Ciąg np. "Warszawa, PL".
     */
    fun displayLocation(): String = if (countryCode.isNotBlank()) "$locationName, $countryCode" else locationName
}
