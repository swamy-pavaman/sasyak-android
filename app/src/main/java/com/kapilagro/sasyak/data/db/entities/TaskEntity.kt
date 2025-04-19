package com.kapilagro.sasyak.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val description: String,
    val status: String,
    val taskType: String,
    val createdBy: String,
    val assignedTo: String?,
    val createdAt: Date,
    val updatedAt: Date,
    val detailsJson: String?,
    val imagesJson: String?,
    val implementationJson: String?,
    val lastSyncedAt: Date = Date()
)