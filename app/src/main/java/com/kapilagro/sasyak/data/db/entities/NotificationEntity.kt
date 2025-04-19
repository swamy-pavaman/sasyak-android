package com.kapilagro.sasyak.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey
    val id: Int,
    val title: String,
    val message: String,
    val taskId: Int?,
    val isRead: Boolean,
    val createdAt: Date,
    val lastSyncedAt: Date = Date()
)