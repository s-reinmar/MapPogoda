package pl.j.reinmar.modules.weather.view

import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.model.WeatherData
import pl.j.reinmar.modules.weather.model.WeatherException
import pl.j.reinmar.modules.weather.model.WeatherForecast

/**
 * Reprezentacja stanu interfejsu użytkownika (UI State) dla ekranu pogody.
 *
 * Klasa zamknięta (sealed interface) modeluje wszystkie możliwe stany widoku,
 * gwarantując jednoźródłowość prawdy (Single Source of Truth) i bezpieczeństwo typów.
 */
sealed interface WeatherUiState {

    /**
     * Stan początkowy lub trwania operacji pobierania danych z sieci / GPS.
     *
     * @property message Opcjonalny opis bieżącego etapu ładowania w języku polskim.
     */
    data class Loading(
        val message: String = "Pobieranie danych pogodowych..."
    ) : WeatherUiState

    /**
     * Stan sukcesu z kompletem aktualnych danych meteorologicznych i prognozy.
     *
     * @property weather Aktualny stan pogody [WeatherData].
     * @property forecast Agregat prognozy pogody [WeatherForecast].
     * @property isGpsLocation Flaga informująca, czy współrzędne pochodzą z czujnika GPS (`true`), czy z mapy/wyszukiwania (`false`).
     * @property activeCoordinates Bieżące współrzędne punktu pomiarowego [Coordinates].
     * @property isRefreshing Flaga oznaczająca odświeżanie w tle (np. pull-to-refresh).
     */
    data class Success(
        val weather: WeatherData,
        val forecast: WeatherForecast,
        val isGpsLocation: Boolean,
        val activeCoordinates: Coordinates,
        val isRefreshing: Boolean = false
    ) : WeatherUiState

    /**
     * Stan błędu operacji (brak sieci, niepoprawny klucz, błąd GPS).
     *
     * @property exception Wyjątek domenowy [WeatherException] z polskim opisem błędu.
     * @property fallbackCoordinates Ostatnio zapamiętane lub domyślne współrzędne.
     * @property cachedWeather Opcjonalne poprzednio załadowane dane do wyświetlenia w trybie offline.
     */
    data class Error(
        val exception: WeatherException,
        val fallbackCoordinates: Coordinates? = null,
        val cachedWeather: WeatherData? = null
    ) : WeatherUiState

    /**
     * Stan informujący o konieczności przyznania uprawnień lokalizacyjnych przez użytkownika.
     */
    object PermissionRequired : WeatherUiState
}

/**
 * Zakładki nawigacyjne w głównym interfejsie aplikacji.
 *
 * @property title Tytuł zakładki w języku polskim.
 * @property testTag Identyfikator testowy dla automatyzacji testów UI.
 */
enum class WeatherTab(val title: String, val testTag: String) {
    /** Główny pulpit pogodowy (aktualna pogoda i prognoza). */
    DASHBOARD("Pogoda", "tab_dashboard"),

    /** Interaktywna mapa Google Maps. */
    MAP("Mapa GPS", "tab_map"),

    /** Wyszukiwarka i konfiguracja klucza API. */
    SETTINGS("Szukaj / Klucz API", "tab_settings")
}
