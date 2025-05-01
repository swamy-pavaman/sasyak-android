package com.kapilagro.sasyak.domain.models

data class DailyForecast(
    val date: String,
    val dayOfWeek: String,
    val tempMin: Double,
    val tempMax: Double,
    val precipitationProbability: Int,
    val description: String,
    val icon: String,
    val windSpeed: Double,
    val humidity: Int
)