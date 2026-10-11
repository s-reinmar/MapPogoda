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