package com.kapilagro.sasyak.domain.models


data class WeatherInfo(
    val location: String,
    val temperature: Double,
    val feelsLike: Double,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val icon: String,
    val formattedDate: String
)