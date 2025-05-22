package com.kapilagro.sasyak.domain.repositories

import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.data.api.models.requests.CreateAdviceRequest
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TaskAdvice
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskAdviceRepositoryImpl @Inject constructor(
    private val apiService: ApiService
) : TaskAdviceRepository {

    override suspend fun createTaskAdvice(taskId: Int, adviceText: String): ApiResponse<TaskAdvice> {
        return try {
            val response = apiService.createTaskAdvice(
                CreateAdviceRequest(
                    taskId = taskId,
                    adviceText = adviceText
                )
            )

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.toDomainModel())
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to create advice")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getAdviceForTask(taskId: Int): ApiResponse<List<TaskAdvice>> {
        return try {
            val response = apiService.getAdviceForTask(taskId)

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.advices.map { it.toDomainModel() })
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get advice")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getAdviceProvidedByCurrentManager(): ApiResponse<List<TaskAdvice>> {
        return try {
            val response = apiService.getAdviceProvidedByCurrentManager()
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.advices.map { it.toDomainModel() })
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get provided advice")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }
}