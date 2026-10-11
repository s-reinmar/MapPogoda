package pl.j.reinmar.modules.weather.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Zestaw testów jednostkowych dla modelu domenowego [WeatherData].
 *
 * Weryfikuje reguły formatowania temperatur, przeliczanie wiatru oraz wskaźniki mrozu.
 */
class WeatherDataTest {

    /**
     * Weryfikuje zaokrąglanie i formatowanie temperatury.
     */
    @Test
    fun `formattedTemperature poprawnie zaokragla temperature`() {
        val weather = createSampleWeather(temperature = 21.6)
        assertEquals("22°C", weather.formattedTemperature())

        val coldWeather = createSampleWeather(temperature = -3.2)
        assertEquals("-3°C", coldWeather.formattedTemperature())
    }

    /**
     * Weryfikuje przeliczanie prędkości wiatru z metrów na sekundę na km/h.
     */
    @Test
    fun `windSpeedKmh poprawnie przelicza metry na sekunde na kilometry na godzine`() {
        val weather = createSampleWeather(windSpeedMps = 10.0)
        assertEquals(36.0, weather.windSpeedKmh(), 0.001)
        assertEquals("36.0 km/h", weather.formattedWindSpeed())
    }

    /**
     * Weryfikuje prawidłowe wyznaczanie kierunku kardynalnego wiatru ze stopni kątowych.
     */
    @Test
    fun `windDirectionCardinal poprawnie mapuje stopnie na kierunki swiata`() {
        assertEquals("N", createSampleWeather(windDirection = 0).windDirectionCardinal())
        assertEquals("NE", createSampleWeather(windDirection = 45).windDirectionCardinal())
        assertEquals("E", createSampleWeather(windDirection = 90).windDirectionCardinal())
        assertEquals("S", createSampleWeather(windDirection = 180).windDirectionCardinal())
        assertEquals("W", createSampleWeather(windDirection = 270).windDirectionCardinal())
        assertEquals("NW", createSampleWeather(windDirection = 315).windDirectionCardinal())
        assertEquals("N", createSampleWeather(windDirection = 360).windDirectionCardinal())
    }

    /**
     * Weryfikuje wykrywanie ujemnej temperatury (mrozu).
     */
    @Test
    fun `isFreezing zwraca true dla temperatur ponizej zera`() {
        assertTrue(createSampleWeather(temperature = -0.5).isFreezing())
        assertFalse(createSampleWeather(temperature = 0.0).isFreezing())
        assertFalse(createSampleWeather(temperature = 15.0).isFreezing())
    }

    /**
     * Pomocnicza metoda tworząca przykładowy obiekt [WeatherData] do testów.
     */
    private fun createSampleWeather(
        temperature: Double = 20.0,
        windSpeedMps: Double = 5.0,
        windDirection: Int = 180
    ): WeatherData {
        return WeatherData(
            locationName = "Warszawa",
            countryCode = "PL",
            coordinates = Coordinates(52.2297, 21.0122),
            temperatureCelsius = temperature,
            feelsLikeCelsius = temperature - 1.0,
            tempMinCelsius = temperature - 2.0,
            tempMaxCelsius = temperature + 2.0,
            condition = WeatherCondition.CLEAR_SKY,
            description = "Bezchmurnie",
            humidityPercent = 50,
            pressureHpa = 1013,
            windSpeedMps = windSpeedMps,
            windDirectionDegrees = windDirection,
            cloudinessPercent = 10,
            timestampSeconds = 1700000000L,
            iconCode = "01d"
        )
    }
}
