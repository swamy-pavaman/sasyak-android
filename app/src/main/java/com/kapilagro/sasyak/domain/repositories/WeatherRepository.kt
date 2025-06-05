package com.kapilagro.sasyak.domain.repositories

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.WeatherInfo

interface WeatherRepository {
    suspend fun getWeatherForLocation(location: String): ApiResponse<WeatherInfo>
}