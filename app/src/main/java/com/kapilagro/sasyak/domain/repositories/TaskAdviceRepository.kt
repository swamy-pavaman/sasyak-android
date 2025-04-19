package com.kapilagro.sasyak.domain.repositories

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TaskAdvice

interface TaskAdviceRepository {
    suspend fun createTaskAdvice(taskId: Int, adviceText: String): ApiResponse<TaskAdvice>
    suspend fun getAdviceForTask(taskId: Int): ApiResponse<List<TaskAdvice>>
}
