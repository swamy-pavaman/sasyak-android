package com.kapilagro.sasyak.domain.repositories

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.models.TaskReport

interface TaskRepository {
    suspend fun getAssignedTasks(page: Int = 0, size: Int = 10): ApiResponse<Pair<List<Task>, Int>>
    suspend fun getCreatedTasks(page: Int = 0, size: Int = 10): ApiResponse<Pair<List<Task>, Int>>
    suspend fun getTaskById(taskId: Int): ApiResponse<Pair<Task, List<TaskAdvice>>>
    suspend fun createTask(
        taskType: String,
        description: String,
        detailsJson: String?,
        imagesJson: String?,
        assignedToId: Int?
    ): ApiResponse<Task>
    suspend fun updateTaskStatus(taskId: Int, status: String, comment: String?): ApiResponse<Task>
    suspend fun updateTaskImplementation(taskId: Int, implementationJson: String): ApiResponse<Task>
    suspend fun getTaskReport(): ApiResponse<TaskReport>
    suspend fun getTasksByType(taskType: String, page: Int = 0, size: Int = 10): ApiResponse<Pair<List<Task>, Int>>
}