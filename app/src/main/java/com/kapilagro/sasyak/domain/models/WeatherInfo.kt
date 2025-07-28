package com.kapilagro.sasyak.domain.models

import com.kapilagro.sasyak.data.api.models.responses.openweather.ForecastItem

data class WeatherInfo(
    val location: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val icon: String,
    val formattedDate: String,
    val tempMin: Double,
    val tempMax: Double,
    val precipitationProbability: Int,
    val uvIndex: Int,
    val pressureHPa: Int,
    val cloudCoverPercentage: Int,
    val forecast: List<ForecastItem>
)