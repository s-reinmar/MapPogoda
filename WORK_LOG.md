# WORK_LOG.md — Dziennik Operacyjny Prac Projektowych

## Iteracja 0: Inicjalizacja Środowiska i Konfiguracja (2026-10-10)

### Zakres i cel:
Przygotowanie środowiska deweloperskiego, wdrożenie zależności Gradle, konfiguracja bezpiecznego zarządzania sekretami i kluczami API oraz identyfikacja wizualna aplikacji.

### Zmodyfikowane i wygenerowane pliki:
- `gradle/libs.versions.toml`: Dodanie zależności `play-services-maps` (19.0.0) oraz `maps-compose` (6.4.1).
- `app/build.gradle.kts`: Ustawienie unikalnego `applicationId` (`com.aistudio.weathergps.kpxwya`), odkomentowanie i dodanie `play-services-location`, `coil-compose`, `maps-compose`, `play-services-maps`, `converter-moshi`, `retrofit`.
- `metadata.json`: Nadanie nazwy "Pogoda GPS" oraz opisu projektu.
- `settings.gradle.kts` oraz `app/src/main/res/values/strings.xml`: Ustawienie nazwy projektu i aplikacji na "Pogoda GPS".
- `.env.example` oraz `.env`: Dodanie szablonów zmiennych środowiskowych `OPENWEATHER_API_KEY` oraz `MAPS_API_KEY`.
- `app/src/main/AndroidManifest.xml`: Dodanie uprawnień `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION` oraz metadanych klucza Google Maps.
- `app/src/main/res/drawable/ic_launcher_foreground.xml` i `ic_launcher_background.xml` oraz ikony rasterowe w `mipmap-*`: Wygenerowanie i wdrożenie adaptacyjnej ikony launchera zgodnie ze standardem Material You.

### Decyzje Architektoniczne (ADR):
- **ADR-001 (Bezpieczeństwo sekretów):** Zastosowano `secrets-gradle-plugin` w połączeniu z plikiem `.env` (znajdującym się w `.gitignore`), eliminując ryzyko wycieku kluczy API do repozytorium kodu.
- **ADR-002 (Interfejs użytkownika):** Zdecydowano o wykorzystaniu Jetpack Compose oraz biblioteki `maps-compose` zamiast tradycyjnych widoków XML i MapView, zapewniając pełną deklaratywność i spójność z Material 3.

### Wyniki weryfikacji kompilacji:
- `compile_applet`: Zakończona sukcesem (BUILD SUCCESSFUL).

---

## Iteracja 1: Planowanie Architektoniczne i Analiza Wymagań (2026-10-10)

### Zakres i cel:
- Zdefiniowanie granic modułu pogody (`com.example.modules.weather`), dekompozycja zadań, określenie reguł domenowych i obsługi błędów.
- Przygotowanie dokumentów `PLAN.md` oraz `WORK_LOG.md`.
- Zaprojektowanie struktury "Package by Feature" pod kątem zasad Clean Architecture i SOLID.

### Zmodyfikowane i wygenerowane pliki:
- `PLAN.md`: Stworzenie całościowego planu wdrożenia, macierzy testowej, punktów awarii i harmonogramu.
- `WORK_LOG.md`: Inicjalizacja dziennika operacyjnego z rejestracją decyzji ADR.

### Zidentyfikowane punkty awarii i mitigacje:
1. *Brak zgody na uprawnienia GPS:* Fallback na domyślną lokalizację (Warszawa / 52.2297, 21.0122) z opcją interaktywnego wyboru punktu na mapie.
2. *Brak internetu:* Zwracanie dedykowanego błędu domenowego `WeatherException.NetworkUnavailable`, uniemożliwienie zawieszenia UI i udostępnienie przycisku ponowienia.
3. *Niepoprawny klucz OpenWeather API:* Obsługa kodów HTTP 401 i 403 z czytelnym komunikatem o konieczności konfiguracji klucza w panelu sekretów.
4. *Walidacja współrzędnych:* Enkapsulacja w klasie domenowej `Coordinates` z rzuceniem wyjątku przy naruszeniu zakresu wartości szerokości i długości geograficznej.

