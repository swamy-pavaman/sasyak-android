package com.kapilagro.sasyak.data.repositories

import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.data.api.models.requests.CreateTaskRequest
import com.kapilagro.sasyak.data.api.models.requests.UpdateImplementationRequest
import com.kapilagro.sasyak.data.api.models.requests.UpdateTaskStatusRequest
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.models.TaskReport
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TaskRepository {

    override suspend fun getAssignedTasks(page: Int, size: Int): ApiResponse<Pair<List<Task>, Int>> {
        return try {
            val response = apiService.getAssignedTasks(page, size)

            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.tasks.map { it.toDomainModel() }
                val totalCount = response.body()!!.totalCount
                ApiResponse.Success(Pair(tasks, totalCount))
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get tasks")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getCreatedTasks(page: Int, size: Int): ApiResponse<Pair<List<Task>, Int>> {
        return try {
            val response = apiService.getCreatedTasks(page, size)

            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.tasks.map { it.toDomainModel() }
                val totalCount = response.body()!!.totalCount
                ApiResponse.Success(Pair(tasks, totalCount))
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get tasks")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getTaskById(taskId: Int): ApiResponse<Pair<Task, List<TaskAdvice>>> {
        return try {
            val response = apiService.getTaskById(taskId)

            if (response.isSuccessful && response.body() != null) {
                val task = response.body()!!.task.toDomainModel()
                val advices = response.body()!!.advices.map { it.toDomainModel() }
                ApiResponse.Success(Pair(task, advices))
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get task")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun createTask(
        taskType: String,
        description: String,
        detailsJson: String?,
        imagesJson: String?,
        assignedToId: Int?
    ): ApiResponse<Task> {
        return try {
            val response = apiService.createTask(
                CreateTaskRequest(
                    taskType = taskType,
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = imagesJson,
                    assignedToId = assignedToId
                )
            )

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.toDomainModel())
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to create task")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun updateTaskStatus(
        taskId: Int,
        status: String,
        comment: String?
    ): ApiResponse<Task> {
        return try {
            val response = apiService.updateTaskStatus(
                taskId,
                UpdateTaskStatusRequest(
                    status = status,
                    comment = comment
                )
            )

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.toDomainModel())
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to update task status")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun updateTaskImplementation(
        taskId: Int,
        implementationJson: String
    ): ApiResponse<Task> {
        return try {
            val response = apiService.updateTaskImplementation(
                taskId,
                UpdateImplementationRequest(
                    implementationJson = implementationJson
                )
            )

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.toDomainModel())
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to update implementation")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getTaskReport(): ApiResponse<TaskReport> {
        // This would be implemented based on your specific API
        // For now returning a placeholder error
        return ApiResponse.Error("Task report not implemented")
    }

    override suspend fun getTasksByType(
        taskType: String,
        page: Int,
        size: Int
    ): ApiResponse<Pair<List<Task>, Int>> {
        return try {
            val response = apiService.getTasksByType(taskType, page, size)

            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.tasks.map { it.toDomainModel() }
                val totalCount = response.body()!!.totalCount
                ApiResponse.Success(Pair(tasks, totalCount))
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get tasks by type")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

//    override suspend fun getTasksByType(
//        taskType: String,
//        page: Int,
//        size: Int
//    ): ApiResponse<Pair<List<Task>, Int>> {
//        return try {
//            // First try to get from local DB
//            val cachedTasks = taskDao.getTasksByType(taskType, size, page * size).first()
//            val cachedCount = taskDao.getTaskCountByType(taskType)
//
//            // If we have cached tasks, return them immediately
//            if (cachedTasks.isNotEmpty()) {
//                return ApiResponse.Success(Pair(cachedTasks.map { it.toDomainModel() }, cachedCount))
//            }
//
//            // If no cached data or refresh is requested, fetch from network
//            val response = apiService.getTasksByType(taskType, page, size)
//
//            if (response.isSuccessful && response.body() != null) {
//                val tasks = response.body()!!.tasks.map { it.toDomainModel() }
//                val totalCount = response.body()!!.totalCount
//
//                // Cache results in the database
//                withContext(Dispatchers.IO) {
//                    taskDao.insertTasks(tasks.map { it.toEntityModel() })
//                }
//
//                ApiResponse.Success(Pair(tasks, totalCount))
//            } else {
//                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get tasks by type")
//            }
//        } catch (e: Exception) {
//            // If network fails, try to return cached data as a fallback
//            try {
//                val cachedTasks = taskDao.getTasksByType(taskType, size, page * size).first()
//                val cachedCount = taskDao.getTaskCountByType(taskType)
//
//                if (cachedTasks.isNotEmpty()) {
//                    return ApiResponse.Success(Pair(cachedTasks.map { it.toDomainModel() }, cachedCount))
//                }
//            } catch (dbError: Exception) {
//                // Ignore DB errors and continue with original error
//            }
//
//            ApiResponse.Error(e.message ?: "An unknown error occurred")
//        }
//    }
}