package pl.j.reinmar.modules.weather.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pl.j.reinmar.modules.weather.model.WeatherCondition
import pl.j.reinmar.modules.weather.model.WeatherData

/**
 * Główna karta wizualna prezentująca aktualną temperaturę, odczuwalną, warunki pogodowe i ikonę.
 *
 * @param weather Obiekt z danymi aktualnej pogody [WeatherData].
 * @param modifier Modyfikator układu Compose.
 */
@Composable
fun CurrentWeatherCard(
    weather: WeatherData,
    modifier: Modifier = Modifier
) {
    val gradientColors = getConditionGradient(weather.condition)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("current_weather_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = weather.formattedTemperature(),
                            style = MaterialTheme.typography.displayLarge.copy(
                                fontSize = 56.sp,
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = weather.formattedFeelsLike(),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    // Ikona pogody (pobierana z OpenWeather z fallbackiem na wektor Material)
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .background(Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(20.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (weather.iconCode.isNotBlank()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data("https://openweathermap.org/img/wn/${weather.iconCode}@2x.png")
                                    .crossfade(true)
                                    .build(),
                                contentDescription = weather.description,
                                modifier = Modifier.size(80.dp)
                            )
                        } else {
                            Icon(
                                imageVector = getConditionVectorIcon(weather.condition),
                                contentDescription = weather.description,
                                tint = Color.White,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = weather.description,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Min: ${Math.round(weather.tempMinCelsius)}°C  •  Max: ${Math.round(weather.tempMaxCelsius)}°C",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Wilgotność",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${weather.humidityPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dobiera atrakcyjny gradient kolorystyczny na podstawie typu pogody.
 *
 * @param condition Domenowy warunek pogodowy.
 * @return Lista kolorów do wygenerowania pędzla gradientowego.
 */
private fun getConditionGradient(condition: WeatherCondition): List<Color> {
    return when (condition) {
        WeatherCondition.CLEAR_SKY -> listOf(Color(0xFFF59E0B), Color(0xFFEF4444))
        WeatherCondition.FEW_CLOUDS -> listOf(Color(0xFF38BDF8), Color(0xFF0284C7))
        WeatherCondition.SCATTERED_CLOUDS,
        WeatherCondition.BROKEN_CLOUDS -> listOf(Color(0xFF64748B), Color(0xFF334155))
        WeatherCondition.SHOWER_RAIN,
        WeatherCondition.RAIN -> listOf(Color(0xFF0284C7), Color(0xFF1E293B))
        WeatherCondition.THUNDERSTORM -> listOf(Color(0xFF4338CA), Color(0xFF0F172A))
        WeatherCondition.SNOW -> listOf(Color(0xFF38BDF8), Color(0xFF94A3B8))
        WeatherCondition.MIST -> listOf(Color(0xFF64748B), Color(0xFF475569))
        WeatherCondition.UNKNOWN -> listOf(Color(0xFF2563EB), Color(0xFF1E293B))
    }
}

/**
 * Zwraca wektorową ikonę Material jako fallback.
 *
 * @param condition Domenowy warunek pogodowy.
 * @return Odpowiedni wektor [ImageVector].
 */
private fun getConditionVectorIcon(condition: WeatherCondition): ImageVector {
    return when (condition) {
        WeatherCondition.CLEAR_SKY -> Icons.Default.WbSunny
        WeatherCondition.FEW_CLOUDS,
        WeatherCondition.SCATTERED_CLOUDS,
        WeatherCondition.BROKEN_CLOUDS -> Icons.Default.Cloud
        WeatherCondition.SHOWER_RAIN,
        WeatherCondition.RAIN -> Icons.Default.Grain
        WeatherCondition.THUNDERSTORM -> Icons.Default.Thunderstorm
        WeatherCondition.SNOW -> Icons.Default.AcUnit
        WeatherCondition.MIST -> Icons.Default.FilterDrama
        WeatherCondition.UNKNOWN -> Icons.Default.WbSunny
    }
}
