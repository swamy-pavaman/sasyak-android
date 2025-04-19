package com.kapilagro.sasyak.domain.usecases

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import javax.inject.Inject

class CreateTaskAdviceUseCase @Inject constructor(
    private val taskAdviceRepository: TaskAdviceRepository
) {
    suspend operator fun invoke(taskId: Int, adviceText: String): ApiResponse<TaskAdvice> {
        return taskAdviceRepository.createTaskAdvice(taskId, adviceText)
    }
}