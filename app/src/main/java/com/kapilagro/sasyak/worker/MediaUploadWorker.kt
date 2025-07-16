package com.kapilagro.sasyak.worker


import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class MediaUploadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val taskRepository: TaskRepository
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_IMAGE_PATHS = "image_paths"

        fun createInputData(taskId: Int, imagePaths: List<String>): Data {
            return Data.Builder()
                .putInt(KEY_TASK_ID, taskId)
                .putStringArray(KEY_IMAGE_PATHS, imagePaths.toTypedArray())
                .build()
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val taskId = inputData.getInt(KEY_TASK_ID, -1)
            val imagePaths = inputData.getStringArray(KEY_IMAGE_PATHS) ?: emptyArray()

            if (taskId == -1 || imagePaths.isEmpty()) {
                return@withContext Result.failure()
            }

            // TODO: Implement presigned URL upload logic here
            // For now, just structure placeholder
            val imageFiles = imagePaths.map { File(it) }.filter { it.exists() }

            if (imageFiles.isEmpty()) {
                return@withContext Result.failure()
            }

            // Placeholder for actual upload logic
            // This will be implemented later with presigned URLs
            uploadMediaFiles(taskId, imageFiles)

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }

    private suspend fun uploadMediaFiles(taskId: Int, files: List<File>) {
        // TODO: Implement actual upload logic with presigned URLs
        // Structure:
        // 1. Get presigned URLs from backend
        // 2. Upload files to presigned URLs
        // 3. Call attachMediaToTask with uploaded URLs

        // Placeholder implementation
        println("Uploading ${files.size} files for task $taskId")
    }
}