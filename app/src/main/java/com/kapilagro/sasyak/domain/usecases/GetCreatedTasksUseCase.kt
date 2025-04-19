package com.kapilagro.sasyak.domain.usecases
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import javax.inject.Inject
class GetCreatedTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(page: Int = 0, size: Int = 10): ApiResponse<Pair<List<Task>, Int>> {
        return taskRepository.getCreatedTasks(page, size)
    }
}