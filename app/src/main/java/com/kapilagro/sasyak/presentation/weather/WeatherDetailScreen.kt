package com.kapilagro.sasyak.presentation.weather

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.kapilagro.sasyak.data.api.models.responses.openweather.ForecastItem
import com.kapilagro.sasyak.domain.models.WeatherInfo
import com.kapilagro.sasyak.presentation.common.theme.AgroLight
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDetailScreen(
    weatherInfo: WeatherInfo,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Weather Details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBackIosNew,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AgroPrimary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Current conditions card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = AgroLight)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Current Conditions in ${weatherInfo.location}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = weatherInfo.formattedDate,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DetailMetricItem(
                            icon = Icons.Outlined.Thermostat,
                            title = "Temperature",
                            value = "${weatherInfo.temperature.toInt()}°C"
                        )
                        DetailMetricItem(
                            icon = Icons.Outlined.WaterDrop,
                            title = "Humidity",
                            value = "${weatherInfo.humidity}%"
                        )
                        DetailMetricItem(
                            icon = Icons.Outlined.Air,
                            title = "Wind Speed",
                            value = "${weatherInfo.windSpeed} km/h"
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        DetailMetricItem(
                            icon = Icons.Outlined.Thermostat,
                            title = "Feels Like",
                            value = "${weatherInfo.feelsLike.toInt()}°C"
                        )
                        DetailMetricItem(
                            icon = Icons.Outlined.Speed,
                            title = "Pressure",
                            value = "${weatherInfo.pressureHPa} hPa"
                        )
                        DetailMetricItem(
                            icon = Icons.Outlined.WbSunny,
                            title = "UV Index",
                            value = "${weatherInfo.uvIndex}"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Extended forecast
            Text(
                text = "7-Day Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            LazyColumn {
                items(weatherInfo.forecast) { forecast ->
                    ExtendedForecastItem(forecast = forecast)
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
fun DetailMetricItem(
    icon: ImageVector,
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = AgroPrimary,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ExtendedForecastItem(
    forecast: ForecastItem
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            ForecastDayLabel(forecast.date)
            Text(
                text = formatReadableDate(forecast.date),
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
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

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${forecast.maxTempC.toInt()}°/${forecast.minTempC.toInt()}°",
                    style = MaterialTheme.typography.bodyLarge
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = "Rain",
                        tint = Color.Blue,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${forecast.chanceOfRain}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Blue
                    )
                }

            }
        }
    }
}

@Composable
fun ForecastDayLabel(dateString: String) {
    val label = remember(dateString) { getDayLabelFromDateString(dateString) }

    Text(
        text = label,
        style = MaterialTheme.typography.bodyLarge,
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

fun formatReadableDate(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val date = inputFormat.parse(dateString)

        val outputFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        outputFormat.format(date ?: return dateString)
    } catch (e: Exception) {
        dateString // fallback to raw string on error
    }
}

