package com.kapilagro.sasyak.presentation.sync

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.kapilagro.sasyak.worker.FileUploadWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SyncViewModel @Inject constructor(
    workManager: WorkManager
) : ViewModel() {

    val uploadJobs: StateFlow<List<UploadJobUiState>> =
        workManager.getWorkInfosByTagFlow(FileUploadWorker.UPLOAD_TAG)
            .map { workInfos ->
                workInfos.mapNotNull { workInfo ->
                    mapToUiState(workInfo)
                }
                    .sortedByDescending { it.enqueuedAt } // Show newest uploads first
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    private fun mapToUiState(workInfo: WorkInfo): UploadJobUiState? {
        val progressData = workInfo.progress

        // The worker may not have run setProgress yet, so data might be missing.
        // If a key piece of info like the task ID isn't there, we can't display it.
        if (!progressData.keyValueMap.containsKey(FileUploadWorker.KEY_PROGRESS_TASK_ID)) {
            return null
        }

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