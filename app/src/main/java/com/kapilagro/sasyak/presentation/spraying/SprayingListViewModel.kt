package com.kapilagro.sasyak.presentation.spraying

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kapilagro.sasyak.worker.AttachUrlWorker
import com.kapilagro.sasyak.worker.FileUploadWorker
import com.kapilagro.sasyak.worker.TaskUploadWorker
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.data.db.dao.PreviewDao
import com.kapilagro.sasyak.data.db.entities.PreviewEntity
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.SprayingDetails
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskResponce
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import javax.inject.Inject
import kotlinx.serialization.encodeToString

@HiltViewModel
class SprayingListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val previewDao: PreviewDao
) : ViewModel() {

    private val _tasksState = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksState: StateFlow<TasksState> = _tasksState.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _taskCount = MutableStateFlow(0)
    val taskCount: StateFlow<Int> = _taskCount.asStateFlow()

    private val _previewData = MutableStateFlow<PreviewEntity?>(null)
    val previewData: StateFlow<PreviewEntity?> = _previewData.asStateFlow()

    private var currentPage = 0
    private var totalItems = 0
    private var isLastPage = false

    init {
        loadLastPreview()
    }

    fun loadSprayingTasks(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 0
            isLastPage = false
            _refreshing.value = true
        }

        if (isLastPage && !refresh) return

        viewModelScope.launch(ioDispatcher) {
            if (currentPage == 0) {
                _tasksState.value = TasksState.Loading
            }

            try {
                when (val response = taskRepository.getTasksByType("SPRAYING", currentPage, 10)) {
                    is ApiResponse.Success -> {
                        val (tasks, total) = response.data
                        totalItems = total
                        _taskCount.value = total

                        if (refresh || currentPage == 0) {
                            _tasksState.value = TasksState.Success(tasks, isLastPage = (currentPage + 1) * 10 >= totalItems)
                        } else {
                            val currentTasks = (_tasksState.value as? TasksState.Success)?.tasks ?: emptyList()
                            _tasksState.value = TasksState.Success(currentTasks + tasks, isLastPage = (currentPage + 1) * 10 >= totalItems)
                        }

                        isLastPage = tasks.isEmpty() || (currentPage + 1) * 10 >= totalItems
                        currentPage++
                    }
                    is ApiResponse.Error -> {
                        _tasksState.value = TasksState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        // Already handling loading state
                    }
                }
            } catch (e: Exception) {
                _tasksState.value = TasksState.Error(e.message ?: "Unknown error")
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun refreshTasks() {
        loadSprayingTasks(true)
    }

    sealed class TasksState {
        object Loading : TasksState()
        data class Success(val tasks: List<Task>, val isLastPage: Boolean) : TasksState()
        data class Error(val message: String) : TasksState()
    }

    fun loadMoreTasks() {
        if (isLastPage || _tasksState.value !is TasksState.Success) return

        loadSprayingTasks(false)
    }

    // Add task creation functionality
    private val _createSprayingState = MutableStateFlow<CreateSprayingState>(CreateSprayingState.Idle)
    val createSprayingState: StateFlow<CreateSprayingState> = _createSprayingState.asStateFlow()

    fun createSprayingTask(
        sprayingDetails: SprayingDetails,
        description: String,
        imagesJson:List<String>,
        assignedToId: Int? = null
    ) {
        val previewEntity = PreviewEntity(
            taskType = "SPRAYING",
            valueName = sprayingDetails.valveName ?: "",
            cropName = sprayingDetails.cropName,
            row = sprayingDetails.row
        )
        viewModelScope.launch(ioDispatcher) {
            previewDao.insertPreview(previewEntity)
        }
        _createSprayingState.value = CreateSprayingState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val detailsJson = Json.encodeToString(sprayingDetails)
                val imagesJson= Json.encodeToString(imagesJson)

                when (val response = taskRepository.createTask(
                    taskType = "SPRAYING",
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = imagesJson,
                    assignedToId = assignedToId
                )) {
                    is ApiResponse.Success -> {
                        _createSprayingState.value = CreateSprayingState.Success(response.data)
                        //refreshTasks()
                    }
                    is ApiResponse.Error -> {
                        _createSprayingState.value = CreateSprayingState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _createSprayingState.value = CreateSprayingState.Loading
                    }
                }
            } catch (e: Exception) {
                _createSprayingState.value = CreateSprayingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun workerSprayingTask(
        context: Context,
        sprayingDetails: SprayingDetails,
        description: String,
        imagesJson: List<String>? = null,
        assignedToId: Int? = null
    ) {
        val previewEntity = PreviewEntity(
            taskType = "SPRAYING",
            valueName = sprayingDetails.valveName ?: "",
            cropName = sprayingDetails.cropName,
            row = sprayingDetails.row
        )

        // Save for preview (non-blocking)
        viewModelScope.launch(ioDispatcher) {
            previewDao.insertPreview(previewEntity)
        }

        // UI feedback //  TODO()
        _createSprayingState.value = CreateSprayingState.Loading

        // ------------------ Step 1: TaskUploadWorker ------------------
        val taskUploadData = workDataOf(
            "taskType" to "SPRAYING",
            "description" to description,
            "imagesJson" to Json.encodeToString(imagesJson),
            "detailsJson" to Json.encodeToString(sprayingDetails),
            "assignedToId" to assignedToId
        )
        Log.d("WORKER", "TaskUploadData: $taskUploadData")

        val taskUploadRequest = OneTimeWorkRequestBuilder<TaskUploadWorker>()
            .setInputData(taskUploadData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        // ------------------ Step 2: FileUploadWorker ------------------
        val fileUploadData = workDataOf(
            "image_paths_input" to imagesJson?.toTypedArray(),
            "folder_input" to "SPRAYING",
            "enqueued_at" to System.currentTimeMillis()
        )

        val fileUploadRequest = OneTimeWorkRequestBuilder<FileUploadWorker>()
            .setInputData(fileUploadData)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        // ------------------ Step 3: AttachUrlWorker ------------------
        val attachUrlRequest = OneTimeWorkRequestBuilder<AttachUrlWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        // ------------------ Chain them ------------------
        WorkManager.getInstance(context)
            .beginWith(taskUploadRequest)
            .then(fileUploadRequest)
            .then(attachUrlRequest)
            .enqueue()

    }

    fun clearCreateSprayingState() {
        _createSprayingState.value = CreateSprayingState.Idle
    }

    sealed class CreateSprayingState {
        object Idle : CreateSprayingState()
        object Loading : CreateSprayingState()
        data class Success(val task: TaskResponce) : CreateSprayingState()
        data class Error(val message: String) : CreateSprayingState()
    }

    fun loadLastPreview() {
        viewModelScope.launch {
            val lastTask = previewDao.getLastPreviewByType("SPRAYING")
            _previewData.value = lastTask
        }
    }
}