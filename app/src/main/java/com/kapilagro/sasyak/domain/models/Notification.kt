package com.kapilagro.sasyak.domain.models
data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val taskId: Int?,
    val isRead: Boolean,
    val createdAt: String,
    val timeAgo: String // Computed field for display purposes
)