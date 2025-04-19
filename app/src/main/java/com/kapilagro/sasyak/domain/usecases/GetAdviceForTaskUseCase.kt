package com.kapilagro.sasyak.domain.usecases

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import javax.inject.Inject

class GetAdviceForTaskUseCase @Inject constructor(
    private val taskAdviceRepository: TaskAdviceRepository
) {
    suspend operator fun invoke(taskId: Int): ApiResponse<List<TaskAdvice>> {
        return taskAdviceRepository.getAdviceForTask(taskId)
    }
}