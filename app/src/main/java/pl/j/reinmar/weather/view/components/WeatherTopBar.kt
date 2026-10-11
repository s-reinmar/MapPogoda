package pl.j.reinmar.modules.weather.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pl.j.reinmar.modules.weather.model.Coordinates

/**
 * Pasek górny aplikacji (TopAppBar) prezentujący bieżącą lokalizację, współrzędne oraz akcje odświeżania.
 *
 * @param locationTitle Nazwa aktualnej lokalizacji (np. "Warszawa, PL").
 * @param coordinates Współrzędne geograficzne punktu.
 * @param isGps Czy dane pochodzą bezpośrednio z geolokalizacji GPS.
 * @param onRefreshClick Akcja wywoływana po kliknięciu ikony odświeżenia.
 * @param onGpsClick Akcja wywoływana w celu powrotu do lokalizacji GPS urządzenia.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherTopBar(
    locationTitle: String,
    coordinates: Coordinates?,
    isGps: Boolean,
    onRefreshClick: () -> Unit,
    onGpsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.testTag("weather_top_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (isGps) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                        contentDescription = if (isGps) "Lokalizacja GPS" else "Lokalizacja wybrana",
                        tint = if (isGps) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = locationTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (coordinates != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = coordinates.toReadableString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isGps) MaterialTheme.colorScheme.primaryContainer
                                    else MaterialTheme.colorScheme.secondaryContainer
                                )
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isGps) "GPS" else "MAPA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isGps) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }
        },
        actions = {
            IconButton(
                onClick = onGpsClick,
                modifier = Modifier.testTag("btn_gps_locate")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Pobierz pozycję GPS",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(
                onClick = onRefreshClick,
                modifier = Modifier.testTag("btn_refresh_weather")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Odśwież dane pogodowe",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}
