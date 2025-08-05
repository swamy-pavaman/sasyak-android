package com.kapilagro.sasyak.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kapilagro.sasyak.data.db.entities.PreviewEntity

@Dao
interface PreviewDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreview(preview : PreviewEntity)

    @Query("SELECT * FROM preview WHERE taskType = :taskType")
    suspend fun getLastPreviewByType(taskType: String): PreviewEntity?
}