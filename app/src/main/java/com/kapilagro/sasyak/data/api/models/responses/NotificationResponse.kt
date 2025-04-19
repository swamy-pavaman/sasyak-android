package com.kapilagro.sasyak.data.api.models.responses
data class NotificationResponse(
    val id: Int,
    val title: String,
    val message: String,
    val taskId: Int?,
    val isRead: Boolean,
    val createdAt: String
)