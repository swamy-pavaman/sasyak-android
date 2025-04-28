package com.kapilagro.sasyak.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository,
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
            when (val response = notificationRepository.getNotifications(false, 0, 50)) {
                is ApiResponse.Success -> {
                    _notificationsState.value = NotificationsState.Success(response.data.first)
                }
                is ApiResponse.Error -> {
                    _notificationsState.value = NotificationsState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _notificationsState.value = NotificationsState.Loading
                }
            }
        }
    }
    fun loadUnreadCount() {
        _unreadCountState.value = UnreadCountState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = notificationRepository.getUnreadNotificationCount()) {
                is ApiResponse.Success -> {
                    _unreadCountState.value = UnreadCountState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    _unreadCountState.value = UnreadCountState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _unreadCountState.value = UnreadCountState.Loading
                }
            }
        }
    }

    fun markNotificationAsRead(notificationId: Int) {
        viewModelScope.launch(ioDispatcher) {
            when (notificationRepository.markNotificationAsRead(notificationId)) {
                is ApiResponse.Success -> {
                    // Update notification in the list to show as read
                    val currentNotifications = (_notificationsState.value as? NotificationsState.Success)?.notifications ?: emptyList()
                    val updatedNotifications = currentNotifications.map { notification ->
                        if (notification.id == notificationId) {
                            notification.copy(isRead = true)
                        } else {
                            notification
                        }
                    }
                    _notificationsState.value = NotificationsState.Success(updatedNotifications)

                    // Reload unread count
                    loadUnreadCount()
                }
                else -> {
                    // Handle error if needed
                }
            }
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch(ioDispatcher) {
            when (notificationRepository.markAllNotificationsAsRead()) {
                is ApiResponse.Success -> {
                    // Update all notifications to show as read
                    val currentNotifications = (_notificationsState.value as? NotificationsState.Success)?.notifications ?: emptyList()
                    val updatedNotifications = currentNotifications.map { it.copy(isRead = true) }
                    _notificationsState.value = NotificationsState.Success(updatedNotifications)

                    // Update unread count to 0
                    _unreadCountState.value = UnreadCountState.Success(0)
                }
                else -> {
                    // Handle error if needed
                }
            }
        }
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