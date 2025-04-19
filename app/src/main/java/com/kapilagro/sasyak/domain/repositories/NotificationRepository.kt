package com.kapilagro.sasyak.domain.repositories

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Notification

interface NotificationRepository {
    suspend fun getNotifications(
        onlyUnread: Boolean = false,
        page: Int = 0,
        size: Int = 10
    ): ApiResponse<Pair<List<Notification>, Int>>
    suspend fun getUnreadNotificationCount(): ApiResponse<Int>
    suspend fun markNotificationAsRead(notificationId: Int): ApiResponse<Boolean>
    suspend fun markAllNotificationsAsRead(): ApiResponse<Boolean>
}