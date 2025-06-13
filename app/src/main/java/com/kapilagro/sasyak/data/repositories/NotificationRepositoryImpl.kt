package com.kapilagro.sasyak.data.repositories

import android.os.Build
import androidx.annotation.RequiresApi
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.data.db.dao.NotificationDao
import com.kapilagro.sasyak.data.db.entities.NotificationEntity
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.domain.repositories.NotificationRepository
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val notificationDao: NotificationDao
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
                notifications.filter { it.isRead }.forEach { notification ->
                    notificationDao.insertNotification(notification.toEntity())
                }
                ApiResponse.Success(Pair(notifications, totalCount))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get notifications"
                if (isOfflineError(errorMessage)) {
                    val localNotifications = notificationDao.getAllNotifications().first().map { it.toDomainModel() }
                    ApiResponse.Success(Pair(localNotifications, localNotifications.size))
                } else {
                    ApiResponse.Error(errorMessage)
                }
            }
        } catch (e: Exception) {
            if (e.message?.contains("Failed to connect") == true) {
                val localNotifications = notificationDao.getAllNotifications().first().map { it.toDomainModel() }
                ApiResponse.Success(Pair(localNotifications, localNotifications.size))
            } else {
                ApiResponse.Error(e.message ?: "An unknown error occurred")
            }
        }
    }

    override suspend fun getUnreadNotificationCount(): ApiResponse<Int> {
        return try {
            val response = apiService.getUnreadNotificationCount()

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.count)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Failed to get unread count"
                if (isOfflineError(errorMessage)) {
                    ApiResponse.Success(0)
                } else {
                    ApiResponse.Error(errorMessage)
                }
            }
        } catch (e: Exception) {
            if (e.message?.contains("Failed to connect") == true) {
                ApiResponse.Success(0)
            } else {
                ApiResponse.Error(e.message ?: "An unknown error occurred")
            }
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

    private fun isOfflineError(errorMessage: String): Boolean {
        return errorMessage.contains("Failed to connect") ||
                errorMessage.contains("No internet") ||
                errorMessage.contains("Network error")
    }

    private fun Notification.toEntity(): NotificationEntity {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val parsedDate = createdAt?.let { dateFormat.parse(it) } ?: Date()
        return NotificationEntity(
            id = id,
            title = title ?: "",
            message = message ?: "",
            taskId = taskId,
            isRead = isRead,
            createdAt = parsedDate,
            lastSyncedAt = Date()
        )
    }

    private fun NotificationEntity.toDomainModel(): Notification {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        return Notification(
            id = id,
            title = title,
            message = message,
            taskId = taskId,
            isRead = isRead,
            createdAt = dateFormat.format(createdAt),
            timeAgo = null
        )
    }
}