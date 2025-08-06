package com.kapilagro.sasyak.data.db.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kapilagro.sasyak.data.api.models.responses.openweather.ForecastItem

class ForecastConverters {

    @TypeConverter
    fun fromForecastList(forecast: List<ForecastItem>): String {
        return Gson().toJson(forecast)
    }

    @TypeConverter
    fun toForecastList(forecastString: String): List<ForecastItem> {
        val listType = object : TypeToken<List<ForecastItem>>() {}.type
        return Gson().fromJson(forecastString, listType)
    }
}
