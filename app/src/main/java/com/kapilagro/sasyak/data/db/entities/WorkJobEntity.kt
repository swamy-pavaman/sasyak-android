package com.kapilagro.sasyak.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "worker_jobs")
data class WorkJobEntity(
    @PrimaryKey val workId: UUID,
    val taskID: Int? = null,
    val taskType: String? = null,
    val description: String? = null,
    val folder: String? = null,
    val enqueuedAt: Long? = null,
)
