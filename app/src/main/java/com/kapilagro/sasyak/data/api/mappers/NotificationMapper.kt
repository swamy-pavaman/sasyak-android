package com.kapilagro.sasyak.data.api.mappers


import com.kapilagro.sasyak.data.db.entities.NotificationEntity
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.utils.DateUtils
import java.text.SimpleDateFormat
import java.util.*

fun NotificationEntity.toDomainModel(): Notification {
    return Notification(
        id = id,
        title = title,
        message = message,
        taskId = taskId,
        isRead = isRead,
        createdAt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).format(createdAt),
        timeAgo = DateUtils.formatTimeAgo(createdAt)
    )
}

fun Notification.toEntityModel(): NotificationEntity {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    return NotificationEntity(
        id = id,
        title = title,
        message = message,
        taskId = taskId,
        isRead = isRead,
        createdAt = try {
            dateFormat.parse(createdAt) ?: Date()
        } catch (e: Exception) {
            Date()
        }
    )
}