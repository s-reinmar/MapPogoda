package pl.j.reinmar.modules.weather.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Zestaw testów jednostkowych dla agregatu [WeatherForecast].
 *
 * Weryfikuje grupowanie dobowe, wyznaczanie maksimów i minimów oraz podział godzinowy.
 */
class WeatherForecastTest {

    /**
     * Weryfikuje, czy getHourly zwraca poprawnie ograniczoną liczbę rekordów.
     */
    @Test
    fun `getHourly ogranicza liczbe zwracanych rekordow do limitu`() {
        val items = (1..15).map { index ->
            createForecastItem(timestamp = 1700000000L + index * 3600L, temp = 15.0 + index)
        }
        val forecast = WeatherForecast(
            locationName = "Kraków",
            countryCode = "PL",
            coordinates = Coordinates(50.0647, 19.9450),
            items = items
        )

        val hourly = forecast.getHourly(count = 6)
        assertEquals(6, hourly.size)
        assertEquals(16.0, hourly.first().temperatureCelsius, 0.001)
    }

    /**
     * Weryfikuje, czy grupowanie dobowe poprawnie wyznacza dobowe min/max temperatury.
     */
    @Test
    fun `getDailySummaries poprawnie wylicza minimalna i maksymalna temperature dla dnia`() {
        // Wszystkie sloty w tym samym dniu w okolicach poludnia (1700040000L)
        val dayTimestamp = 1700040000L
        val items = listOf(
            createForecastItem(timestamp = dayTimestamp, temp = 12.0, minTemp = 10.0, maxTemp = 14.0),
            createForecastItem(timestamp = dayTimestamp + 3600L, temp = 18.0, minTemp = 15.0, maxTemp = 22.0),
            createForecastItem(timestamp = dayTimestamp + 7200L, temp = 14.0, minTemp = 11.0, maxTemp = 16.0)
        )

        val forecast = WeatherForecast(
            locationName = "Gdańsk",
            countryCode = "PL",
            coordinates = Coordinates(54.3520, 18.6466),
            items = items
        )

        val daily = forecast.getDailySummaries()
        assertEquals(1, daily.size)

        val summary = daily.first()
        assertEquals(10.0, summary.minTemperatureCelsius, 0.001)
        assertEquals(22.0, summary.maxTemperatureCelsius, 0.001)
        assertEquals("10° / 22°", summary.formattedRange())
    }

    /**
     * Pomocnicza metoda tworząca obiekt [ForecastItem] do testów.
     */
    private fun createForecastItem(
        timestamp: Long,
        temp: Double,
        minTemp: Double = temp - 2.0,
        maxTemp: Double = temp + 2.0
    ): ForecastItem {
        return ForecastItem(
            timestampSeconds = timestamp,
            temperatureCelsius = temp,
            tempMinCelsius = minTemp,
            tempMaxCelsius = maxTemp,
            condition = WeatherCondition.FEW_CLOUDS,
            description = "Niewielkie zachmurzenie",
            precipitationProbability = 0.2,
            windSpeedMps = 3.5,
            iconCode = "02d"
        )
    }
}
