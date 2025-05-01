package com.kapilagro.sasyak.data.api

import com.kapilagro.sasyak.data.api.models.responses.openweather.GeocodeResponse
import com.kapilagro.sasyak.data.api.models.responses.openweather.OpenWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApiService {

    @GET("data/3.0/onecall")
    suspend fun getWeatherOneCall(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("exclude") exclude: String = "minutely,hourly,alerts",
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OpenWeatherResponse

    @GET("geo/1.0/reverse")
    suspend fun getReverseGeocode(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 1,
        @Query("appid") apiKey: String
    ): List<GeocodeResponse>
}