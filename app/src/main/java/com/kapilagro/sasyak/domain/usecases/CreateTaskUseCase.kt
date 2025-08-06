package com.kapilagro.sasyak.domain.usecases
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.models.TaskResponce
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import javax.inject.Inject
class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        taskType: String,
        description: String,
        detailsJson: String? = null,
        imagesJson: String? = null,
        assignedToId: Int? = null
    ): ApiResponse<TaskResponce> {
        return taskRepository.createTask(
            taskType = taskType,
            description = description,
            detailsJson = detailsJson,
            imagesJson = imagesJson,
            assignedToId = assignedToId
        )
    }
}