package pl.j.reinmar.modules.weather.repository

import pl.j.reinmar.modules.weather.model.Coordinates

/**
 * Kontrakt dostawcy usług geolokalizacyjnych (GPS / Lokalizacja sieciowa).
 *
 * Odpowiada za pobieranie aktualnej pozycji fizycznej urządzenia
 * oraz weryfikację stanu uprawnień, izolując system Android od logiki domenowej.
 */
interface ILocationProvider {

    /**
     * Sprawdza, czy aplikacja posiada nadane przez użytkownika uprawnienia
     * do odczytu dokładnej (FINE) lub przybliżonej (COARSE) lokalizacji.
     *
     * @return `true` jeśli uprawnienia są przyznane, w przeciwnym razie `false`.
     */
    fun hasLocationPermission(): Boolean

    /**
     * Asynchronicznie pobiera bieżące współrzędne geograficzne urządzenia.
     *
     * @return Wynik [Result] zawierający obiekt wartości [Coordinates]
     *         lub [pl.j.reinmar.modules.weather.model.WeatherException.LocationUnavailable] /
     *         [pl.j.reinmar.modules.weather.model.WeatherException.LocationPermissionDenied] w razie błędu.
     */
    suspend fun getCurrentCoordinates(): Result<Coordinates>

    /**
     * Zwraca ostatnią znaną lokalizację zapisaną w pamięci podręcznej systemu lub lokalizację domyślną.
     *
     * @return Zwraca [Coordinates] jeśli są dostępne, w przeciwnym razie domyślne [Coordinates.WARSAW].
     */
    suspend fun getLastKnownCoordinatesOrDefault(): Coordinates
}
