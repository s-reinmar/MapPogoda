package pl.j.reinmar.modules.weather.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import pl.j.reinmar.modules.weather.model.WeatherException
import pl.j.reinmar.modules.weather.service.WeatherServiceLocator
import pl.j.reinmar.modules.weather.view.components.ApiKeyDialog
import pl.j.reinmar.modules.weather.view.components.CurrentWeatherCard
import pl.j.reinmar.modules.weather.view.components.DailyForecastList
import pl.j.reinmar.modules.weather.view.components.HourlyForecastRow
import pl.j.reinmar.modules.weather.view.components.WeatherMetricsGrid
import pl.j.reinmar.modules.weather.view.components.WeatherTopBar

/**
 * Główny ekran aplikacji MapPogoda spinający wszystkie podwidoki i stan z [WeatherViewModel].
 *
 * @param viewModel Instancja [WeatherViewModel].
 * @param onRequestLocationPermission Wywołanie systemowego żądania uprawnień lokalizacyjnych.
 */
@Composable
fun WeatherMainScreen(
    viewModel: WeatherViewModel,
    onRequestLocationPermission: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val mapCoordinates by viewModel.mapCoordinates.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }

    val activeLocationTitle = when (val state = uiState) {
        is WeatherUiState.Success -> state.weather.displayLocation()
        is WeatherUiState.Error -> state.cachedWeather?.displayLocation() ?: stringResource(R.string.app_name)
        else -> stringResource(R.string.app_name)
    }

    val activeCoordinates = when (val state = uiState) {
        is WeatherUiState.Success -> state.activeCoordinates
        is WeatherUiState.Error -> state.fallbackCoordinates
        else -> null
    }

    val isGps = (uiState as? WeatherUiState.Success)?.isGpsLocation ?: false

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentTab != WeatherTab.MAP) {
                WeatherTopBar(
                    locationTitle = activeLocationTitle,
                    coordinates = activeCoordinates,
                    isGps = isGps,
                    onRefreshClick = { viewModel.refresh() },
                    onGpsClick = {
                        viewModel.loadForUserLocation()
                        viewModel.selectTab(WeatherTab.DASHBOARD)
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar"),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                NavigationBarItem(
                    selected = currentTab == WeatherTab.DASHBOARD,
                    onClick = { viewModel.selectTab(WeatherTab.DASHBOARD) },
                    icon = { Icon(Icons.Default.WbSunny, contentDescription = "Pulpit pogodowy") },
                    label = { Text("Pogoda") },
                    modifier = Modifier.testTag("nav_item_dashboard")
                )
                NavigationBarItem(
                    selected = currentTab == WeatherTab.MAP,
                    onClick = { viewModel.selectTab(WeatherTab.MAP) },
                    icon = { Icon(Icons.Default.Map, contentDescription = "Interaktywna mapa") },
                    label = { Text("Mapa GPS") },
                    modifier = Modifier.testTag("nav_item_map")
                )
                NavigationBarItem(
                    selected = currentTab == WeatherTab.SETTINGS,
                    onClick = { viewModel.selectTab(WeatherTab.SETTINGS) },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Wyszukiwanie i ustawienia") },
                    label = { Text("Szukaj") },
                    modifier = Modifier.testTag("nav_item_settings")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                WeatherTab.DASHBOARD -> {
                    DashboardContent(
                        uiState = uiState,
                        onRefresh = { viewModel.refresh() },
                        onRequestPermission = onRequestLocationPermission,
                        onConfigureApiKey = { showApiKeyDialog = true }
                    )
                }

                WeatherTab.MAP -> {
                    WeatherMapScreen(
                        selectedCoordinates = mapCoordinates,
                        onLocationSelected = { coords ->
                            viewModel.loadForCoordinates(coords, isGps = false)
                            viewModel.selectTab(WeatherTab.DASHBOARD)
                        },
                        onGpsLocateClick = {
                            viewModel.loadForUserLocation()
                        }
                    )
                }

                WeatherTab.SETTINGS -> {
                    SearchAndSettingsContent(
                        searchQuery = searchQuery,
                        onQueryChanged = { viewModel.onSearchQueryChanged(it) },
                        onSearch = { viewModel.searchCity(it) },
                        onConfigureApiKey = { showApiKeyDialog = true }
                    )
                }
            }
        }
    }

    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = WeatherServiceLocator.getApiKey(),
            onDismiss = { showApiKeyDialog = false },
            onKeySaved = { newKey ->
                viewModel.updateApiKey(newKey)
                showApiKeyDialog = false
            }
        )
    }
}

/**
 * Treść głównego pulpitu pogodowego obsługująca stany Loading, Success, Error i PermissionRequired.
 */
@Composable
private fun DashboardContent(
    uiState: WeatherUiState,
    onRefresh: () -> Unit,
    onRequestPermission: () -> Unit,
    onConfigureApiKey: () -> Unit
) {
    when (uiState) {
        is WeatherUiState.Loading -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("state_loading"),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = uiState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        is WeatherUiState.PermissionRequired -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .testTag("state_permission_required"),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Dostęp do lokalizacji GPS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aby wyświetlić prognozę dla Twojej obecnej lokalizacji, aplikacja potrzebuje dostępu do GPS urządzenia.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onRequestPermission,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("btn_grant_location_permission"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Włącz lokalizację GPS")
                        }
                    }
                }
            }
        }

        is WeatherUiState.Error -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .testTag("state_error"),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Problem z pobraniem pogody",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = uiState.exception.message ?: "Wystąpił nieoczekiwany błąd.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.9f),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = onRefresh,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("btn_error_retry"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Ponów")
                                }

                                if (uiState.exception is WeatherException.InvalidApiKey) {
                                    OutlinedButton(
                                        onClick = onConfigureApiKey,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("btn_error_configure_key"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Klucz API")
                                    }
                                }
                            }
                        }
                    }
                }

                // Jeżeli są dostępne dane z pamięci podręcznej (offline cache)
                if (uiState.cachedWeather != null) {
                    item {
                        Text(
                            text = "Ostatnio zapisane dane offline:",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    item {
                        CurrentWeatherCard(weather = uiState.cachedWeather)
                    }
                }
            }
        }

        is WeatherUiState.Success -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("state_success"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    CurrentWeatherCard(weather = uiState.weather)
                }

                item {
                    HourlyForecastRow(items = uiState.forecast.getHourly(8))
                }

                item {
                    DailyForecastList(summaries = uiState.forecast.getDailySummaries())
                }

                item {
                    WeatherMetricsGrid(weather = uiState.weather)
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * Widok wyszukiwarki miejscowości oraz ustawień klucza API.
 */
@Composable
private fun SearchAndSettingsContent(
    searchQuery: String,
    onQueryChanged: (String) -> Unit,
    onSearch: (String) -> Unit,
    onConfigureApiKey: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("search_settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Wyszukaj miejscowość",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onQueryChanged,
                    label = { Text("Wpisz miasto (np. Kraków)") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_search_city")
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onSearch(searchQuery) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("btn_submit_search")
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Szukaj")
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Szybki wybór popularnych miast:",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    listOf("Warszawa", "Kraków", "Gdańsk", "Wrocław", "Poznań", "Zakopane").chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { city ->
                                OutlinedButton(
                                    onClick = { onSearch(city) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(city, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Ustawienia i integracje",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Klucz API OpenWeather",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Aplikacja pobiera dane meteorologiczne z serwerów OpenWeatherMap. Możesz w każdej chwili podać własny darmowy klucz.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onConfigureApiKey,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_settings_change_key")
                    ) {
                        Text("Zmień klucz API")
                    }
                }
            }
        }
    }
}
