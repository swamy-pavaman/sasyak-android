package com.kapilagro.sasyak.presentation.sync

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kapilagro.sasyak.worker.FileUploadWorker
import com.kapilagro.sasyak.worker.TaskUploadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    private val workManager: WorkManager
) : ViewModel() {

    val uploadJobs: StateFlow<List<UploadJobUiState>> =
        workManager.getWorkInfosByTagFlow(FileUploadWorker.UPLOAD_TAG)
            .map { workInfos ->
                workInfos
                    .filter { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.ENQUEUED,
                            WorkInfo.State.RUNNING,
                            WorkInfo.State.BLOCKED -> true
                            else -> false // ignore succeeded, failed, cancelled
                        }
                    }
                    .mapNotNull { workInfo -> mapToUiState(workInfo) }
                    .sortedByDescending { it.enqueuedAt }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    val taskUploadJobs: StateFlow<List<TaskUploadJobUiState>> =
        workManager.getWorkInfosByTagFlow(TaskUploadWorker.UPLOAD_TAG)
            .map { workInfos ->
                workInfos
                    .filter { workInfo ->
                        when (workInfo.state) {
                            WorkInfo.State.ENQUEUED,
                            WorkInfo.State.RUNNING,
                            WorkInfo.State.BLOCKED -> true

                            else -> false // ignore succeeded, failed, cancelled
                        }
                    }
                    .map { workInfo -> mapTaskUploadToUiState(workInfo) }
                    .sortedByDescending { it.enqueuedAt }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun mapToUiState(workInfo: WorkInfo): UploadJobUiState? {
        val progressData = workInfo.progress
        return UploadJobUiState(
            id = workInfo.id,
            taskId = progressData.getInt(FileUploadWorker.KEY_PROGRESS_TASK_ID, -1),
            folder = progressData.getString(FileUploadWorker.KEY_PROGRESS_FOLDER) ?: "N/A",
            totalFiles = progressData.getInt(FileUploadWorker.KEY_PROGRESS_TOTAL, 0),
            uploadedFiles = progressData.getInt(FileUploadWorker.KEY_PROGRESS_UPLOADED, 0),
            status = workInfo.state,
            enqueuedAt = progressData.getLong(FileUploadWorker.KEY_PROGRESS_ENQUEUED_AT, 0L)
        )
    }
    private fun mapTaskUploadToUiState(workInfo: WorkInfo): TaskUploadJobUiState {
        val input = workInfo.progress
        Log.d("VIEWMODEL", "ProgressData: ${input.keyValueMap}")
        return TaskUploadJobUiState(
            id = workInfo.id,
            taskType = input.getString(TaskUploadWorker.KEY_TASK_TYPE) ?: "Unknown",
            description = input.getString(TaskUploadWorker.KEY_DESCRIPTION) ?: " ",
            status = workInfo.state,
            enqueuedAt = input.getLong(TaskUploadWorker.KEY_PROGRESS_ENQUEUED_AT, 0L)
        )
    }

    fun cancelJob(id: UUID) {
        workManager.cancelWorkById(id)
    }
}



data class UploadJobUiState(
    val id: UUID,
    val taskId: Int,
    val folder: String,
    val totalFiles: Int,
    val uploadedFiles: Int,
    val status: WorkInfo.State,
    val enqueuedAt: Long
)
data class TaskUploadJobUiState(
    val id: UUID,
    val taskType: String,
    val description: String,
    val status: WorkInfo.State,
    val enqueuedAt: Long
)