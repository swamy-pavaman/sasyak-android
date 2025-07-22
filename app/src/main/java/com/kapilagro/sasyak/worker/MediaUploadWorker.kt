package com.kapilagro.sasyak.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.ImageUploadService
import com.kapilagro.sasyak.data.api.models.requests.MediaAttachRequest
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File


@HiltWorker
class MediaUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageUploader: ImageUploadService,
    private val taskRepository: TaskRepository
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_IMAGE_PATHS = "image_paths"
        const val FOLDER = "MEDIA"

        fun createInputData(taskId: Int, imagePaths: List<String>, folder: String): Data {
            return Data.Builder()
                .putInt(KEY_TASK_ID, taskId)
                .putStringArray(KEY_IMAGE_PATHS, imagePaths.toTypedArray())
                .putString(FOLDER, folder)
                .build()
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val taskId = inputData.getInt(KEY_TASK_ID, -1)
        val imagePaths = inputData.getStringArray(KEY_IMAGE_PATHS) ?: emptyArray()
        val folder = inputData.getString(FOLDER) ?: "Tasks"

        if (taskId == -1 || imagePaths.isEmpty()) {
            Log.e("MediaUploadWorker", "Invalid input data.")
            return@withContext Result.failure()
        }

        val imageFiles = imagePaths.map { File(it) }.filter { it.exists() }
        if (imageFiles.isEmpty()) {
            Log.e("MediaUploadWorker", "No valid image files found.")
            return@withContext Result.failure()
        }

        // 1. Upload images to S3
        Log.d("MediaUploadWorker", "Uploading ${imageFiles.size} files for task $taskId")
        // ★★★ FIX: Call the renamed 'uploadFiles' method.
        val uploadResult = imageUploader.uploadFiles(imageFiles, folder)

        if (uploadResult !is ApiResponse.Success) {
            val errorMsg = (uploadResult as? ApiResponse.Error)?.errorMessage ?: "Unknown upload error"
            Log.e("MediaUploadWorker", "Upload failed: $errorMsg")
            return@withContext Result.retry()
        }

        // 2. Attach uploaded media filenames to the task via the repository
        // TODO here i have to send presined urls not file names
//        val uploadedFileNames = imageFiles.map { it.name }
        val uploadedFileNames = uploadResult.data
        Log.d("MediaUploadWorker", "Attaching filenames: ${uploadResult.data}")

        return@withContext try {
            val attachResponse = taskRepository.attachMediaToTask(taskId, uploadedFileNames)

            if (attachResponse.isSuccessful) {
                Log.d("MediaUploadWorker", "Work finished successfully for task $taskId.")
                Result.success()
            } else {
                val errorMsg = attachResponse.errorBody()?.string() ?: "Failed to attach media"
                Log.e("MediaUploadWorker", "Failed to attach media to task: $errorMsg")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("MediaUploadWorker", "Exception during media attachment: ${e.message}")
            Result.retry()
        }
    }
}