---

## Iteracja 2: Warstwa Domeny & Kontraktów (2026-10-10)

### Zakres i cel:
- Implementacja encji, modeli wartości (Value Objects), agregatów oraz hierarchii wyjątków w pakiecie `pl.j.reinmar.modules.weather.model`.
- Zdefiniowanie interfejsów kontraktów `IWeatherRepository` oraz `ILocationProvider` w pakiecie `pl.j.reinmar.modules.weather.repository`.
- Pokrycie wszystkich klas, metod i właściwości 100% polskojęzyczną dokumentacją KDoc.

### Zmodyfikowane i wygenerowane pliki:
- `pl/j/reinmar/modules/weather/model/Coordinates.kt`: Obiekt wartości z rygorystyczną walidacją zakresu WGS 84 i fabryką `createSafe`.
- `pl/j/reinmar/modules/weather/model/WeatherCondition.kt`: Typ wyliczeniowy z polskimi nazwami zjawisk oraz mapowaniem kodów OpenWeather.
- `pl/j/reinmar/modules/weather/model/WeatherData.kt`: Główna niemutowalna encja aktualnej pogody z metodami pomocniczymi (formatowanie, wskaźniki mrozu, konwersja jednostek).
- `pl/j/reinmar/modules/weather/model/ForecastItem.kt`: Model pojedynczego punktu prognozy (formatowanie dat, godzin, prawdopodobieństwa opadów).
- `pl/j/reinmar/modules/weather/model/WeatherForecast.kt`: Agregat prognozy pogody z metodami grupowania dobowego (`getDailySummaries`) i godzinowego (`getHourly`).
- `pl/j/reinmar/modules/weather/model/WeatherException.kt`: Hierarchia wyjątków domenowych (błędy sieci, uprawnień, GPS, autoryzacji API, błędnych współrzędnych).
- `pl/j/reinmar/modules/weather/repository/IWeatherRepository.kt`: Kontrakt dostępu do danych pogodowych (DIP, ISP).
- `pl/j/reinmar/modules/weather/repository/ILocationProvider.kt`: Kontrakt dostawcy lokalizacji fizycznej GPS.

### Zgodność z regułami:
- Zastosowanie pakietu `pl.j.reinmar` zgodnie z życzeniem użytkownika.
- 100% polskojęzyczny KDoc.
- Brak jakichkolwiek zależności od frameworków zewnętrznych (UI / Retrofit / Android SDK) w warstwie `model/`.
- Kompilacja zweryfikowana z sukcesem (`compile_applet`).

---

## Iteracja 3: Warstwa Logiki i Danych (2026-10-10)

### Zakres i cel:
- Implementacja obiektów DTO dla OpenWeather v2.5 (`CurrentWeatherDto`, `ForecastDto`).
- Implementacja interfejsu klienta Retrofit `OpenWeatherApi`.
- Implementacja produkcyjnego repozytorium `WeatherRepositoryImpl` z bezpieczną obsługą kluczy API, mapowaniem do encji i translacją wyjątków HTTP/sieciowych.
- Implementacja `GpsLocationProvider` z integracją `FusedLocationProviderClient`, bezpieczną weryfikacją uprawnień czasu wykonania (Runtime Permissions) i obsługą asynchroniczną przez `suspendCancellableCoroutine`.
- Wdrożenie dedykowanych przypadków użycia (Use Cases): `GetCurrentWeatherUseCase`, `GetForecastUseCase`, `GetUserLocationUseCase`, `SearchCityWeatherUseCase`.
- Centralny kontener wstrzykiwania zależności `WeatherServiceLocator`.
- Pełna polskojęzyczna dokumentacja KDoc dla wszystkich klas, metod i parametrów.

