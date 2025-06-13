package com.kapilagro.sasyak.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.data.db.dao.NotificationDao
import com.kapilagro.sasyak.data.db.entities.NotificationEntity
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import com.kapilagro.sasyak.domain.repositories.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository,
    private val notificationDao: NotificationDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _notificationsState = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val notificationsState: StateFlow<NotificationsState> = _notificationsState.asStateFlow()

    private val _unreadCountState = MutableStateFlow<UnreadCountState>(UnreadCountState.Loading)
    val unreadCountState: StateFlow<UnreadCountState> = _unreadCountState.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    init {
        loadNotifications()
        loadUnreadCount()

        viewModelScope.launch {
            authRepository.getUserRole().collect { role ->
                _userRole.value = role
            }
        }
    }

    fun loadNotifications() {
        _notificationsState.value = NotificationsState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                when (val response = notificationRepository.getNotifications(false, 0, 50)) {
                    is ApiResponse.Success -> {
                        _notificationsState.value = NotificationsState.Success(response.data.first)
                    }
                    is ApiResponse.Error -> {
                        if (isOfflineError(response.errorMessage)) {
                            notificationDao.getAllNotifications().collectLatest { entities ->
                                val notifications = entities.map { it.toDomainModel() }
                                _notificationsState.value = NotificationsState.Success(notifications)
                            }
                        } else {
                            _notificationsState.value = NotificationsState.Error(response.errorMessage)
                        }
                    }
                    is ApiResponse.Loading -> {
                        _notificationsState.value = NotificationsState.Loading
                    }
                }
            } catch (e: Exception) {
                notificationDao.getAllNotifications().collectLatest { entities ->
                    val notifications = entities.map { it.toDomainModel() }
                    _notificationsState.value = NotificationsState.Success(notifications)
                }
            }
        }
    }

    fun loadUnreadCount() {
        _unreadCountState.value = UnreadCountState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                when (val response = notificationRepository.getUnreadNotificationCount()) {
                    is ApiResponse.Success -> {
                        _unreadCountState.value = UnreadCountState.Success(response.data)
                    }
                    is ApiResponse.Error -> {
                        if (isOfflineError(response.errorMessage)) {
                            _unreadCountState.value = UnreadCountState.Success(0)
                        } else {
                            _unreadCountState.value = UnreadCountState.Error(response.errorMessage)
                        }
                    }
                    is ApiResponse.Loading -> {
                        _unreadCountState.value = UnreadCountState.Loading
                    }
                }
            } catch (e: Exception) {
                _unreadCountState.value = UnreadCountState.Success(0)
            }
        }
    }

    fun markNotificationAsRead(notificationId: Int) {
        viewModelScope.launch(ioDispatcher) {
            try {
                when (val response = notificationRepository.markNotificationAsRead(notificationId)) {
                    is ApiResponse.Success -> {
                        val currentNotifications = (_notificationsState.value as? NotificationsState.Success)?.notifications ?: emptyList()
                        val updatedNotifications = currentNotifications.map { notification ->
                            if (notification.id == notificationId) {
                                val updated = notification.copy(isRead = true)
                                notificationDao.insertNotification(updated.toEntity())
                                updated
                            } else {
                                notification
                            }
                        }
                        _notificationsState.value = NotificationsState.Success(updatedNotifications)
                        loadUnreadCount()
                    }
                    is ApiResponse.Error -> {
                        // Handle error if needed
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Handle unexpected errors
            }
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch(ioDispatcher) {
            try {
                when (val response = notificationRepository.markAllNotificationsAsRead()) {
                    is ApiResponse.Success -> {
                        val currentNotifications = (_notificationsState.value as? NotificationsState.Success)?.notifications ?: emptyList()
                        val updatedNotifications = currentNotifications.map {
                            val updated = it.copy(isRead = true)
                            notificationDao.insertNotification(updated.toEntity())
                            updated
                        }
                        _notificationsState.value = NotificationsState.Success(updatedNotifications)
                        _unreadCountState.value = UnreadCountState.Success(0)
                    }
                    is ApiResponse.Error -> {
                        // Handle error if needed
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Handle unexpected errors
            }
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
            timeAgo = null // Assuming timeAgo is computed in the UI
        )
    }

    sealed class NotificationsState {
        object Loading : NotificationsState()
        data class Success(val notifications: List<Notification>) : NotificationsState()
        data class Error(val message: String) : NotificationsState()
    }

    sealed class UnreadCountState {
        object Loading : UnreadCountState()
        data class Success(val count: Int) : UnreadCountState()
        data class Error(val message: String) : UnreadCountState()
    }
}