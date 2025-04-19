package com.kapilagro.sasyak.data.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.domain.repositories.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject    constructor(
    private val apiService: ApiService
) : NotificationRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getNotifications(
        onlyUnread: Boolean,
        page: Int,
        size: Int
    ): ApiResponse<Pair<List<Notification>, Int>> {
        return try {
            val response = apiService.getNotifications(onlyUnread, page, size)

            if (response.isSuccessful && response.body() != null) {
                val notifications = response.body()!!.notifications.map { it.toDomainModel() }
                val totalCount = response.body()!!.totalCount
                ApiResponse.Success(Pair(notifications, totalCount))
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get notifications")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getUnreadNotificationCount(): ApiResponse<Int> {
        return try {
            val response = apiService.getUnreadNotificationCount()

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.count)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get unread count")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun markNotificationAsRead(notificationId: Int): ApiResponse<Boolean> {
        return try {
            val response = apiService.markNotificationAsRead(notificationId)

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.success)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to mark as read")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun markAllNotificationsAsRead(): ApiResponse<Boolean> {
        return try {
            val response = apiService.markAllNotificationsAsRead()

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.success)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to mark all as read")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }
}