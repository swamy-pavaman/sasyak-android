package com.kapilagro.sasyak.domain.repositories

import com.kapilagro.sasyak.data.api.models.responses.TeamMemberListResponse
import com.kapilagro.sasyak.data.api.models.responses.TrendReportResponse
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.DailyTaskCount
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
    suspend fun getTaskReport(): ApiResponse<TaskReport>
    suspend fun getTrendReport(): ApiResponse<TrendReportResponse>
    suspend fun updateTaskStatus(taskId: Int, status: String, comment: String?): ApiResponse<Task>
    suspend fun updateTaskImplementation(taskId: Int, implementationJson: String): ApiResponse<Task>
    suspend fun getTasksByType(taskType: String, page: Int = 0, size: Int = 10): ApiResponse<Pair<List<Task>, Int>>
    suspend fun getTasksByStatus(status: String, page: Int, size: Int): ApiResponse<Pair<List<Task>, Int>>
    suspend fun getTasksBySupervisors(page: Int = 0, size: Int = 10): ApiResponse<Pair<List<Task>, Int>>
    //Admin
    suspend fun getTasksByUserId(userId: Int, page: Int = 0, size: Int = 10): ApiResponse<Pair<List<Task>, Int>>
    suspend fun getUsersByRole(role: String): ApiResponse<TeamMemberListResponse>
}