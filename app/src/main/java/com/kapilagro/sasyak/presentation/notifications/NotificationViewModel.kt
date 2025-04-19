package com.kapilagro.sasyak.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Notification
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _notificationsState = MutableStateFlow<NotificationsState>(NotificationsState.Loading)
    val notificationsState: StateFlow<NotificationsState> = _notificationsState.asStateFlow()

    private val _unreadCountState = MutableStateFlow<UnreadCountState>(UnreadCountState.Loading)
    val unreadCountState: StateFlow<UnreadCountState> = _unreadCountState.asStateFlow()

    private val _markAsReadState = MutableStateFlow<MarkAsReadState>(MarkAsReadState.Idle)
    val markAsReadState: StateFlow<MarkAsReadState> = _markAsReadState.asStateFlow()

    init {
        loadNotifications()
        loadUnreadCount()
    }

    fun loadNotifications(onlyUnread: Boolean = false, page: Int = 0, size: Int = 20) {
        _notificationsState.value = NotificationsState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = notificationRepository.getNotifications(onlyUnread, page, size)) {
                is ApiResponse.Success -> {
                    _notificationsState.value = NotificationsState.Success(
                        notifications = response.data.first,
                        totalCount = response.data.second,
                        hasMore = response.data.second > (page + 1) * size
                    )
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
        _markAsReadState.value = MarkAsReadState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = notificationRepository.markNotificationAsRead(notificationId)) {
                is ApiResponse.Success -> {
                    _markAsReadState.value = MarkAsReadState.Success
                    loadNotifications()
                    loadUnreadCount()
                }
                is ApiResponse.Error -> {
                    _markAsReadState.value = MarkAsReadState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _markAsReadState.value = MarkAsReadState.Loading
                }
            }
        }
    }

    fun markAllNotificationsAsRead() {
        _markAsReadState.value = MarkAsReadState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = notificationRepository.markAllNotificationsAsRead()) {
                is ApiResponse.Success -> {
                    _markAsReadState.value = MarkAsReadState.Success
                    loadNotifications()
                    loadUnreadCount()
                }
                is ApiResponse.Error -> {
                    _markAsReadState.value = MarkAsReadState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _markAsReadState.value = MarkAsReadState.Loading
                }
            }
        }
    }

    fun clearMarkAsReadState() {
        _markAsReadState.value = MarkAsReadState.Idle
    }

    sealed class NotificationsState {
        object Loading : NotificationsState()
        data class Success(
            val notifications: List<Notification>,
            val totalCount: Int,
            val hasMore: Boolean
        ) : NotificationsState()
        data class Error(val message: String) : NotificationsState()
    }

    sealed class UnreadCountState {
        object Loading : UnreadCountState()
        data class Success(val count: Int) : UnreadCountState()
        data class Error(val message: String) : UnreadCountState()
    }

    sealed class MarkAsReadState {
        object Idle : MarkAsReadState()
        object Loading : MarkAsReadState()
        object Success : MarkAsReadState()
        data class Error(val message: String) : MarkAsReadState()
    }
}
