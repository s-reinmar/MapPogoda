package pl.j.reinmar.modules.weather.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Model domenowy reprezentujący pojedynczy przedział prognozy pogody (np. slot 3-godzinny).
 *
 * @property timestampSeconds Czas odczytu w sekundach (Unix epoch).
 * @property temperatureCelsius Temperatura prognozowana w stopniach Celsjusza.
 * @property tempMinCelsius Minimalna prognozowana temperatura.
 * @property tempMaxCelsius Maksymalna prognozowana temperatura.
 * @property condition Domenowy stan warunków pogodowych.
 * @property description Opis zjawiska atmosferycznego w języku polskim.
 * @property precipitationProbability Prawdopodobieństwo wystąpienia opadów (wartość od 0.0 do 1.0).
 * @property windSpeedMps Prędkość wiatru w metrach na sekundę.
 * @property iconCode Kod ikony pogodowej.
 */
data class ForecastItem(
    val timestampSeconds: Long,
    val temperatureCelsius: Double,
    val tempMinCelsius: Double,
    val tempMaxCelsius: Double,
    val condition: WeatherCondition,
    val description: String,
    val precipitationProbability: Double,
    val windSpeedMps: Double,
    val iconCode: String
) {
    /**
     * Sformatowana temperatura zaokrąglona do liczby całkowitej (np. "18°C").
     *
     * @return Czytelny ciąg temperatury.
     */
    fun formattedTemperature(): String = "${Math.round(temperatureCelsius)}°C"

    /**
     * Zwraca godzinę prognozy w formacie 24-godzinnym (np. "14:00").
     *
     * @return Godzina prognozy.
     */
    fun formattedHour(): String {
        val date = Date(timestampSeconds * 1000L)
        val format = SimpleDateFormat("HH:mm", Locale("pl", "PL"))
        return format.format(date)
    }

    /**
     * Zwraca nazwę dnia tygodnia w języku polskim (np. "Pon.", "Wt.").
     *
     * @return Skrót dnia tygodnia.
     */
    fun formattedDayOfWeek(): String {
        val date = Date(timestampSeconds * 1000L)
        val format = SimpleDateFormat("EEE", Locale("pl", "PL"))
        return format.format(date).replaceFirstChar { it.uppercase() }
    }

    /**
     * Zwraca pełną datę i godzinę prognozy (np. "Pn, 12 paź, 15:00").
     *
     * @return Czytelna pełna data.
     */
    fun formattedFullDate(): String {
        val date = Date(timestampSeconds * 1000L)
        val format = SimpleDateFormat("EEE, d MMM, HH:mm", Locale("pl", "PL"))
        return format.format(date)
    }

    /**
     * Sformatowany procent prawdopodobieństwa opadów (np. "45%").
     *
     * @return Wartość procentowa opadów.
     */
    fun formattedPrecipitationPercent(): String {
        val percent = Math.round(precipitationProbability * 100)
        return "$percent%"
    }
}
