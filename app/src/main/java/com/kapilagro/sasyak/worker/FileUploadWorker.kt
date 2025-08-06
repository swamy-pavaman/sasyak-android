package com.kapilagro.sasyak.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.kapilagro.sasyak.data.api.ImageUploadService
import com.kapilagro.sasyak.domain.models.ApiResponse
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@HiltWorker
class FileUploadWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val imageUploader: ImageUploadService // Assumes this service is provided via Hilt
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val UPLOAD_TAG = "file-upload-tag"

        // Input keys (read by the worker from its own input)
        const val KEY_TASK_ID_INPUT = "task_id_input"
        const val KEY_IMAGE_PATHS_INPUT = "image_paths_input"
        const val KEY_FOLDER_INPUT = "folder_input"
        const val KEY_ENQUEUED_AT = "enqueued_at"

        // Progress data keys (published by worker, read by ViewModel)
        const val KEY_PROGRESS_TASK_ID = "progress_task_id"
        const val KEY_PROGRESS_FOLDER = "progress_folder"
        const val KEY_PROGRESS_ENQUEUED_AT = "progress_enqueued_at"
        const val KEY_PROGRESS_UPLOADED = "progress_uploaded"
        const val KEY_PROGRESS_TOTAL = "progress_total"

        // Output keys (for the next worker in the chain)
        const val KEY_TASK_ID_OUTPUT = "task_id_output"
        const val KEY_UPLOADED_URLS_OUTPUT = "uploaded_urls_output"

        fun createInputData(taskId: Int, imagePaths: List<String>, folder: String): Data {
            print("shiva , $taskId , $imagePaths , $folder")
            return workDataOf(
                KEY_TASK_ID_INPUT to taskId,
                KEY_IMAGE_PATHS_INPUT to imagePaths.toTypedArray(),
                KEY_FOLDER_INPUT to folder,
                KEY_ENQUEUED_AT to System.currentTimeMillis()
            )
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val taskId = inputData.getInt(KEY_TASK_ID_INPUT, -1)
        val imagePaths = inputData.getStringArray(KEY_IMAGE_PATHS_INPUT) ?: emptyArray()
        val folder = inputData.getString(KEY_FOLDER_INPUT) ?: "Tasks"
        val enqueuedAt = inputData.getLong(KEY_ENQUEUED_AT, 0L)

        if (taskId == -1 || imagePaths.isEmpty()) {
            return@withContext Result.failure()
        }

        val imageFiles = imagePaths.map { File(it) }.filter { it.exists() }
        val totalFiles = imageFiles.size
        if (imageFiles.isEmpty()) {
            return@withContext Result.failure()
        }

        // Publish an initial progress report with all static UI info
        val initialProgress = workDataOf(
            KEY_PROGRESS_TASK_ID to taskId,
            KEY_PROGRESS_FOLDER to folder,
            KEY_PROGRESS_ENQUEUED_AT to enqueuedAt,
            KEY_PROGRESS_TOTAL to totalFiles,
            KEY_PROGRESS_UPLOADED to 0
        )
        setProgress(initialProgress)

        val uploadedUrls = mutableListOf<String>()

        imageFiles.forEachIndexed { index, file ->
            try {
                // Assuming `uploadFiles` can take a single file in a list
                when (val result = imageUploader.uploadFiles(listOf(file), folder)) {
                    is ApiResponse.Success -> {
                        uploadedUrls.addAll(result.data)
                        val progressData = workDataOf(
                            KEY_PROGRESS_TASK_ID to taskId,
                            KEY_PROGRESS_FOLDER to folder,
                            KEY_PROGRESS_ENQUEUED_AT to enqueuedAt,
                            KEY_PROGRESS_TOTAL to totalFiles,
                            KEY_PROGRESS_UPLOADED to (index + 1)
                        )
                        setProgress(progressData)
                    }
                    is ApiResponse.Error -> {
                        Log.e("FileUploadWorker", "Upload failed: ${result.errorMessage}. Retrying.")
                        return@withContext Result.retry()
                    }
                    is ApiResponse.Loading -> {} // Not applicable in a background worker
                }
            } catch (e: Exception) {
                Log.e("FileUploadWorker", "Exception during upload: ${e.message}. Retrying.")
                return@withContext Result.retry()
            }
        }

        val outputData = workDataOf(
            KEY_TASK_ID_OUTPUT to taskId,
            KEY_UPLOADED_URLS_OUTPUT to uploadedUrls.toTypedArray()
        )
        return@withContext Result.success(outputData)
    }
}