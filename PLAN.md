# PLAN.md — Plan Architektury i Realizacji Aplikacji Pogoda GPS

## 1. Cel i Zakres Projektu
Aplikacja mobilna na platformę Android służąca do monitorowania bieżącej pogody oraz szczegółowej prognozy pogody na podstawie bieżącej lokalizacji użytkownika (GPS) lub punktu wskazanego na interaktywnej mapie (Google Maps SDK).

### Główne cele:
- Płynne pobieranie geolokalizacji z wykorzystaniem `FusedLocationProviderClient`.
- Obsługa uprawnień w czasie wykonywania aplikacji (Runtime Permissions dla Android 6.0+ / API 23+).
- Pobieranie danych meteorologicznych (aktualna pogoda, prognoza godzinowa i kilkudniowa, wskaźniki jakości powietrza/parametry wiatru, ciśnienia, wilgotności) z OpenWeather API.
- Interaktywna mapa Google Maps z obsługą wyboru dowolnej lokalizacji przez kliknięcie oraz centrowania na pozycji GPS.
- Tryb offline i odporność na brak połączenia internetowego (Graceful Degradation, informowanie użytkownika o stanie sieci).
- Bezpieczne zarządzanie kluczami API przez `BuildConfig` i `secrets-gradle-plugin`.
- Pełna polskojęzyczna dokumentacja KDoc dla wszystkich klas, interfejsów i metod.

---

## 2. Stos Technologiczny i Narzędzia
- **Język:** Kotlin (100% idiomatyczny, immutability, `data class`, Coroutines, Flow).
- **Interfejs użytkownika:** Jetpack Compose (Material Design 3, dynamiczne motywy, Edge-to-Edge).
- **Architektura:** MVVM + Clean Architecture / Package by Feature (`pl.j.reinmar.modules.weather`).
- **Warstwa sieciowa:** Retrofit 2 + Moshi + OkHttp + Logging Interceptor.
- **Geolokalizacja:** Google Play Services Location (`FusedLocationProviderClient`).
- **Mapy:** Google Maps Compose (`com.google.maps.android:maps-compose`) + Play Services Maps.
- **Wstrzykiwanie zależności:** Wstrzykiwanie przez konstruktory (Constructor Injection) i moduł ServiceLocator/Factory.
- **Testy:** JUnit4, Robolectric, Kotlinx Coroutines Test.

---

## 3. Struktura Modułowa ("Package by Feature")
```
app/src/main/java/pl/j/reinmar/modules/weather/
├── model/                     # Encje domenowe, modele wartości (VO), wyjątki, fabryki
│   ├── WeatherData.kt         # Model aktualnej pogody (temperatura, warunki, wiatr, ciśnienie itp.)
│   ├── ForecastItem.kt        # Element prognozy godzinowej / dziennej
│   ├── Coordinates.kt         # Współrzędne geograficzne z walidacją
│   └── WeatherException.kt    # Dedykowane wyjątki domenowe (brak sieci, błąd API, brak GPS)
├── repository/                # Kontrakty i abstrakcje dostępu do danych
│   ├── IWeatherRepository.kt  # Interfejs repozytorium pogodowego
│   └── ILocationProvider.kt   # Interfejs dostawcy lokalizacji (GPS)
├── infrastructure/            # Implementacje techniczne i adaptory
│   ├── api/
│   │   ├── OpenWeatherApi.kt  # Interfejs Retrofit dla OpenWeather
│   │   └── dto/               # Obiekty transferu danych (DTO) dla JSON
│   ├── WeatherRepositoryImpl.kt # Implementacja repozytorium z obsługą błędów
│   └── GpsLocationProvider.kt # Implementacja geolokalizacji z FusedLocationProvider
├── service/                   # Przypadki użycia (Use Cases / Services)
│   ├── GetCurrentWeatherUseCase.kt
│   ├── GetForecastUseCase.kt
│   ├── GetUserLocationUseCase.kt
│   └── WeatherServiceLocator.kt # Fabryka i dostawca instancji (DI)
└── view/                      # Warstwa prezentacji Jetpack Compose
    ├── WeatherViewModel.kt    # ViewModel z StateFlow (UiState)
    ├── WeatherUiState.kt      # Stany UI (Ładowanie, Sukces, Błąd, Brak uprawnień)
    ├── WeatherScreen.kt       # Główny ekran z kartami pogodowymi i zakładkami
    ├── WeatherMapScreen.kt    # Widok mapy Google z wyborem współrzędnych
    └── components/            # Komponenty wielokrotnego użytku (karty, wskaźniki, wykresy)
```

