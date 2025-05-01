package com.kapilagro.sasyak.data.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.DailyForecast
import com.kapilagro.sasyak.domain.models.WeatherInfo
import com.kapilagro.sasyak.domain.repositories.WeatherRepository
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class WeatherRepositoryImpl @Inject constructor() : WeatherRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getWeatherForLocation(location: String): ApiResponse<WeatherInfo> {
        // Simulate network delay
        delay(800)

        // Mock weather data
        return try {
            // Generate 7-day forecast
            val forecast = generateForecast()

            val weatherInfo = WeatherInfo(
                location = "Pithapuram, Andhra Pradesh",
                temperature = 32.0,
                feelsLike = 35.0,
                humidity = 68,
                windSpeed = 14.0,
                description = "Partly Cloudy",
                icon = "02d",
                formattedDate = "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
                tempMin = 28.0,
                tempMax = 36.0,
                precipitationProbability = 30,
                uvIndex = 7,
                pressureHPa = 1012,
                cloudCoverPercentage = 45,
                forecast = forecast
            )
            ApiResponse.Success(weatherInfo)
        } catch (e: Exception) {
            ApiResponse.Error("Failed to fetch weather data")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateForecast(): List<DailyForecast> {
        val forecast = mutableListOf<DailyForecast>()
        val today = LocalDate.now()

        for (i in 0 until 7) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = if (i == 0) "Today" else if (i == 1) "Tomorrow" else
                date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            forecast.add(
                DailyForecast(
                    date = date.format(DateTimeFormatter.ofPattern("MMM d")),
                    dayOfWeek = dayOfWeek,
                    tempMin = 27.0 + Random.nextInt(-2, 3),
                    tempMax = 35.0 + Random.nextInt(-3, 3),
                    precipitationProbability = Random.nextInt(0, 70),
                    description = getRandomWeatherDescription(),
                    icon = "02d",
                    windSpeed = 12.0 + Random.nextInt(-3, 5),
                    humidity = 60 + Random.nextInt(-10, 15)
                )
            )
        }

        return forecast
    }

    private fun getRandomWeatherDescription(): String {
        val descriptions = listOf(
            "Clear Sky",
            "Mostly Sunny",
            "Partly Cloudy",
            "Scattered Clouds",
            "Light Rain",
            "Moderate Rain",
            "Cloudy"
        )
        return descriptions.random()
    }
}
