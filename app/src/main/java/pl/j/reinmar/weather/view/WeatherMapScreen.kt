package pl.j.reinmar.modules.weather.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.LocationSearching
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import kotlinx.coroutines.launch
import pl.j.reinmar.modules.weather.model.Coordinates

/**
 * Ekran interaktywnej mapy Google Maps umożliwiający eksplorację pogodową dowolnego punktu na Ziemi.
 *
 * Użytkownik może kliknąć w dowolne miejsce na mapie, aby umieścić znacznik (Marker)
 * oraz załadować szczegółową prognozę pogody dla wybranego rejonu.
 *
 * @param selectedCoordinates Aktualnie wybrane współrzędne geograficzne.
 * @param onLocationSelected Akcja wywoływana po zatwierdzeniu wyboru współrzędnych.
 * @param onGpsLocateClick Akcja wywoływana w celu centrowania na bieżącej pozycji GPS.
 * @param modifier Modyfikator układu Compose.
 */
@Composable
fun WeatherMapScreen(
    selectedCoordinates: Coordinates,
    onLocationSelected: (Coordinates) -> Unit,
    onGpsLocateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentPoint by remember(selectedCoordinates) {
        mutableStateOf(LatLng(selectedCoordinates.latitude, selectedCoordinates.longitude))
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(currentPoint, 10f)
    }

    val markerState = rememberMarkerState(position = currentPoint)

    LaunchedEffect(selectedCoordinates) {
        val newLatLng = LatLng(selectedCoordinates.latitude, selectedCoordinates.longitude)
        currentPoint = newLatLng
        markerState.position = newLatLng
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(newLatLng, 10f))
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        )
    }

    val mapProperties = remember {
        MapProperties(
            isMyLocationEnabled = false
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("weather_map_screen")
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = uiSettings,
            properties = mapProperties,
            onMapClick = { latLng ->
                currentPoint = latLng
                markerState.position = latLng
                coroutineScope.launch {
                    cameraPositionState.animate(CameraUpdateFactory.newLatLng(latLng))
                }
            }
        ) {
            Marker(
                state = markerState,
                title = "Wybrana lokalizacja",
                snippet = "Współrzędne: ${String.format(java.util.Locale.US, "%.3f, %.3f", currentPoint.latitude, currentPoint.longitude)}"
            )
        }

        // Górny pasek informacyjny z instrukcją
        Card(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Kliknij dowolny punkt na mapie, aby sprawdzić pogodę",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // Przycisk pływający FAB: Centruj na GPS
        FloatingActionButton(
            onClick = {
                onGpsLocateClick()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 128.dp, end = 16.dp)
                .testTag("fab_map_gps"),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ) {
            Icon(
                imageVector = Icons.Default.MyLocation,
                contentDescription = "Moja lokalizacja GPS"
            )
        }

        // Dolny panel podsumowujący kliknięty punkt
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationSearching,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Wybrany punkt",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = String.format(
                                java.util.Locale.US,
                                "%.4f°N, %.4f°E",
                                currentPoint.latitude,
                                currentPoint.longitude
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val coords = Coordinates(currentPoint.latitude, currentPoint.longitude)
                        onLocationSelected(coords)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_load_weather_for_coords"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Pokaż pogodę dla tego miejsca",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