---

## 4. Punkty Awarii i Scenariusze Brzegowe (Edge Cases & Failure Points)
1. **Odmowa uprawnień lokalizacyjnych:**
   - Użytkownik odrzuca uprawnienie `ACCESS_FINE_LOCATION` i `ACCESS_COARSE_LOCATION`.
   - *Rozwiązanie:* Aplikacja przechodzi do domyślnej lokalizacji (np. Warszawa) z jasnym komunikatem i możliwością wyboru miasta na mapie lub ręcznego wyszukiwania.
2. **Wyłączony moduł GPS w urządzeniu:**
   - *Rozwiązanie:* Wykrycie braku dostępności dostawcy lokalizacji i sugestia włączenia GPS lub wskazania punktu na mapie.
3. **Brak połączenia sieciowego (Offline):**
   - *Rozwiązanie:* Zwrócenie błędu domenowego `WeatherException.NetworkUnavailable`, wyświetlenie komunikatu w UI z przyciskiem ponowienia (Retry) oraz zachowanie ostatnio pobranych danych w pamięci podręcznej stanu.
4. **Błędny lub brakujący klucz API:**
   - *Rozwiązanie:* Obsługa kodu 401 z OpenWeather API i przejście do przyjaznego komunikatu o konfiguracji klucza w `.env`.
5. **Przekroczenie limitu zapytań (Rate Limiting / HTTP 429):**
   - *Rozwiązanie:* Zabezpieczenie przed nadmiarowymi zapytaniami (debouncing przy przesuwaniu mapy), czytelny komunikat błędu.
6. **Ekstremalne lub nieprawidłowe współrzędne geograficzne:**
   - Walidacja zakresów szerokości [-90, 90] i długości [-180, 180] geograficznej w klasie wartości `Coordinates`.

---

## 5. Przypadki Testowe (QA Test Matrix)
- **TC-01:** Pobranie pogody dla poprawnych współrzędnych GPS -> Status sukces, prawidłowe mapowanie temperatur z Kelvinów/Celsjusza.
- **TC-02:** Walidacja `Coordinates` -> Odrzucenie nieprawidłowych współrzędnych i rzucenie `DomainException`.
- **TC-03:** Brak internetu podczas zapytania -> Rzucenie `WeatherException.NetworkUnavailable`, odpowiedni stan w `WeatherUiState.Error`.
- **TC-04:** Błąd autoryzacji API (HTTP 401) -> `WeatherException.Unauthorized`.
- **TC-05:** Wybór nowego punktu na mapie -> Odświeżenie danych pogodowych dla nowej lokalizacji.

---

## 6. Backlog Zadań (Roadmap)
- [x] **Krok 0:** Inicjalizacja środowiska, konfiguracja zależności, Secrets Plugin, uprawnienia i ikona aplikacji.
- [x] **Krok 1:** Analiza wymagań, architektura domenowa, utworzenie `PLAN.md` i `WORK_LOG.md`.
- [x] **Krok 2:** Warstwa Domeny & Kontraktów (`model/` oraz `repository/`).
- [x] **Krok 3:** Warstwa Logiki i Danych (`service/` oraz `infrastructure/` dla OpenWeather i GPS).
- [x] **Krok 4:** Warstwa Prezentacji Jetpack Compose (`view/`, `WeatherViewModel`, integracja Google Maps).
- [x] **Krok 5:** Testy jednostkowe, integracyjne i brzegowe Robolectric/JUnit (`test/`).
- [ ] **Krok 6:** Domknięcie iteracji, podsumowanie i aktualizacja statusów.
