package com.kapilagro.sasyak.data.api.mappers

import com.kapilagro.sasyak.data.db.entities.NotificationEntity
import com.kapilagro.sasyak.domain.models.Notification
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun NotificationEntity.toDomainModel(): Notification {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    return Notification(
        id = id,
        title = title,
        message = message,
        taskId = taskId,
        isRead = isRead,
        createdAt = dateFormat.format(createdAt),
        timeAgo = null // timeAgo will be computed in the UI if needed
    )
}

fun Notification.toEntityModel(): NotificationEntity {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    return NotificationEntity(
        id = id,
        title = title ?: "", // Provide default empty string if null
        message = message ?: "", // Provide default empty string if null
        taskId = taskId,
        isRead = isRead,
        createdAt = createdAt?.let { dateFormat.parse(it) } ?: Date(),
        lastSyncedAt = Date() // Set to current date
    )
}