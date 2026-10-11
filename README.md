# MapPogoda (v1.0.0)

Aplikacja mobilna na platformę Android łącząca dane meteorologiczne z interaktywną mapą oraz geolokalizacją GPS.

---

## 🇵🇱 O Projekcie

**MapPogoda** to nowoczesna, responsywna aplikacja pogodowa zbudowana w oparciu o **Jetpack Compose** oraz **Material Design 3**. Aplikacja prezentuje bieżące warunki atmosferyczne oraz 5-dniową prognozę pogody dla aktualnej lokalizacji użytkownika (pobranej z czujnika GPS) lub dla dowolnego punktu wskazanego na zintegrowanej mapie Google Maps.

### Główne Funkcjonalności:
- **Aktualna pogoda i prognoza:** Temperatura rzeczywista i odczuwalna, wilgotność, ciśnienie atmosferyczne, prędkość i kierunek wiatru, zachmurzenie.
- **Prognoza godzinowa i 5-dniowa:** Czytelne wykresy i karty z przewidywanymi temperaturami oraz prawdopodobieństwem wystąpienia opadów deszczu/śniegu.
- **Interaktywna mapa Google Maps:** Wybór dowolnego punktu na Ziemi za pomocą pojedynczego kliknięcia, automatyczne centrowanie kamery oraz marker lokalizacji.
- **Geolokalizacja GPS (`FusedLocationProviderClient`):** Precyzyjny odczyt współrzędnych urządzenia z obsługą uprawnień czasu wykonania (Runtime Permissions dla Android 6+).
- **Graceful Degradation & Tryb Offline:** Odporność na brak dostępu do internetu, informowanie użytkownika o stanie sieci i możliwość prezentacji ostatnio pobranych danych z pamięci podręcznej.
- **Wyszukiwarka miast i konfiguracja klucza API:** Możliwość szybkiego wyszukiwania popularnych miast oraz bezpieczna zmiana klucza OpenWeather z poziomu interfejsu aplikacji.

---

## 🏛 Architektura & Stos Technologiczny

Projekt zorganizowany jest zgodnie z wzorcami **Clean Architecture**, **MVVM (Model-View-ViewModel)** oraz zasadą podziału **Package by Feature**:

```
app/src/main/java/pl/j/reinmar/modules/weather/
├── model/                     # Encje domenowe, Value Objects (Coordinates, WeatherData, WeatherForecast), wyjątki
├── repository/                # Abstrakcje i kontrakty (IWeatherRepository, ILocationProvider) - DIP, ISP
├── infrastructure/            # Implementacje (WeatherRepositoryImpl, GpsLocationProvider, OpenWeatherApi, DTO)
├── service/                   # Przypadki użycia (Use Cases) oraz wstrzykiwanie zależności (WeatherServiceLocator)
└── view/                      # Interfejs Jetpack Compose (WeatherViewModel, WeatherMainScreen, WeatherMapScreen, komponenty)
```

- **Język:** Kotlin (100% idiomatyczny, Coroutines, StateFlow, niemutowalne obiekty danych).
- **UI:** Jetpack Compose (Material Design 3, dynamiczne motywy, Edge-to-Edge).
- **Sieć:** Retrofit 2, Moshi, OkHttp 4 z interfejsem logowania.
- **Lokalizacja:** Google Play Services Location (`FusedLocationProviderClient`).
- **Mapy:** Google Maps Compose (`com.google.maps.android:maps-compose`).
- **Zarządzanie sekretami:** `secrets-gradle-plugin` (bezpieczne wstrzykiwanie kluczy z pliku `.env` do `BuildConfig`).
- **Testy:** JUnit 4, Robolectric, Kotlinx Coroutines Test (100% sukcesu testów jednostkowych i integracyjnych).

---

## 🔑 Konfiguracja Kluczy API

Aplikacja wykorzystuje dwa zewnętrzne interfejsy API:
1. **OpenWeather API:** do pobierania prognozy pogody.
2. **Google Maps SDK for Android:** do wyświetlania mapy.

### Krok 1: Utworzenie pliku `.env`
W katalogu głównym projektu skopiuj szablon `.env.example` do `.env`:
```properties
OPENWEATHER_API_KEY=twoj_klucz_openweather
MAPS_API_KEY=twoj_klucz_google_maps
```

*Plik `.env` jest automatycznie ignorowany przez system kontroli wersji (`.gitignore`), co zabezpiecza klucze przed wyciekiem.*

Możesz również podać lub zmienić klucz OpenWeather w samej aplikacji: zakładka **Szukaj** -> **Klucz API OpenWeather** -> **Zmień klucz API**.

---

## 🚀 Uruchomienie i Budowanie Projektu

### Kompilacja aplikacji:
```bash
./gradlew assembleDebug
```

### Uruchomienie testów jednostkowych i Robolectric:
```bash
./gradlew testDebugUnitTest
```

---

## 🇬🇧 English Summary

**MapPogoda** is an Android weather application that pairs real-time weather analytics with Google Maps and GPS geolocation. Built with **Kotlin**, **Jetpack Compose (Material 3)**, and **Clean Architecture / MVVM**.

### Key Highlights:
- Live weather, hourly forecasts, and 5-day summaries powered by OpenWeather API.
- Fully interactive Google Maps pane with pin drop and location-based weather fetching.
- Robust GPS integration with runtime permission handling.
- Zero API keys hardcoded in source code (injected via `secrets-gradle-plugin` and `.env`).
- 26 unit and Robolectric tests covering domain models, repositories, and ViewModels.

---

## 📄 Licencja

Projekt stworzony na otwartej licencji MIT.
Wersja: **v1.0.0**
Pakiet: `pl.j.reinmar.modules.weather`
