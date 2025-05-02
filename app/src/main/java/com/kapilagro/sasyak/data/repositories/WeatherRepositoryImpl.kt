package com.kapilagro.sasyak.data.repositories

//import android.os.Build
//import android.util.Log
//import androidx.annotation.RequiresApi
//import com.kapilagro.sasyak.data.api.OpenWeatherApiService
//import com.kapilagro.sasyak.di.NetworkModule
//import com.kapilagro.sasyak.domain.models.ApiResponse
//import com.kapilagro.sasyak.domain.models.DailyForecast
//import com.kapilagro.sasyak.domain.models.WeatherInfo
//import com.kapilagro.sasyak.domain.repositories.WeatherRepository
//import com.kapilagro.sasyak.utils.LocationService
//import java.time.Instant
//import java.time.LocalDate
//import java.time.ZoneId
//import java.time.format.DateTimeFormatter
//import java.time.format.TextStyle
//import java.util.*
//import javax.inject.Inject
//import javax.inject.Singleton
//
//@Singleton
//class WeatherRepositoryImpl @Inject constructor(
//    private val openWeatherApiService: OpenWeatherApiService,
//    private val locationService: LocationService
//) : WeatherRepository {
//
//    @RequiresApi(Build.VERSION_CODES.O)
//    override suspend fun getWeatherForLocation(location: String): ApiResponse<WeatherInfo> {
//        Log.d("WeatherRepository", "Starting to fetch weather data...")
//
//        return try {
//            // Get current location
//            val locationResult = locationService.getLocation()
//
//            if (locationResult.isFailure) {
//                val error = locationResult.exceptionOrNull()?.message ?: "Unknown location error"
//                Log.e("WeatherRepository", "Location error: $error")
//                return ApiResponse.Error(error)
//            }
//
//            val location = locationResult.getOrNull()
//            if (location == null) {
//                Log.e("WeatherRepository", "Location is null")
//                return ApiResponse.Error("Unable to get location")
//            }
//
//            Log.d("WeatherRepository", "Location obtained: ${location.latitude}, ${location.longitude}")
//
//            // Get weather data
//            Log.d("WeatherRepository", "Calling OpenWeather API...")
//            val weatherResponse = openWeatherApiService.getWeatherOneCall(
//                latitude = location.latitude,
//                longitude = location.longitude,
//                apiKey = NetworkModule.OPENWEATHER_API_KEY
//            )
//
//            Log.d("WeatherRepository", "Weather data received successfully")
//
//            // Get location name from reverse geocoding
//            val geocodeResponse = try {
//                openWeatherApiService.getReverseGeocode(
//                    latitude = location.latitude,
//                    longitude = location.longitude,
//                    apiKey = NetworkModule.OPENWEATHER_API_KEY
//                )
//            } catch (e: Exception) {
//                Log.e("WeatherRepository", "Geocoding error: ${e.message}")
//                emptyList()
//            }
//
//            val locationName = if (geocodeResponse.isNotEmpty()) {
//                val loc = geocodeResponse[0]
//                "${loc.name}, ${loc.state ?: loc.country}"
//            } else {
//                "Lat: ${location.latitude}, Lon: ${location.longitude}"
//            }
//
//            // Convert to domain model
//            val forecast = weatherResponse.daily.take(7).mapIndexed { index, daily ->
//                val date = Instant.ofEpochSecond(daily.dateTime).atZone(ZoneId.systemDefault()).toLocalDate()
//                val dayOfWeek = when (index) {
//                    0 -> "Today"
//                    1 -> "Tomorrow"
//                    else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
//                }
//
//                DailyForecast(
//                    date = date.format(DateTimeFormatter.ofPattern("MMM d")),
//                    dayOfWeek = dayOfWeek,
//                    tempMin = daily.temperature.min,
//                    tempMax = daily.temperature.max,
//                    precipitationProbability = (daily.precipitationProbability * 100).toInt(),
//                    description = daily.weather.firstOrNull()?.description?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
//                        Locale.ROOT
//                    ) else it.toString() }
//                        ?: "Unknown",
//                    icon = daily.weather.firstOrNull()?.icon ?: "01d",
//                    windSpeed = daily.windSpeed,
//                    humidity = daily.humidity
//                )
//            }
//
//            val current = weatherResponse.current
//            val weatherInfo = WeatherInfo(
//                location = locationName,
//                temperature = current.temperature,
//                feelsLike = current.feelsLike,
//                humidity = current.humidity,
//                windSpeed = current.windSpeed,
//                description = current.weather.firstOrNull()?.description?.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
//                    Locale.ROOT
//                ) else it.toString() }
//                    ?: "Unknown",
//                icon = current.weather.firstOrNull()?.icon ?: "01d",
//                formattedDate = "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
//                tempMin = weatherResponse.daily.firstOrNull()?.temperature?.min ?: current.temperature,
//                tempMax = weatherResponse.daily.firstOrNull()?.temperature?.max ?: current.temperature,
//                precipitationProbability = (weatherResponse.daily.firstOrNull()?.precipitationProbability?.times(100))?.toInt() ?: 0,
//                uvIndex = current.uvIndex.toInt(),
//                pressureHPa = current.pressure,
//                cloudCoverPercentage = current.cloudCover,
//                forecast = forecast
//            )
//
//            Log.d("WeatherRepository", "Weather data processed successfully")
//            ApiResponse.Success(weatherInfo)
//        } catch (e: Exception) {
//            Log.e("WeatherRepository", "Error fetching weather data", e)
//            e.printStackTrace()
//            ApiResponse.Error("Failed to fetch weather data: ${e.message}")
//        }
//    }
//}


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