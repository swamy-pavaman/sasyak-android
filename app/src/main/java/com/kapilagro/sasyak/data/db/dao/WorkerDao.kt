package com.kapilagro.sasyak.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kapilagro.sasyak.data.db.entities.WorkJobEntity
import java.util.UUID

@Dao
interface WorkerDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(workRequest: WorkJobEntity)

    @Query("SELECT * FROM worker_jobs WHERE workId = :workId")
    suspend fun getByWorkId(workId: UUID): WorkJobEntity?

    @Query("DELETE FROM worker_jobs WHERE workId = :workId")
    suspend fun deleteByWorkId(workId: UUID)
}