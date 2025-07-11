package com.kapilagro.sasyak.data.repositories

import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.TaskMapper
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.data.api.models.requests.CreateTaskRequest
import com.kapilagro.sasyak.data.api.models.requests.FilterRequest
import com.kapilagro.sasyak.data.api.models.requests.UpdateImplementationRequest
import com.kapilagro.sasyak.data.api.models.requests.UpdateTaskStatusRequest
import com.kapilagro.sasyak.data.api.models.responses.DailyTaskCount
import com.kapilagro.sasyak.data.api.models.responses.TaskListResponse
import com.kapilagro.sasyak.data.api.models.responses.TeamMemberListResponse
import com.kapilagro.sasyak.data.api.models.responses.TrendReportResponse
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
        return try {
            val response = apiService.getTaskReport()
            ApiResponse.Success(TaskMapper.toTaskReport(response))
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "Failed to fetch task report")
        }
    }

    override suspend fun getTrendReport(): ApiResponse<TrendReportResponse> {
        return try {
            val response = apiService.getTrendReport()
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to fetch trend report")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
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

    override suspend fun getTasksByStatus(status: String, page: Int, size: Int): ApiResponse<Pair<List<Task>, Int>> {
        return try {
            val response = apiService.getTasksByStatus(status, page, size)
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResponse.Success(Pair(it.tasks.map { taskDTO -> taskDTO.toDomainModel() }, it.totalCount))
                } ?: ApiResponse.Error("Response body is null")
            } else {
                ApiResponse.Error("Failed to get tasks by status: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResponse.Error("Exception when getting tasks by status: ${e.message}")
        }
    }

    override suspend fun getTasksBySupervisors(page: Int, size: Int): ApiResponse<Pair<List<Task>, Int>> {
        return try {
            val response = apiService.getTasksBySupervisors(page, size)
            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.tasks.map { it.toDomainModel() }
                val totalCount = response.body()!!.totalCount
                ApiResponse.Success(Pair(tasks, totalCount))
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get tasks by supervisors")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getTasksByUserId(userId: Int, page: Int, size: Int): ApiResponse<Pair<List<Task>, Int>> {
        return try {
            val response = apiService.getTasksByUserId(userId, page, size)
            if (response.isSuccessful && response.body() != null) {
                val tasks = response.body()!!.tasks.map { it.toDomainModel() }
                val totalCount = response.body()!!.totalCount
                ApiResponse.Success(Pair(tasks, totalCount))
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get tasks by user ID")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getUsersByRole(role: String): ApiResponse<TeamMemberListResponse> {
        return try {
            val response = apiService.getUsersByRole(role)
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get users by role")
            }
        }catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getTasksByFilter(status: String, page: Int, size: Int, sortBy: String, sortDirection: String, managerId: Int?): ApiResponse<Pair<List<Task>, Int>> {
        return try {
            val response = apiService.getTasksByFilter(
                FilterRequest(
                    status = status,
                    page = page,
                    size = size,
                    sortBy = sortBy,
                    sortDirection = sortDirection,
                    managerId = managerId
                )
            )
            if (response.isSuccessful) {
                response.body()?.let {
                    ApiResponse.Success(Pair(it.tasks.map { taskDTO -> taskDTO.toDomainModel() }, it.totalCount))
                } ?: ApiResponse.Error("Response body is null")
            } else {
                ApiResponse.Error("Failed to get tasks by status: ${response.message()}")
            }
        } catch (e: Exception) {
            ApiResponse.Error("Exception when getting tasks by status: ${e.message}")
        }
    }
}