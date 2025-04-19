package com.kapilagro.sasyak.domain.usecases

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.repositories.NotificationRepository
import javax.inject.Inject

class GetUnreadNotificationCountUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    suspend operator fun invoke(): ApiResponse<Int> {
        return notificationRepository.getUnreadNotificationCount()
    }
}