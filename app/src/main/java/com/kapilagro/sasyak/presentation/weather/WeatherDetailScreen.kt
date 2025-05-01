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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapilagro.sasyak.domain.models.DailyForecast
import com.kapilagro.sasyak.domain.models.WeatherInfo
import com.kapilagro.sasyak.presentation.common.theme.AgroPrimary
import com.kapilagro.sasyak.presentation.common.theme.AgroLight


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
                        text = "Current Conditions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

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
                text = "Extended Forecast",
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
    forecast: DailyForecast
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = forecast.dayOfWeek,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = forecast.date,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = getWeatherEmoji(forecast.description),
                fontSize = 24.sp
            )

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${forecast.tempMax.toInt()}°/${forecast.tempMin.toInt()}°",
                    style = MaterialTheme.typography.bodyLarge
                )
                if (forecast.precipitationProbability > 0) {
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
                            text = "${forecast.precipitationProbability}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Blue
                        )
                    }
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
