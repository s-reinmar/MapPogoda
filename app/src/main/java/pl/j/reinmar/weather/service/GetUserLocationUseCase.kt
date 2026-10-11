package pl.j.reinmar.modules.weather.service

import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.repository.ILocationProvider

/**
 * Przypadek użycia (Use Case) odpowiedzialny za pozyskanie bieżącej lokalizacji użytkownika.
 *
 * Realizuje zasadę pojedynczej odpowiedzialności (SRP) i hermetyzuje logikę
 * odpytywania dostawcy lokalizacji z opcją pobrania wartości rezerwowej.
 *
 * @property locationProvider Abstrakcja dostawcy geolokalizacji [ILocationProvider].
 */
class GetUserLocationUseCase(
    private val locationProvider: ILocationProvider
) {
    /**
     * Sprawdza, czy aplikacja posiada nadane uprawnienia lokalizacyjne.
     *
     * @return `true` jeśli uprawnienia są przyznane.
     */
    fun hasLocationPermission(): Boolean = locationProvider.hasLocationPermission()

    /**
     * Pobiera aktualne współrzędne GPS urządzenia.
     *
     * @return Wynik [Result] zawierający [Coordinates] lub wyjątek.
     */
    suspend fun getCurrentLocation(): Result<Coordinates> {
        return locationProvider.getCurrentCoordinates()
    }

    /**
     * Pobiera współrzędne urządzenia lub domyślną lokalizację Warszawy w przypadku braku odczytu.
     *
     * @return Obiekt [Coordinates] z bieżącymi lub domyślnymi współrzędnymi.
     */
    suspend fun getLocationOrDefault(): Coordinates {
        return locationProvider.getLastKnownCoordinatesOrDefault()
    }
}
