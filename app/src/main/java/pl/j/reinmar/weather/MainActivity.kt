package pl.j.reinmar.weather

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.j.reinmar.modules.weather.ui.theme.MyApplicationTheme
import pl.j.reinmar.modules.weather.service.WeatherServiceLocator
import pl.j.reinmar.modules.weather.view.WeatherMainScreen
import pl.j.reinmar.modules.weather.view.WeatherViewModel

/**
 * Główna aktywność aplikacji MapPogoda.
 *
 * Inicjalizuje motyw Material 3, obsługuje tryb Edge-to-Edge,
 * zarządza uprawnieniami geolokalizacji w czasie działania (Runtime Permissions dla Android 6+)
 * oraz osadza główny kontener interfejsu użytkownika [WeatherMainScreen].
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val appContext = applicationContext
                    val viewModel: WeatherViewModel = viewModel {
                        WeatherViewModel(
                            getUserLocationUseCase = WeatherServiceLocator.provideGetUserLocationUseCase(appContext)
                        )
                    }

                    // Launcher uprawnień czasu wykonania dla geolokalizacji (FINE i COARSE)
                    val permissionLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestMultiplePermissions()
                    ) { permissions ->
                        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
                        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

                        if (fineGranted || coarseGranted) {
                            viewModel.onLocationPermissionGranted()
                        } else {
                            viewModel.onLocationPermissionDenied()
                        }
                    }

                    LaunchedEffect(Unit) {
                        val hasPermission = WeatherServiceLocator.provideLocationProvider(appContext).hasLocationPermission()
                        if (!hasPermission) {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    }

                    WeatherMainScreen(
                        viewModel = viewModel,
                        onRequestLocationPermission = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}
