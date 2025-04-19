package com.kapilagro.sasyak.domain.usecases

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Notification
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.domain.repositories.NotificationRepository
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import com.kapilagro.sasyak.domain.repositories.UserRepository
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(
        onlyUnread: Boolean = false,
        page: Int = 0,
        size: Int = 10
    ): ApiResponse<Pair<List<Notification>, Int>> {
        return notificationRepository.getNotifications(onlyUnread, page, size)
    }
}