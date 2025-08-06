package com.kapilagro.sasyak.data.api

import com.kapilagro.sasyak.data.api.models.responses.openweather.GeocodeResponse
import com.kapilagro.sasyak.data.api.models.responses.openweather.WeatherForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    @GET("geo/1.0/reverse")
    suspend fun getReverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<GeocodeResponse>

    @GET("forecast.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String, // e.g., "17.4213,78.3478"
        @Query("days") days: Int = 7
    ): WeatherForecastResponse
}