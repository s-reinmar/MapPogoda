package pl.j.reinmar.modules.weather.view

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.model.WeatherData
import pl.j.reinmar.modules.weather.model.WeatherException
import pl.j.reinmar.modules.weather.model.WeatherForecast
import pl.j.reinmar.modules.weather.service.GetCurrentWeatherUseCase
import pl.j.reinmar.modules.weather.service.GetForecastUseCase
import pl.j.reinmar.modules.weather.service.GetUserLocationUseCase
import pl.j.reinmar.modules.weather.service.SearchCityWeatherUseCase
import pl.j.reinmar.modules.weather.service.WeatherServiceLocator

/**
 * ViewModel zarządzający stanem prezentacji danych pogodowych w architekturze MVVM.
 *
 * Klasa odpowiada za koordynację pobierania pozycji GPS, asynchroniczne wywoływanie
 * przypadków użycia (Use Cases) oraz emisję niemutowalnego stanu interfejsu [WeatherUiState].
 *
 * @property getCurrentWeatherUseCase Przypadek użycia pobierania aktualnej pogody.
 * @property getForecastUseCase Przypadek użycia pobierania wielodniowej prognozy.
 * @property getUserLocationUseCase Przypadek użycia odczytu pozycji GPS użytkownika.
 * @property searchCityWeatherUseCase Przypadek użycia wyszukiwania pogody po nazwie miasta.
 */
