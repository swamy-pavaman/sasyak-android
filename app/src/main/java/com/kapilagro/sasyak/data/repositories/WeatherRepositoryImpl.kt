package com.kapilagro.sasyak.data.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.WeatherInfo
import com.kapilagro.sasyak.domain.repositories.WeatherRepository
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
            val weatherInfo = WeatherInfo(
                location = "Pithapuram, Andhra Pradesh",
                temperature = 32.0,       // Approximate daytime temperature (real-time varies)
                feelsLike = 35.0,         // Feels hotter due to humidity
                humidity = 68,            // Coastal Andhra has high humidity
                windSpeed = 14.0,         // Light to moderate winds
                description = "Partly Cloudy",  // Typical description for Pithapuram's April weather
                icon = "02d",             // 02d means 'few clouds' icon
                formattedDate = "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}"
            )
            ApiResponse.Success(weatherInfo)
        } catch (e: Exception) {
            ApiResponse.Error("Failed to fetch weather data")
        }
    }

    private fun getRandomWeatherDescription(): String {
        val descriptions = listOf(
            "Clear Sky",
            "Mostly Sunny",
            "Partly Cloudy",
            "Scattered Clouds",
            "Light Rain",
            "Moderate Rain"
        )
        return descriptions.random()
    }
}