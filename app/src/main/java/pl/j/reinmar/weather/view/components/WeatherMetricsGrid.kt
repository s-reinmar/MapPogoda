package pl.j.reinmar.modules.weather.view.components

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pl.j.reinmar.modules.weather.model.WeatherData

/**
 * Siatka kafelków prezentująca szczegółowe parametry meteorologiczne: wiatr, wilgotność, ciśnienie i zachmurzenie.
 *
 * @param weather Dane aktualnej pogody [WeatherData].
 * @param modifier Modyfikator układu Compose.
 */
@Composable
fun WeatherMetricsGrid(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Szczegóły meteorologiczne",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Wiatr",
                value = weather.formattedWindSpeed(),
                subtitle = "Kierunek: ${weather.windDirectionCardinal()}",
                icon = Icons.Default.Air,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Wilgotność",
                value = "${weather.humidityPercent}%",
                subtitle = "Wilgotność wzgl.",
                icon = Icons.Default.WaterDrop,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                title = "Ciśnienie",
                value = "${weather.pressureHpa} hPa",
                subtitle = "Normalne",
                icon = Icons.Default.Compress,
                modifier = Modifier.weight(1f)
            )
            MetricCard(
                title = "Zachmurzenie",
                value = "${weather.cloudinessPercent}%",
                subtitle = "Pokrycie nieba",
                icon = Icons.Default.Cloud,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Pojedynczy kafelek wskaźnika pogodowego.
 *
 * @param title Nazwa parametru.
 * @param value Wartość z jednostką.
 * @param subtitle Dodatkowy opis lub status.
 * @param icon Ikona wektorowa reprezentująca wskaźnik.
 */
@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("metric_card_${title.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}