class WeatherViewModel(
    private val getCurrentWeatherUseCase: GetCurrentWeatherUseCase = WeatherServiceLocator.provideGetCurrentWeatherUseCase(),
    private val getForecastUseCase: GetForecastUseCase = WeatherServiceLocator.provideGetForecastUseCase(),
    private val getUserLocationUseCase: GetUserLocationUseCase,
    private val searchCityWeatherUseCase: SearchCityWeatherUseCase = WeatherServiceLocator.provideSearchCityWeatherUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading("Inicjalizacja modułu pogodowego..."))
    /**
     * Strumień stanu interfejsu użytkownika obserwowany przez komponenty Compose.
     */
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    private val _currentTab = MutableStateFlow(WeatherTab.DASHBOARD)
    /**
     * Aktualnie wybrana zakładka nawigacyjna.
     */
    val currentTab: StateFlow<WeatherTab> = _currentTab.asStateFlow()

    private val _mapCoordinates = MutableStateFlow(Coordinates.WARSAW)
    /**
     * Współrzędne aktualnie wybrane na mapie (punkt z markerem).
     */
    val mapCoordinates: StateFlow<Coordinates> = _mapCoordinates.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    /**
     * Tekst wprowadzany w polu wyszukiwania miasta.
     */
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    /**
     * Ostatnie pomyślnie pobrane dane pogodowe do buforowania w razie awarii sieci.
     */
    private var lastSuccessfulWeather: WeatherData? = null
    private var lastSuccessfulForecast: WeatherForecast? = null
    private var lastCoordinates: Coordinates = Coordinates.WARSAW

    init {
        initialLoad()
    }

    /**
     * Wykonuje początkowe sprawdzenie uprawnień i pobranie pogody dla pozycji GPS lub domyślnej.
     */
    fun initialLoad() {
        if (!getUserLocationUseCase.hasLocationPermission()) {
            _uiState.value = WeatherUiState.PermissionRequired
        } else {
            loadForUserLocation()
        }
    }

    /**
     * Przełącza aktualną zakładkę nawigacyjną.
     *
     * @param tab Nowa zakładka [WeatherTab].
     */
    fun selectTab(tab: WeatherTab) {
        _currentTab.value = tab
    }

    /**
     * Aktualizuje tekst zapytania w polu wyszukiwania.
     *
     * @param query Wprowadzany ciąg znaków.
     */
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    /**
     * Zmienia punkt zaznaczony na mapie Google.
     *
     * @param coordinates Nowo kliknięte współrzędne na mapie.
     */
    fun onMapCoordinatesSelected(coordinates: Coordinates) {
        _mapCoordinates.value = coordinates
    }

    /**
     * Pobiera pogodę i prognozę na podstawie bieżącej pozycji GPS użytkownika.
     */
    fun loadForUserLocation() {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading("Ustalanie pozycji GPS...")

            val locationResult = getUserLocationUseCase.getCurrentLocation()
            val coordinates = locationResult.getOrElse {
                // Gdy GPS niedostępny, użyj ostatniej znanej lub domyślnej lokalizacji
                getUserLocationUseCase.getLocationOrDefault()
            }

            _mapCoordinates.value = coordinates
            fetchWeatherData(coordinates, isGps = locationResult.isSuccess)
        }
    }

    /**
     * Pobiera pogodę i prognozę dla wskazanych współrzędnych geograficznych.
     *
     * @param coordinates Docelowe współrzędne.
     * @param isGps Flaga oznaczająca, czy współrzędne pochodzą z GPS.
     */
    fun loadForCoordinates(coordinates: Coordinates, isGps: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading("Pobieranie pogody dla wybranego punktu...")
            _mapCoordinates.value = coordinates
            fetchWeatherData(coordinates, isGps = isGps)
        }
    }

    /**
     * Wyszukuje pogodę po tekstowej nazwie miasta.
     *
     * @param cityName Nazwa miasta wpisana w formularzu.
     */
    fun searchCity(cityName: String) {
        val trimmed = cityName.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading("Wyszukiwanie miasta: $trimmed...")
            val result = searchCityWeatherUseCase(trimmed)

            result.fold(
                onSuccess = { weather ->
                    _mapCoordinates.value = weather.coordinates
                    lastSuccessfulWeather = weather
                    lastCoordinates = weather.coordinates

                    // Pobranie prognozy dla odnalezionego miasta
                    val forecastResult = getForecastUseCase(weather.coordinates)
                    val forecast = forecastResult.getOrElse {
                        WeatherForecast(
                            locationName = weather.locationName,
                            countryCode = weather.countryCode,
                            coordinates = weather.coordinates,
                            items = emptyList()
                        )
                    }
                    lastSuccessfulForecast = forecast

                    _uiState.value = WeatherUiState.Success(
                        weather = weather,
                        forecast = forecast,
                        isGpsLocation = false,
                        activeCoordinates = weather.coordinates
                    )
                    _currentTab.value = WeatherTab.DASHBOARD
                },
                onFailure = { throwable ->
                    val domainException = (throwable as? WeatherException)
                        ?: WeatherException.GenericError(throwable.message ?: "Błąd wyszukiwania miasta", throwable)
                    _uiState.value = WeatherUiState.Error(
                        exception = domainException,
                        fallbackCoordinates = lastCoordinates,
                        cachedWeather = lastSuccessfulWeather
                    )
                }
            )
        }
    }

    /**
     * Odświeża aktualnie wyświetlane dane pogodowe.
     */
    fun refresh() {
        val currentState = _uiState.value
        val coords = when (currentState) {
            is WeatherUiState.Success -> currentState.activeCoordinates
            is WeatherUiState.Error -> currentState.fallbackCoordinates ?: lastCoordinates
            else -> lastCoordinates
        }
        val isGps = (currentState as? WeatherUiState.Success)?.isGpsLocation ?: false
        loadForCoordinates(coords, isGps = isGps)
    }

    /**
     * Reaguje na przyznanie uprawnień do lokalizacji przez użytkownika.
     */
    fun onLocationPermissionGranted() {
        loadForUserLocation()
    }

    /**
     * Reaguje na odmowę przyznania uprawnień lokalizacyjnych przez użytkownika.
     * Wczytuje domyślną lokalizację (Warszawa) z informacją dla użytkownika.
     */
    fun onLocationPermissionDenied() {
        loadForCoordinates(Coordinates.WARSAW, isGps = false)
    }

    /**
     * Aktualizuje klucz API w [WeatherServiceLocator] i ponownie ładuje pogodę.
     *
     * @param newApiKey Nowy klucz OpenWeather API.
     */
    fun updateApiKey(newApiKey: String) {
        val cleanKey = newApiKey.trim()
        WeatherServiceLocator.setCustomApiKey(cleanKey)
        refresh()
    }

    /**
     * Wewnętrzna metoda koordynująca równoległe lub sekwencyjne pobieranie aktualnej pogody oraz prognozy.
     *
     * @param coordinates Docelowe współrzędne geograficzne.
     * @param isGps Flaga oznaczająca czy lokalizacja pochodzi z sensora GPS.
     */
    private suspend fun fetchWeatherData(coordinates: Coordinates, isGps: Boolean) {
        val weatherResult = getCurrentWeatherUseCase(coordinates)

        weatherResult.fold(
            onSuccess = { weather ->
                lastSuccessfulWeather = weather
                lastCoordinates = coordinates

                // Pobranie prognozy
                val forecastResult = getForecastUseCase(coordinates)
                val forecast = forecastResult.getOrElse {
                    lastSuccessfulForecast ?: WeatherForecast(
                        locationName = weather.locationName,
                        countryCode = weather.countryCode,
                        coordinates = coordinates,
                        items = emptyList()
                    )
                }
                lastSuccessfulForecast = forecast

                _uiState.value = WeatherUiState.Success(
                    weather = weather,
                    forecast = forecast,
                    isGpsLocation = isGps,
                    activeCoordinates = coordinates
                )
            },
            onFailure = { throwable ->
                val domainException = (throwable as? WeatherException)
                    ?: WeatherException.GenericError(throwable.message ?: "Błąd pobierania pogody", throwable)
                _uiState.value = WeatherUiState.Error(
                    exception = domainException,
                    fallbackCoordinates = coordinates,
                    cachedWeather = lastSuccessfulWeather
                )
            }
        )
    }
}
