package pl.j.reinmar.modules.weather.view

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pl.j.reinmar.modules.weather.model.Coordinates
import pl.j.reinmar.modules.weather.model.WeatherCondition
import pl.j.reinmar.modules.weather.model.WeatherData
import pl.j.reinmar.modules.weather.model.WeatherException
import pl.j.reinmar.modules.weather.model.WeatherForecast
import pl.j.reinmar.modules.weather.repository.ILocationProvider
import pl.j.reinmar.modules.weather.repository.IWeatherRepository
import pl.j.reinmar.modules.weather.service.GetCurrentWeatherUseCase
import pl.j.reinmar.modules.weather.service.GetForecastUseCase
import pl.j.reinmar.modules.weather.service.GetUserLocationUseCase
import pl.j.reinmar.modules.weather.service.SearchCityWeatherUseCase

/**
 * Zestaw testów jednostkowych dla [WeatherViewModel].
 *
 * Weryfikuje przepływy stanów (Loading, Success, Error, PermissionRequired),
 * obsługę uprawnień GPS oraz nawigację między zakładkami.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class WeatherViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var fakeRepository: FakeWeatherRepository
    private lateinit var fakeLocationProvider: FakeLocationProvider

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeWeatherRepository()
        fakeLocationProvider = FakeLocationProvider()
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /**
     * Weryfikuje, czy brak uprawnień lokalizacyjnych powoduje przejście do stanu PermissionRequired.
     */
    @Test
    fun `brak uprawnien lokalizacji przy starcie powoduje stan PermissionRequired`() = runTest(testDispatcher) {
        fakeLocationProvider.hasPermission = false

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value is WeatherUiState.PermissionRequired)
    }

    /**
     * Weryfikuje, czy przyznanie uprawnień skutkuje pobraniem danych i stanem Success.
     */
    @Test
    fun `przyznanie uprawnien lokalizacji skutkuje pobraniem pogody i stanem Success`() = runTest(testDispatcher) {
        fakeLocationProvider.hasPermission = true
        fakeLocationProvider.mockCoordinates = Coordinates.WARSAW

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
        val successState = state as WeatherUiState.Success
        assertEquals("Warszawa", successState.weather.locationName)
        assertTrue(successState.isGpsLocation)
    }

    /**
     * Weryfikuje, czy przełączanie zakładek aktualizuje strumień currentTab.
     */
    @Test
    fun `selectTab poprawnie zmienia biezaca zakladke`() = runTest(testDispatcher) {
        fakeLocationProvider.hasPermission = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectTab(WeatherTab.MAP)
        assertEquals(WeatherTab.MAP, viewModel.currentTab.value)

        viewModel.selectTab(WeatherTab.SETTINGS)
        assertEquals(WeatherTab.SETTINGS, viewModel.currentTab.value)
    }

    /**
     * Weryfikuje, czy błąd sieci w repozytorium powoduje przejście do stanu Error.
     */
    @Test
    fun `blad sieci w repozytorium skutkuje stanem Error z odpowiednim wyjatkiem`() = runTest(testDispatcher) {
        fakeLocationProvider.hasPermission = true
        fakeRepository.returnError = WeatherException.NetworkUnavailable()

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Error)
        val errorState = state as WeatherUiState.Error
        assertTrue(errorState.exception is WeatherException.NetworkUnavailable)
    }

    /**
     * Weryfikuje, czy wyszukiwanie miasta ładuje nowe dane i przełącza zakładkę na DASHBOARD.
     */
    @Test
    fun `searchCity poprawnie wyszukuje miasto i przelacza zakladke na DASHBOARD`() = runTest(testDispatcher) {
        fakeLocationProvider.hasPermission = true
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.selectTab(WeatherTab.SETTINGS)
        viewModel.searchCity("Kraków")
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state is WeatherUiState.Success)
        assertEquals(WeatherTab.DASHBOARD, viewModel.currentTab.value)
    }

    private fun createViewModel(): WeatherViewModel {
        return WeatherViewModel(
            getCurrentWeatherUseCase = GetCurrentWeatherUseCase(fakeRepository),
            getForecastUseCase = GetForecastUseCase(fakeRepository),
            getUserLocationUseCase = GetUserLocationUseCase(fakeLocationProvider),
            searchCityWeatherUseCase = SearchCityWeatherUseCase(fakeRepository)
        )
    }

    /**
     * Atrapa repozytorium do testowania ViewModelu.
     */
    private class FakeWeatherRepository : IWeatherRepository {
        var returnError: WeatherException? = null

        override suspend fun getCurrentWeather(coordinates: Coordinates): Result<WeatherData> {
            returnError?.let { return Result.failure(it) }
            return Result.success(
                WeatherData(
                    locationName = "Warszawa",
                    countryCode = "PL",
                    coordinates = coordinates,
                    temperatureCelsius = 20.0,
                    feelsLikeCelsius = 19.0,
                    tempMinCelsius = 15.0,
                    tempMaxCelsius = 22.0,
                    condition = WeatherCondition.CLEAR_SKY,
                    description = "Bezchmurnie",
                    humidityPercent = 55,
                    pressureHpa = 1013,
                    windSpeedMps = 4.0,
                    windDirectionDegrees = 180,
                    cloudinessPercent = 0,
                    timestampSeconds = 1700000000L,
                    iconCode = "01d"
                )
            )
        }

        override suspend fun getForecast(coordinates: Coordinates): Result<WeatherForecast> {
            returnError?.let { return Result.failure(it) }
            return Result.success(
                WeatherForecast(
                    locationName = "Warszawa",
                    countryCode = "PL",
                    coordinates = coordinates,
                    items = emptyList()
                )
            )
        }

        override suspend fun searchWeatherByCity(cityName: String): Result<WeatherData> {
            returnError?.let { return Result.failure(it) }
            return Result.success(
                WeatherData(
                    locationName = cityName,
                    countryCode = "PL",
                    coordinates = Coordinates(50.0647, 19.9450),
                    temperatureCelsius = 18.0,
                    feelsLikeCelsius = 17.0,
                    tempMinCelsius = 14.0,
                    tempMaxCelsius = 20.0,
                    condition = WeatherCondition.FEW_CLOUDS,
                    description = "Małe zachmurzenie",
                    humidityPercent = 60,
                    pressureHpa = 1015,
                    windSpeedMps = 3.0,
                    windDirectionDegrees = 90,
                    cloudinessPercent = 20,
                    timestampSeconds = 1700000000L,
                    iconCode = "02d"
                )
            )
        }
    }

    /**
     * Atrapa dostawcy lokalizacji do testowania ViewModelu.
     */
    private class FakeLocationProvider : ILocationProvider {
        var hasPermission: Boolean = true
        var mockCoordinates: Coordinates = Coordinates.WARSAW

        override fun hasLocationPermission(): Boolean = hasPermission

        override suspend fun getCurrentCoordinates(): Result<Coordinates> {
            return if (hasPermission) Result.success(mockCoordinates)
            else Result.failure(WeatherException.LocationPermissionDenied())
        }

        override suspend fun getLastKnownCoordinatesOrDefault(): Coordinates = mockCoordinates
    }
}
