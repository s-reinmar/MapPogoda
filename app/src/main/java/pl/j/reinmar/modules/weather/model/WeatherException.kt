package pl.j.reinmar.modules.weather.model

/**
 * Bazowy wyjątek domenowy dla modułu meteorologicznego.
 *
 * Wszystkie błędy występujące w warstwie biznesowej i integracyjnej
 * są mapowane na jawne podklasy tego wyjątku.
 *
 * @param message Zrozumiały dla użytkownika komunikat błędu w języku polskim.
 * @param cause Opcjonalny wyjątek źródłowy.
 */
sealed class WeatherException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Błąd sygnalizujący brak łączności z siecią internetową.
     *
     * @param cause Wyjątek sieciowy (np. UnknownHostException, SocketTimeoutException).
     */
    class NetworkUnavailable(cause: Throwable? = null) :
        WeatherException("Brak połączenia z siecią internetową. Sprawdź połączenie Wi-Fi lub transmisję danych.", cause)

    /**
     * Błąd sygnalizujący brak wymaganych uprawnień do odczytu lokalizacji urządzenia.
     */
    class LocationPermissionDenied :
        WeatherException("Aplikacja nie posiada uprawnień do lokalizacji GPS. Włącz uprawnienia w ustawieniach systemu.")

    /**
     * Błąd sygnalizujący niedostępność modułu lub sygnału GPS.
     */
    class LocationUnavailable(cause: Throwable? = null) :
        WeatherException("Nie udało się ustalić pozycji GPS. Upewnij się, że lokalizacja w urządzeniu jest włączona.", cause)

    /**
     * Błąd sygnalizujący nieprawidłowy, brakujący lub zablokowany klucz API.
     */
    class InvalidApiKey :
        WeatherException("Nieprawidłowy lub brakujący klucz API dostawcy pogody. Skonfiguruj poprawny klucz w pliku .env.")

    /**
     * Błąd sygnalizujący przekroczenie limitu zapytań (HTTP 429).
     */
    class ApiRateLimitExceeded :
        WeatherException("Przekroczono dopuszczalny limit zapytań do serwera pogodowego. Spróbuj ponownie za chwilę.")

    /**
     * Błąd odpowiedzi serwera dostawcy danych meteorologicznych (kody 5xx).
     *
     * @property httpCode Kod odpowiedzi HTTP zwrócony przez serwer.
     */
    class ServerSideError(val httpCode: Int) :
        WeatherException("Serwer pogodowy zgłosił błąd (kod HTTP: $httpCode). Spróbuj ponownie później.")

    /**
     * Błąd walidacji wprowadzonych współrzędnych geograficznych.
     *
     * @property latitude Podana szerokość geograficzna.
     * @property longitude Podana długość geograficzna.
     * @param details Szczegółowy opis naruszenia reguły walidacyjnej.
     */
    class InvalidCoordinatesException(
        val latitude: Double,
        val longitude: Double,
        details: String
    ) : WeatherException("Nieprawidłowe współrzędne geograficzne ($latitude, $longitude): $details")

    /**
     * Błąd oznaczający, że wskazana lokalizacja lub miasto nie zostało odnalezione.
     *
     * @property query Nazwa poszukiwanego miasta lub zapytanie.
     */
    class CityNotFound(val query: String) :
        WeatherException("Nie odnaleziono danych pogodowych dla lokalizacji: $query.")

    /**
     * Ogólny błąd przetwarzania danych lub błąd integracji.
     *
     * @param customMessage Własny komunikat błędu.
     * @param cause Wyjątek źródłowy.
     */
    class GenericError(customMessage: String, cause: Throwable? = null) :
        WeatherException(customMessage, cause)
}