### Zmodyfikowane i wygenerowane pliki:
- `pl/j/reinmar/modules/weather/infrastructure/api/dto/CurrentWeatherDto.kt`: DTO bieżącej pogody z adapterami Moshi.
- `pl/j/reinmar/modules/weather/infrastructure/api/dto/ForecastDto.kt`: DTO 5-dniowej prognozy pogody.
- `pl/j/reinmar/modules/weather/infrastructure/api/OpenWeatherApi.kt`: Kontrakt Retrofit dla zapytań pogodowych (GPS i po nazwie miasta).
- `pl/j/reinmar/modules/weather/infrastructure/WeatherRepositoryImpl.kt`: Implementacja repozytorium (obsługa błędów 401, 403, 404, 429, 5xx, sieciowych).
- `pl/j/reinmar/modules/weather/infrastructure/GpsLocationProvider.kt`: Integracja z Google Play Services Fused Location.
- `pl/j/reinmar/modules/weather/service/GetCurrentWeatherUseCase.kt`: Przypadek użycia pobierania aktualnej pogody.
- `pl/j/reinmar/modules/weather/service/GetForecastUseCase.kt`: Przypadek użycia pobierania prognozy.
- `pl/j/reinmar/modules/weather/service/GetUserLocationUseCase.kt`: Przypadek użycia pobierania współrzędnych użytkownika.
- `pl/j/reinmar/modules/weather/service/SearchCityWeatherUseCase.kt`: Przypadek użycia wyszukiwania pogody po nazwie miasta.
- `pl/j/reinmar/modules/weather/service/WeatherServiceLocator.kt`: Fabryka DI (Moshi, OkHttp, Retrofit, Use Cases).

### Zgodność z regułami:
- Zastosowanie zasad SOLID (SRP, DIP, ISP, OCP).
- Pełna separacja warstw: brak zależności warstwy domenowej od warstwy infrastruktury.
- Bezpieczeństwo kluczy: odczyt z `BuildConfig` bez twardego kodowania wartości w kodzie źródłowym.
- Pomyślna weryfikacja kompilacji (`compile_applet`).

---

## Iteracja 4: Warstwa Prezentacji Jetpack Compose (2026-10-10)

### Zakres i cel:
- Implementacja modelu stanu interfejsu `WeatherUiState` oraz zakładek `WeatherTab`.
- Implementacja `WeatherViewModel` z zarządzaniem stanem (`StateFlow`), obsługą błędów, fallbackiem offline i reakcją na uprawnienia GPS.
- Implementacja komponentów UI Material 3:
  - `WeatherTopBar`: Górny pasek z nazwą lokalizacji, współrzędnymi, wskaźnikiem źródła (GPS vs MAPA) i odświeżaniem.
  - `CurrentWeatherCard`: Główna karta z gradientem tematycznym, wielką temperaturą, ikoną pogody i temperaturą odczuwalną.
  - `HourlyForecastRow`: Poziomy pasek kart godzinowych (godzina, dzień, ikona, temperatura, prawdopodobieństwo opadów).
  - `DailyForecastList`: Lista 5-dniowej prognozy dobowej z podsumowaniem minimów i maksimów.
  - `WeatherMetricsGrid`: Siatka 4 kafelków ze wskaźnikami (Wiatr, Wilgotność, Ciśnienie, Zachmurzenie).
  - `ApiKeyDialog`: Okno konfiguracji i aktualizacji klucza OpenWeather API.
  - `WeatherMapScreen`: Integracja z Google Maps (`maps-compose`), wybór punktu przez kliknięcie na mapie, animacja kamery, przycisk centrowania na GPS oraz podgląd klikniętego punktu z przyciskiem pobrania pogody.
  - `WeatherMainScreen`: Płynna nawigacja w dolnym pasku (Pulpit, Mapa GPS, Wyszukiwarka/Ustawienia), obsługa stanów Loading, Success, Error (z opcją Offline Cache) i PermissionRequired.
  - `MainActivity`: Integracja `enableEdgeToEdge`, `rememberLauncherForActivityResult` dla uprawnień `ACCESS_FINE_LOCATION` i `ACCESS_COARSE_LOCATION`.

