package com.kapilagro.sasyak.domain.usecases

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import javax.inject.Inject

class UpdateTaskImplementationUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int, implementationJson: String): ApiResponse<Task> {
        return taskRepository.updateTaskImplementation(taskId, implementationJson)
    }
}