package com.kapilagro.sasyak.domain.usecases

import javax.inject.Inject
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import com.kapilagro.sasyak.domain.repositories.UserRepository


class GetSupervisorManagerUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): ApiResponse<User> {
        return userRepository.getSupervisorManager()
    }
}