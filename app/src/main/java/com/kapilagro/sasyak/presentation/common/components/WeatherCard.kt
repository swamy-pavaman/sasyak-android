package com.kapilagro.sasyak.presentation.common.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kapilagro.sasyak.data.api.models.responses.openweather.ForecastItem
import com.kapilagro.sasyak.domain.models.WeatherInfo
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import com.kapilagro.sasyak.presentation.common.theme.AgroLight
import java.text.SimpleDateFormat
import java.util.*

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
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = AgroLight)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
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
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
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
                            modifier = Modifier.weight(0.4f),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = "${weatherInfo.temperature.toInt()}°C",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Min: ${weatherInfo.tempMin.toInt()}°",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                            Text(
                                text = "Max: ${weatherInfo.tempMax.toInt()}°",
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
                        title = "Rain",
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
    forecast : ForecastItem
) {
    Card(
        modifier = Modifier
            .widthIn(min = 90.dp, max = 100.dp)
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

            ForecastDayLabel(dateString = forecast.date)

            Box {
                AsyncImage(
                    model = "https:${forecast.iconUrl}",
                    contentDescription = "forecast icon",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(6.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Text(
                text = "${forecast.maxTempC.toInt()}°/${forecast.minTempC.toInt()}°",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )

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
                    text = "${forecast.chanceOfRain}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Blue,
                    modifier = Modifier.padding(start = 2.dp)
                )
            }

        }
    }
}

@Composable
fun ForecastDayLabel(dateString: String) {
    val label = remember(dateString) { getDayLabelFromDateString(dateString) }

    Text(
        text = label,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = FontWeight.Medium
    )
}

fun getDayLabelFromDateString(dateString: String): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val inputDate: Date? = try {
        sdf.parse(dateString)
    } catch (e: Exception) {
        null
    }

    if (inputDate == null) return ""

    val todayCal = Calendar.getInstance()
    val inputCal = Calendar.getInstance().apply { time = inputDate }

    val todayYear = todayCal.get(Calendar.YEAR)
    val inputYear = inputCal.get(Calendar.YEAR)
    val todayDay = todayCal.get(Calendar.DAY_OF_YEAR)
    val inputDay = inputCal.get(Calendar.DAY_OF_YEAR)

    return when {
        todayYear == inputYear && todayDay == inputDay -> "Today"
        todayYear == inputYear && inputDay == todayDay + 1 -> "Tomorrow"
        else -> {
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // e.g., "Fri"
            dayFormat.format(inputDate)
        }
    }
}