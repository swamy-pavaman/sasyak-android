package com.kapilagro.sasyak.data.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kapilagro.sasyak.data.api.WeatherApiService
import com.kapilagro.sasyak.data.api.models.responses.openweather.CurrentWeatherResponse
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

            // Default coordinates (Pithapuram)
            var latitude = 17.264690446951853
            var longitude = 78.27062508184291

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
                latitude = latitude,
                longitude = longitude,
                apiKey = NetworkModule.OPENWEATHER_API_KEY
            )
            Log.d(TAG, "Current weather data received successfully")

            // Get location name from reverse geocoding
            val geocodeResponse = try {
                weatherApiService.getReverseGeocode(
                    latitude = latitude,
                    longitude = longitude,
                    apiKey = NetworkModule.OPENWEATHER_API_KEY
                )
            } catch (e: Exception) {
                Log.e(TAG, "Geocoding error: ${e.message}")
                emptyList()
            }

            val locationName = if (geocodeResponse.isNotEmpty()) {
                val loc = geocodeResponse[0]
                "${loc.name}, ${loc.state ?: loc.country}"
            } else {
                "Lat: $latitude, Lon: $longitude"
            }

            // Create WeatherInfo object
            val weatherInfo = WeatherInfo(
                location = locationName,
                temperature = weatherResponse.main?.temp ?: 0.0,
                feelsLike = weatherResponse.main?.feelsLike ?: 0.0,
                humidity = weatherResponse.main?.humidity ?: 0,
                windSpeed = weatherResponse.wind?.speed ?: 0.0,
                description = weatherResponse.weather?.firstOrNull()?.description?.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                } ?: "Unknown",
                icon = weatherResponse.weather?.firstOrNull()?.icon ?: "01d",
                formattedDate = "Today, ${LocalDate.now().format(DateTimeFormatter.ofPattern("MMM d"))}",
                tempMin = weatherResponse.main?.tempMin ?: 0.0,
                tempMax = weatherResponse.main?.tempMax ?: 0.0,
                precipitationProbability = weatherResponse.clouds?.all?.let { if (it > 50) it / 2 else 0 } ?: 0,
                uvIndex = 0, // Not available in Current Weather API
                pressureHPa = weatherResponse.main?.pressure ?: 0,
                cloudCoverPercentage = weatherResponse.clouds?.all ?: 0,
                forecast = generateApproximateForecast(weatherResponse)
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
            updatedAt = System.currentTimeMillis()
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun mapEntityToWeatherInfo(entity: WeatherEntity): WeatherInfo {
        val forecast = generateApproximateForecastFromEntity(entity)

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
            forecast = forecast
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateApproximateForecast(current: CurrentWeatherResponse): List<DailyForecast> {
        val forecast = mutableListOf<DailyForecast>()
        val today = LocalDate.now()
        val currentTemp = current.main?.temp ?: 0.0
        val currentDescription = current.weather?.firstOrNull()?.description ?: "Unknown"
        val currentIcon = current.weather?.firstOrNull()?.icon ?: "01d"
        val currentTempMin = current.main?.tempMin ?: 0.0
        val currentTempMax = current.main?.tempMax ?: 0.0
        val currentHumidity = current.main?.humidity ?: 0
        val currentWindSpeed = current.wind?.speed ?: 0.0
        val currentClouds = current.clouds?.all ?: 0

        for (i in 0 until 7) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }

            // Approximate forecast by slightly varying current conditions
            val tempVariation = Random.nextDouble(-2.0, 2.0)
            val precipVariation = Random.nextInt(0, 20)
            forecast.add(
                DailyForecast(
                    date = date.format(DateTimeFormatter.ofPattern("MMM d")),
                    dayOfWeek = dayOfWeek,
                    tempMin = currentTempMin + tempVariation - 1,
                    tempMax = currentTempMax + tempVariation + 1,
                    precipitationProbability = currentClouds.let { prob ->
                        (prob / 2 + precipVariation).coerceIn(0, 100)
                    },
                    description = currentDescription,
                    icon = currentIcon,
                    windSpeed = currentWindSpeed + Random.nextDouble(-1.0, 1.0),
                    humidity = (currentHumidity + Random.nextInt(-5, 5)).coerceIn(0, 100)
                )
            )
        }
        return forecast
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun generateApproximateForecastFromEntity(entity: WeatherEntity): List<DailyForecast> {
        val forecast = mutableListOf<DailyForecast>()
        val today = LocalDate.now()

        for (i in 0 until 7) {
            val date = today.plusDays(i.toLong())
            val dayOfWeek = when (i) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            }

            // Approximate forecast by slightly varying cached conditions
            val tempVariation = Random.nextDouble(-2.0, 2.0)
            val precipVariation = Random.nextInt(0, 20)
            forecast.add(
                DailyForecast(
                    date = date.format(DateTimeFormatter.ofPattern("MMM d")),
                    dayOfWeek = dayOfWeek,
                    tempMin = entity.tempMin + tempVariation - 1,
                    tempMax = entity.tempMax + tempVariation + 1,
                    precipitationProbability = entity.cloudCoverPercentage.let { prob ->
                        (prob / 2 + precipVariation).coerceIn(0, 100)
                    },
                    description = entity.description,
                    icon = entity.icon,
                    windSpeed = entity.windSpeed + Random.nextDouble(-1.0, 1.0),
                    humidity = (entity.humidity + Random.nextInt(-5, 5)).coerceIn(0, 100)
                )
            )
        }
        return forecast
    }
}