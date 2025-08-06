package com.kapilagro.sasyak.data.api.models.responses.openweather

import com.google.gson.annotations.SerializedName

data class WeatherForecastResponse(
    @SerializedName("location") val location: Location?,
    @SerializedName("current") val current: Current?,
    @SerializedName("forecast") val forecast: Forecast?
)

data class Location(
    @SerializedName("name") val name: String?,
    @SerializedName("region") val region: String?,
    @SerializedName("country") val country: String?,
    @SerializedName("lat") val lat: Double?,
    @SerializedName("lon") val lon: Double?,
    @SerializedName("tz_id") val tzId: String?,
    @SerializedName("localtime_epoch") val localtimeEpoch: Long?,
    @SerializedName("localtime") val localtime: String?
)

data class Current(
    @SerializedName("temp_c") val tempC: Double?,
    @SerializedName("is_day") val isDay: Int?,
    @SerializedName("condition") val condition: Condition?,
    @SerializedName("wind_kph") val windKph: Double?,
    @SerializedName("pressure_mb") val pressureMb: Double?,
    @SerializedName("humidity") val humidity: Int?,
    @SerializedName("cloud") val cloud: Int?,
    @SerializedName("feelslike_c") val feelsLikeC: Double?,
    @SerializedName("uv") val uv: Double?
)

data class Forecast(
    @SerializedName("forecastday") val forecastDay: List<ForecastDay>?
)

data class ForecastDay(
    @SerializedName("date") val date: String?,
    @SerializedName("day") val day: Day?
)

data class Day(
    @SerializedName("maxtemp_c") val maxTempC: Double?,
    @SerializedName("mintemp_c") val minTempC: Double?,
    @SerializedName("avgtemp_c") val avgTempC: Double?,
    @SerializedName("daily_chance_of_rain") val chanceOfRain: Int?,
    @SerializedName("condition") val condition: Condition?
)

data class Condition(
    @SerializedName("text") val text: String?,
    @SerializedName("icon") val icon: String?
)

data class ForecastItem(
    val date: String,
    val maxTempC: Double,
    val minTempC: Double,
    val avgTempC: Double,
    val chanceOfRain: Int,
    val conditionText: String,
    val iconUrl: String
)
