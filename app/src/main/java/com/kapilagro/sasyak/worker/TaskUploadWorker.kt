package com.kapilagro.sasyak.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.collections.toTypedArray

@HiltWorker
class TaskUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val taskType = inputData.getString("taskType") ?: return Result.failure()
            val description = inputData.getString("description") ?: return Result.failure()
            val detailsJson = inputData.getString("detailsJson") ?: return Result.failure()
            val imagesJson = inputData.getString("imagesJson") ?: return Result.failure()
            val assignedToId = inputData.getInt("assignedToId", -1).takeIf { it != -1 }
            Log.d("WORKER", "doWork1: $taskType, $description, $detailsJson, $assignedToId")


            val result = taskRepository.createTask(
                taskType = taskType,
                description = description,
                detailsJson = detailsJson,
                imagesJson = "[]",
                assignedToId = assignedToId
            )

            return if (result is ApiResponse.Success) {
                val taskId = result.data?.id ?: return Result.failure()
                Result.success(workDataOf("task_id_input" to taskId))
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}