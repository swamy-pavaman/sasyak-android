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
                location = location,
                temperature = 22.0 + Random.nextDouble(-5.0, 5.0),
                feelsLike = 20.0 + Random.nextDouble(-3.0, 3.0),
                humidity = Random.nextInt(40, 80),
                windSpeed = 5.0 + Random.nextDouble(0.0, 10.0),
                description = getRandomWeatherDescription(),
                icon = "01d", // Sunny icon code
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