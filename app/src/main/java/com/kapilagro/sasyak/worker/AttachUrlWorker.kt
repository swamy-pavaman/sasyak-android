package com.kapilagro.sasyak.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class AttachUrlWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Get the Task ID and URLs from the previous worker's output.
        val taskId = inputData.getInt(FileUploadWorker.KEY_TASK_ID_OUTPUT, -1)
        val uploadedUrls = inputData.getStringArray(FileUploadWorker.KEY_UPLOADED_URLS_OUTPUT)

        if (taskId == -1 || uploadedUrls.isNullOrEmpty()) {
            Log.e("AttachUrlWorker", "Invalid input data from FileUploadWorker.")
            return@withContext Result.failure()
        }

        Log.d("AttachUrlWorker", "Step 2: Attaching ${uploadedUrls.size} URLs to task $taskId")

        return@withContext try {
            val attachResponse = taskRepository.attachMediaToTask(taskId, uploadedUrls.toList())

            if (attachResponse.isSuccessful) {
                Log.d("AttachUrlWorker", "Stepx 2 SUCCEEDED. Work complete.")
                Result.success()
            } else {
                val errorMsg = attachResponse.errorBody()?.string() ?: "Failed to attach media"
                Log.e("AttachUrlWorker", "Step 2 FAILED: $errorMsg. Retrying.")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("AttachUrlWorker", "Exception during media attachment: ${e.message}. Retrying.")
            Result.retry()
        }
    }
}