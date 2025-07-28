package com.kapilagro.sasyak.data.repositories

import android.R.attr.description
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kapilagro.sasyak.data.api.WeatherApiService
import com.kapilagro.sasyak.data.api.models.responses.openweather.ForecastItem
import com.kapilagro.sasyak.data.db.dao.WeatherDao
import com.kapilagro.sasyak.data.db.entities.WeatherEntity
import com.kapilagro.sasyak.di.NetworkModule
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.DailyForecast
import com.kapilagro.sasyak.domain.models.WeatherInfo
import com.kapilagro.sasyak.domain.repositories.WeatherRepository
import com.kapilagro.sasyak.utils.LocationService
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherApiService: WeatherApiService,
    private val locationService: LocationService,
    private val weatherDao: WeatherDao
) : WeatherRepository {

    companion object {
        private const val TAG = "WeatherRepository"
        @Volatile
        private var hasCalledApiThisSession = false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getWeatherForLocation(location: String): ApiResponse<WeatherInfo> {
        Log.d(TAG, "Starting to fetch weather data...")

        return try {
            // Check if we have cached data and haven't called API this session
            val cachedWeather = weatherDao.getWeather()
            if (cachedWeather != null && hasCalledApiThisSession) {
                Log.d(TAG, "Using cached weather data (API already called this session)")
                return ApiResponse.Success(mapEntityToWeatherInfo(cachedWeather))
            }

            Log.d(TAG, "Fetching from API (app launch or no cached data)...")

            // Default coordinates 17.264495783553038, 78.27057143815432
            var latitude = 17.4213574
            var longitude = 78.3477303

            // Try to get actual location
            val result = locationService.getLocation()
            if (result.isSuccess) {
                val locationResult = result.getOrNull()
                if (locationResult != null) {
                    latitude = locationResult.latitude
                    longitude = locationResult.longitude
                    Log.d(TAG, "Using actual location coordinates: $latitude, $longitude")
                } else {
                    Log.w(TAG, "Location result is null, using default coordinates: $latitude, $longitude")
                }
            } else {
                Log.w(TAG, "Failed to get location: ${result.exceptionOrNull()?.message}, using default coordinates: $latitude, $longitude")
            }

            Log.d(TAG, "Final coordinates being used: $latitude, $longitude")

            // Get current weather data from OpenWeather API
            Log.d(TAG, "Calling Weather API for current weather...")
            val weatherResponse = weatherApiService.getCurrentWeather(
                location = "$latitude,$longitude",
                apiKey = NetworkModule.OPENWEATHER_API_KEY
            )
            Log.d(TAG, "Current weather data received successfully")

            val current = weatherResponse.current
            val location = weatherResponse.location
            val forecastDays = weatherResponse.forecast?.forecastDay.orEmpty()

            // Create WeatherInfo object
            val weatherInfo = WeatherInfo(
                location = "${location?.name}, ${location?.region}" ?: "Unknown",
                temperature = current?.tempC ?: 0.0,
                feelsLike = current?.feelsLikeC ?: 0.0,
                humidity = current?.humidity ?: 0,
                windSpeed = current?.windKph ?: 0.0,
                description = current?.condition?.text?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                } ?: "Unknown",
                icon = current?.condition?.icon ?: "",
                formattedDate = "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
                tempMin = forecastDays.firstOrNull()?.day?.minTempC ?: 0.0, // Approx for min/max
                tempMax = forecastDays.firstOrNull()?.day?.maxTempC ?: 0.0,
                precipitationProbability = forecastDays.firstOrNull()?.day?.chanceOfRain ?: 0,
                uvIndex = current?.uv?.toInt() ?: 0,
                pressureHPa = current?.pressureMb?.toInt() ?: 0,
                cloudCoverPercentage = current?.cloud ?: 0,
                forecast = forecastDays.map {
                    ForecastItem(
                        date = it.date ?: "",
                        maxTempC = it.day?.maxTempC ?: 0.0,
                        minTempC = it.day?.minTempC ?: 0.0,
                        avgTempC = it.day?.avgTempC ?: 0.0,
                        chanceOfRain = it.day?.chanceOfRain ?: 0,
                        conditionText = it.day?.condition?.text ?: "N/A",
                        iconUrl = it.day?.condition?.icon ?: ""
                    )
                }
            )

            // Cache the weather data and mark API as called
            val weatherEntity = mapWeatherInfoToEntity(weatherInfo)
            weatherDao.insertWeather(weatherEntity)
            hasCalledApiThisSession = true
            Log.d(TAG, "Weather data cached successfully and API marked as called")

            Log.d(TAG, "Weather data processed successfully")
            ApiResponse.Success(weatherInfo)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching weather data", e)
            e.printStackTrace()

            // If API fails, try to return cached data
            val cachedWeather = weatherDao.getWeather()
            if (cachedWeather != null) {
                Log.d(TAG, "API failed, returning cached data")
                return ApiResponse.Success(mapEntityToWeatherInfo(cachedWeather))
            }

            ApiResponse.Error("Failed to fetch weather data: ${e.message}")
        }
    }

    // Add method to reset the session flag (call this from Application class)
    override fun resetSessionFlag() {
        hasCalledApiThisSession = false
        Log.d(TAG, "Session flag reset - API will be called on next request")
    }

    private fun mapWeatherInfoToEntity(weatherInfo: WeatherInfo): WeatherEntity {
        return WeatherEntity(
            id = 1,
            location = weatherInfo.location,
            temperature = weatherInfo.temperature,
            feelsLike = weatherInfo.feelsLike,
            humidity = weatherInfo.humidity,
            windSpeed = weatherInfo.windSpeed,
            description = weatherInfo.description,
            icon = weatherInfo.icon,
            tempMin = weatherInfo.tempMin,
            tempMax = weatherInfo.tempMax,
            precipitationProbability = weatherInfo.precipitationProbability,
            uvIndex = weatherInfo.uvIndex,
            pressureHPa = weatherInfo.pressureHPa,
            cloudCoverPercentage = weatherInfo.cloudCoverPercentage,
            updatedAt = System.currentTimeMillis(),
            forecast = weatherInfo.forecast
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mapEntityToWeatherInfo(entity: WeatherEntity): WeatherInfo {

        return WeatherInfo(
            location = entity.location,
            temperature = entity.temperature,
            feelsLike = entity.feelsLike,
            humidity = entity.humidity,
            windSpeed = entity.windSpeed,
            description = entity.description,
            icon = entity.icon,
            formattedDate = "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
            tempMin = entity.tempMin,
            tempMax = entity.tempMax,
            precipitationProbability = entity.precipitationProbability,
            uvIndex = entity.uvIndex,
            pressureHPa = entity.pressureHPa,
            cloudCoverPercentage = entity.cloudCoverPercentage,
            forecast = entity.forecast
        )
    }
}