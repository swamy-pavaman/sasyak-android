package com.kapilagro.sasyak.data.repositories

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.data.api.mappers.toEntityModel
import com.kapilagro.sasyak.data.db.dao.NotificationDao
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.domain.repositories.NotificationRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject    constructor(
    private val apiService: ApiService,
    private val notificationsDao: NotificationDao
) : NotificationRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getNotifications(
        onlyUnread: Boolean,
        page: Int,
        size: Int
    ): ApiResponse<Pair<List<Notification>, Int>> {
        return try {
            val response = apiService.getNotifications(onlyUnread, page, size)
            Log.d("NotificationRepository", "API response: $response")

            if (response.isSuccessful && response.body() != null) {
                val notifications = response.body()!!.notifications.map { it.toDomainModel() }
                val totalCount = response.body()!!.totalCount
                notifications.filter { it.isRead }.forEach { notifications ->
                    notificationsDao.insertNotification(notifications.toEntityModel())
                }
                ApiResponse.Success(Pair(notifications, totalCount))
            } else {
                val cachedNotifications = notificationsDao.getAllNotifications().first().map{ it.toDomainModel() }
                Log.d("NotificationRepository", "Cached notifications: $cachedNotifications")
                if (cachedNotifications.isNotEmpty()) {
                    ApiResponse.Success(Pair(cachedNotifications, cachedNotifications.size))
                }
                Log.d("NotificationRepository", "Failed to fetch notifications from cache")
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get notification's")
            }
        } catch (e: Exception) {
            val cachedNotifications = notificationsDao.getAllNotifications().first().map{ it.toDomainModel() }
            Log.d("NotificationRepository", "Cached notifications: $cachedNotifications")

            if (cachedNotifications.isNotEmpty()) {
                ApiResponse.Success(Pair(cachedNotifications, cachedNotifications.size))
            }else
                (ApiResponse.Error(e.message ?: "An unknown error occurred"))
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