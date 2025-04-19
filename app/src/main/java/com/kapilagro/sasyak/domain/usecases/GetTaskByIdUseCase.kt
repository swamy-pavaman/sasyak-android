package com.kapilagro.sasyak.domain.usecases
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import com.kapilagro.sasyak.domain.repositories.UserRepository
import javax.inject.Inject

class GetTaskByIdUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Int): ApiResponse<Pair<Task, List<TaskAdvice>>> {
        return taskRepository.getTaskById(taskId)
    }
}