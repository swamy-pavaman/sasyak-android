package com.kapilagro.sasyak.data.api.models.responses

data class NotificationListResponse(
    val notifications: List<NotificationResponse>,
    val totalCount: Int,
    val currentPage: Int,
    val totalPages: Int
)