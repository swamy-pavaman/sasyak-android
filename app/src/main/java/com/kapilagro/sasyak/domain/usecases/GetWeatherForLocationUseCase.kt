package com.kapilagro.sasyak.domain.usecases

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.WeatherInfo
import com.kapilagro.sasyak.domain.repositories.WeatherRepository
import javax.inject.Inject

class GetWeatherForLocationUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(location: String): ApiResponse<WeatherInfo> {
        return weatherRepository.getWeatherForLocation(location)
    }
}