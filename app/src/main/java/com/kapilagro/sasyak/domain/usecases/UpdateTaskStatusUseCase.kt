package com.kapilagro.sasyak.domain.usecases

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import javax.inject.Inject

class UpdateTaskStatusUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, status: String, comment: String?): ApiResponse<Task> {
        return taskRepository.updateTaskStatus(taskId, status, comment)
    }
}
