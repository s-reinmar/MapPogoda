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
