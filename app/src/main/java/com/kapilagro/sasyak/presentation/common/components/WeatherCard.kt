package com.kapilagro.sasyak.presentation.common.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapilagro.sasyak.domain.models.DailyForecast
import com.kapilagro.sasyak.domain.models.WeatherInfo
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import com.kapilagro.sasyak.presentation.common.theme.AgroLight
import com.kapilagro.sasyak.presentation.common.theme.AgroSecondary
import com.kapilagro.sasyak.presentation.common.theme.CardBackground
import com.kapilagro.sasyak.presentation.common.theme.CardBorder

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherCard(
    weatherInfo: WeatherInfo,
    onFullDetailsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = CardBackground // Updated to new color
        ),
        border = BorderStroke(0.5.dp, CardBorder) // Updated to new border color
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            // Main weather section with green background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = AgroPrimary)
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // Location and current weather
                        Column {
                            Text(
                                text = weatherInfo.location,
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = weatherInfo.formattedDate,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = weatherInfo.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }

                        // Temperature
                        Column(
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${weatherInfo.temperature.toInt()}°C",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Min: ${weatherInfo.tempMin.toInt()}° / Max: ${weatherInfo.tempMax.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }

            // Critical information for farmers
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Primary metrics row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeatherMetricItem(
                        icon = Icons.Outlined.WaterDrop,
                        title = "Rain Chance",
                        value = "${weatherInfo.precipitationProbability}%",
                        tint = AgroPrimary
                    )
                    WeatherMetricItem(
                        icon = Icons.Outlined.Air,
                        title = "Wind",
                        value = "${weatherInfo.windSpeed.toInt()} km/h",
                        tint = AgroPrimary
                    )
                    WeatherMetricItem(
                        icon = Icons.Outlined.WaterDrop,
                        title = "Humidity",
                        value = "${weatherInfo.humidity}%",
                        tint = AgroPrimary
                    )
                }

                Divider(
                    modifier = Modifier.padding(vertical = 16.dp),
                    color = Color.Gray.copy(alpha = 0.2f)
                )

                // 7-Day forecast
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "7-Day Forecast",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )

                    TextButton(onClick = onFullDetailsClick) {
                        Text(
                            text = "Full Details",
                            color = AgroPrimary,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "View full details",
                            tint = AgroPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(weatherInfo.forecast.take(7)) { forecast ->
                        ForecastDayCard(forecast = forecast)
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherMetricItem(
    icon: ImageVector,
    title: String,
    value: String,
    tint: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun ForecastDayCard(
    forecast: DailyForecast
) {
    Card(
        modifier = Modifier
            .width(90.dp)
            .height(140.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = forecast.dayOfWeek,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = getWeatherEmoji(forecast.description),
                fontSize = 24.sp
            )

            Text(
                text = "${forecast.tempMax.toInt()}°/${forecast.tempMin.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )

            // Rain probability if significant
            if (forecast.precipitationProbability > 20) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = "Rain probability",
                        tint = Color.Blue,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${forecast.precipitationProbability}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Blue,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }
        }
    }
}

private fun getWeatherEmoji(description: String): String {
    return when {
        description.contains("rain", ignoreCase = true) -> "🌧️"
        description.contains("cloud", ignoreCase = true) -> "☁️"
        description.contains("sun", ignoreCase = true) || description.contains("clear", ignoreCase = true) -> "☀️"
        description.contains("storm", ignoreCase = true) -> "⛈️"
        description.contains("thunder", ignoreCase = true) -> "⚡"
        else -> "🌤️"
    }
}