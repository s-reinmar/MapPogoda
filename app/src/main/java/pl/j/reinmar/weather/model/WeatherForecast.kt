package pl.j.reinmar.modules.weather.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Agregat domenowy reprezentujący pełną prognozę pogody dla danej lokalizacji.
 *
 * Klasa przechowuje listę punktów prognozy oraz dostarcza metody pomocnicze
 * do grupowania i filtrowania danych według dni oraz godzin.
 *
 * @property locationName Nazwa miejscowości lub rejonu.
 * @property countryCode Kod kraju (np. "PL").
 * @property coordinates Współrzędne geograficzne.
 * @property items Lista punktów prognozy uporządkowana chronologicznie.
 */
data class WeatherForecast(
    val locationName: String,
    val countryCode: String,
    val coordinates: Coordinates,
    val items: List<ForecastItem>
) {
    /**
     * Zwraca prognozę godzinową ograniczoną do podanej liczby slotów (np. najbliższe 8 slotów / 24h).
     *
     * @param count Maksymalna liczba slotów do pobrania.
     * @return Lista najbliższych wpisów prognozy.
     */
    fun getHourly(count: Int = 8): List<ForecastItem> {
        return items.take(count)
    }

    /**
     * Grupuje wpisy prognozy według dni i wyznacza dobowe podsumowanie meteorologiczne.
     *
     * @return Lista podsumowań dobowych [DailySummary].
     */
    fun getDailySummaries(): List<DailySummary> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val groupedByDay = items.groupBy { dateFormat.format(Date(it.timestampSeconds * 1000L)) }

        return groupedByDay.map { (_, dayItems) ->
            val first = dayItems.first()
            val minTemp = dayItems.minOfOrNull { it.tempMinCelsius } ?: first.tempMinCelsius
            val maxTemp = dayItems.maxOfOrNull { it.tempMaxCelsius } ?: first.tempMaxCelsius
            val maxPrecip = dayItems.maxOfOrNull { it.precipitationProbability } ?: 0.0

            // Wybór reprezentatywnego warunku pogodowego (np. z okolic południa)
            val representative = dayItems.getOrNull(dayItems.size / 2) ?: first

            DailySummary(
                dayTimestampSeconds = first.timestampSeconds,
                minTemperatureCelsius = minTemp,
                maxTemperatureCelsius = maxTemp,
                condition = representative.condition,
                description = representative.description,
                precipitationProbability = maxPrecip,
                iconCode = representative.iconCode
            )
        }
    }
}

/**
 * Model podsumowania dobowego prognozy pogody.
 *
 * @property dayTimestampSeconds Czas reprezentujący dany dzień (Unix epoch).
 * @property minTemperatureCelsius Minimalna temperatura w ciągu doby.
 * @property maxTemperatureCelsius Maksymalna temperatura w ciągu doby.
 * @property condition Główny warunek pogodowy dnia.
 * @property description Opis słowny w języku polskim.
 * @property precipitationProbability Maksymalne prawdopodobieństwo opadów.
 * @property iconCode Kod ikony pogodowej.
 */
data class DailySummary(
    val dayTimestampSeconds: Long,
    val minTemperatureCelsius: Double,
    val maxTemperatureCelsius: Double,
    val condition: WeatherCondition,
    val description: String,
    val precipitationProbability: Double,
    val iconCode: String
) {
    /**
     * Nazwa dnia tygodnia (np. "Poniedziałek", "Wtorek").
     *
     * @return Pełna nazwa dnia.
     */
    fun dayOfWeekName(): String {
        val date = Date(dayTimestampSeconds * 1000L)
        val format = SimpleDateFormat("EEEE", Locale("pl", "PL"))
        return format.format(date).replaceFirstChar { it.uppercase() }
    }

    /**
     * Krótka data kalendarzowa (np. "12 paź").
     *
     * @return Skrót daty.
     */
    fun shortDate(): String {
        val date = Date(dayTimestampSeconds * 1000L)
        val format = SimpleDateFormat("d MMM", Locale("pl", "PL"))
        return format.format(date)
    }

    /**
     * Sformatowany zakres temperatur (np. "12° / 22°").
     *
     * @return Czytelny zakres temperatur.
     */
    fun formattedRange(): String = "${Math.round(minTemperatureCelsius)}° / ${Math.round(maxTemperatureCelsius)}°"
}
