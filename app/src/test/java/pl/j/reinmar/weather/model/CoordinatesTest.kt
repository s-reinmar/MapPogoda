package pl.j.reinmar.modules.weather.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Zestaw testów jednostkowych dla obiektu wartości [Coordinates] (TC-02).
 *
 * Weryfikuje walidację reguł domeny, granice WGS 84 oraz bezpieczną fabrykę.
 */
class CoordinatesTest {

    /**
     * Weryfikuje, czy poprawne współrzędne geograficzne są poprawnie tworzone.
     */
    @Test
    fun `tworzenie obiektu Coordinates z poprawnymi wartosciami konczy sie sukcesem`() {
        val coords = Coordinates(latitude = 52.2297, longitude = 21.0122)
        assertEquals(52.2297, coords.latitude, 0.0001)
        assertEquals(21.0122, coords.longitude, 0.0001)
    }

    /**
     * Weryfikuje, czy przekroczenie zakresu szerokości geograficznej rzuca wyjątek IllegalArgumentException.
     */
    @Test(expected = IllegalArgumentException::class)
    fun `szerokosc geograficzna powyzej 90 stopni rzuca wyjatek`() {
        Coordinates(latitude = 90.001, longitude = 20.0)
    }

    /**
     * Weryfikuje, czy szerokość geograficzna poniżej -90 stopni rzuca wyjątek.
     */
    @Test(expected = IllegalArgumentException::class)
    fun `szerokosc geograficzna ponizej minus 90 stopni rzuca wyjatek`() {
        Coordinates(latitude = -90.001, longitude = 20.0)
    }

    /**
     * Weryfikuje, czy przekroczenie zakresu długości geograficznej rzuca wyjątek.
     */
    @Test(expected = IllegalArgumentException::class)
    fun `dlugosc geograficzna powyzej 180 stopni rzuca wyjatek`() {
        Coordinates(latitude = 50.0, longitude = 180.001)
    }

    /**
     * Weryfikuje, czy długość geograficzna poniżej -180 stopni rzuca wyjątek.
     */
    @Test(expected = IllegalArgumentException::class)
    fun `dlugosc geograficzna ponizej minus 180 stopni rzuca wyjatek`() {
        Coordinates(latitude = 50.0, longitude = -180.001)
    }

    /**
     * Weryfikuje, czy bezpieczna fabryka createSafe zwraca Result sukcesu dla poprawnych danych.
     */
    @Test
    fun `fabryka createSafe zwraca sukces dla poprawnych wspolrzednych`() {
        val result = Coordinates.createSafe(50.0647, 19.9450)
        assertTrue(result.isSuccess)
        val coords = result.getOrNull()
        assertEquals(50.0647, coords?.latitude ?: 0.0, 0.0001)
    }

    /**
     * Weryfikuje, czy bezpieczna fabryka createSafe zwraca Result z błędem InvalidCoordinatesException dla błędnych danych.
     */
    @Test
    fun `fabryka createSafe zwraca blad InvalidCoordinatesException dla blednych danych`() {
        val result = Coordinates.createSafe(100.0, 20.0)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull() is WeatherException.InvalidCoordinatesException)
    }

    /**
     * Weryfikuje formatowanie współrzędnych do czytelnego ciągu znaków.
     */
    @Test
    fun `toReadableString poprawnie formatuje polnoc i wschod`() {
        val coords = Coordinates(52.23, 21.01)
        val formatted = coords.toReadableString()
        assertEquals("52.23°N, 21.01°E", formatted)
    }
}