### Zmodyfikowane i wygenerowane pliki:
- `pl/j/reinmar/modules/weather/view/WeatherUiState.kt`
- `pl/j/reinmar/modules/weather/view/WeatherViewModel.kt`
- `pl/j/reinmar/modules/weather/view/components/WeatherTopBar.kt`
- `pl/j/reinmar/modules/weather/view/components/CurrentWeatherCard.kt`
- `pl/j/reinmar/modules/weather/view/components/HourlyForecastRow.kt`
- `pl/j/reinmar/modules/weather/view/components/DailyForecastList.kt`
- `pl/j/reinmar/modules/weather/view/components/WeatherMetricsGrid.kt`
- `pl/j/reinmar/modules/weather/view/components/ApiKeyDialog.kt`
- `pl/j/reinmar/modules/weather/view/WeatherMapScreen.kt`
- `pl/j/reinmar/modules/weather/view/WeatherMainScreen.kt`
- `com/example/MainActivity.kt`

### Zgodność z regułami:
- Zastosowanie unikalnych `Modifier.testTag` dla wszystkich kluczowych elementów UI.
- Spełnienie wytycznych dostępności (minimalny rozmiar klikalny 48dp, opisy `contentDescription`).
- 100% polskojęzyczna dokumentacja KDoc.
- Kompilacja zweryfikowana z sukcesem (`compile_applet`).

---

## Iteracja 5: Weryfikacja QA, Testy i Refaktoryzacja (2026-10-10)

### Zakres i cel:
- Implementacja kompleksowego zestawu testów jednostkowych i integracyjnych pokrywających warstwę domeny, repozytorium oraz ViewModel.
- Pokrycie scenariuszy sukcesu, warunków brzegowych (edge cases), punktów awarii oraz błędów sieci/API.
- Weryfikacja zgodności z regułami SOLID, hermetyzacji i 100% polskojęzyczną dokumentacją KDoc.

### Zmodyfikowane i wygenerowane pliki testowe:
- `pl/j/reinmar/modules/weather/model/CoordinatesTest.kt`: Testy walidacji szerokości i długości geograficznej WGS 84, rzucania wyjątków, formatowania stringów i fabryki `createSafe` (TC-02).
- `pl/j/reinmar/modules/weather/model/WeatherDataTest.kt`: Testy przeliczeń jednostek (m/s -> km/h), zaokrąglania temperatur, określania kierunku wiatru i wykrywania mrozu.
- `pl/j/reinmar/modules/weather/model/WeatherForecastTest.kt`: Testy logiki dobowego agregowania minimów i maksimów (`getDailySummaries`) oraz podziału godzinowego (`getHourly`).
- `pl/j/reinmar/modules/weather/infrastructure/WeatherRepositoryImplTest.kt`: Testy mapowania DTO na domenę (TC-01), translacji błędów sieciowych (TC-03), błędów HTTP 401 (TC-04) i 404.
- `pl/j/reinmar/modules/weather/view/WeatherViewModelTest.kt`: Testy przepływów UI (PermissionRequired -> Success, przełączanie zakładek, wyszukiwanie miast, obsługa błędów).
- `com/example/ExampleRobolectricTest.kt`: Aktualizacja asercji `app_name` dla zgodności z nazwą "Pogoda GPS".
- `app/build.gradle.kts`: Dodanie `manifestPlaceholders["MAPS_API_KEY"] = "dummy_maps_key"` do `defaultConfig` na potrzeby bezbłędnej fuzji manifestu w testach jednostkowych.

### Wyniki testów QA:
- Wykonano zadanie Gradle `:app:testDebugUnitTest`.
- Wynik: **26/26 testów zakończonych pełnym sukcesem (BUILD SUCCESSFUL)**.
- Kompilacja aplikacji produkcyjnej: **BUILD SUCCESSFUL** (`compile_applet`).




