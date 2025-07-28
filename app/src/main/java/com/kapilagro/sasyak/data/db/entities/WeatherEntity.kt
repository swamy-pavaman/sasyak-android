package com.kapilagro.sasyak.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kapilagro.sasyak.data.api.models.responses.openweather.ForecastItem

@Entity(tableName = "weather")
data class WeatherEntity (
    @PrimaryKey
    val id: Int = 1,  // Always 1 for the only row
    val location: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val icon: String,
    val tempMin: Double,
    val tempMax: Double,
    val precipitationProbability: Int,
    val uvIndex: Int,
    val pressureHPa: Int,
    val cloudCoverPercentage: Int,
    val updatedAt: Long, // add this for staleness check
    val forecast: List<ForecastItem>
)