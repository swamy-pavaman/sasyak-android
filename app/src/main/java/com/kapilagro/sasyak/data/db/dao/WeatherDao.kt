package com.kapilagro.sasyak.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kapilagro.sasyak.data.db.entities.WeatherEntity

@Dao
interface WeatherDao {

    @Query("SELECT * FROM weather WHERE id = 1 LIMIT 1")
    suspend fun getWeather(): WeatherEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

}