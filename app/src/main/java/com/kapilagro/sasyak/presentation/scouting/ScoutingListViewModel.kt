package com.kapilagro.sasyak.presentation.scouting

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.kapilagro.sasyak.data.db.dao.PreviewDao
import com.kapilagro.sasyak.data.db.entities.PreviewEntity
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.ScoutingDetails
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskResponce
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import com.kapilagro.sasyak.worker.AttachUrlWorker
import com.kapilagro.sasyak.worker.FileUploadWorker
import com.kapilagro.sasyak.worker.TaskUploadWorker
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
class ScoutingListViewModel @Inject constructor(
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

    fun loadScoutingTasks(refresh: Boolean = false) {
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
                when (val response = taskRepository.getTasksByType("SCOUTING", currentPage, 10)) {
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
        loadScoutingTasks(true)
    }

    sealed class TasksState {
        object Loading : TasksState()
        data class Success(val tasks: List<Task>, val isLastPage: Boolean) : TasksState()
        data class Error(val message: String) : TasksState()
    }
    fun loadMoreTasks() {
        if (isLastPage || _tasksState.value !is TasksState.Success) return

        loadScoutingTasks(false)
    }


    // Add these properties and methods to your existing ScoutingListViewModel class:

    private val _createScoutingState = MutableStateFlow<CreateScoutingState>(CreateScoutingState.Idle)
    val createScoutingState: StateFlow<CreateScoutingState> = _createScoutingState.asStateFlow()

    fun createScoutingTask(
        scoutingDetails: ScoutingDetails,
        description: String,
        imagesJson:List<String>?=null,
        assignedToId:Int?=null
    ) {
        val previewEntity = PreviewEntity(
            taskType = "SCOUTING",
            valueName = scoutingDetails.valveName,
            cropName = scoutingDetails.cropName,
            row = scoutingDetails.row,
            treeNo = scoutingDetails.treeNo
        )
        viewModelScope.launch(ioDispatcher) {
            previewDao.insertPreview(previewEntity)
        }
        _createScoutingState.value = CreateScoutingState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val detailsJson = Json.encodeToString(scoutingDetails)
                val imagesJson = Json.encodeToString(imagesJson)

                when (val response = taskRepository.createTask(
                    taskType = "SCOUTING",
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = imagesJson,
                    assignedToId = assignedToId
                )) {
                    is ApiResponse.Success -> {
                        _createScoutingState.value = CreateScoutingState.Success(response.data)
                    }
                    is ApiResponse.Error -> {
                        _createScoutingState.value = CreateScoutingState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _createScoutingState.value = CreateScoutingState.Loading
                    }
                }
            } catch (e: Exception) {
                _createScoutingState.value = CreateScoutingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun workerScoutingTask(
        context: Context,
        scoutingDetails: ScoutingDetails,
        description: String,
        imagesJson: List<String>? = null,
        assignedToId: Int? = null
    ) {
        Log.d("WORKER", "WorkerScoutingTask called")
        val previewEntity = PreviewEntity(
            taskType = "SCOUTING",
            valueName = scoutingDetails.valveName,
            cropName = scoutingDetails.cropName,
            row = scoutingDetails.row,
            treeNo = scoutingDetails.treeNo
        )
        val folder = buildString {
            append("SCOUTING/")
            append(scoutingDetails.cropName)

            val category = scoutingDetails.disease
                ?: scoutingDetails.pest
                ?: scoutingDetails.nutrients

            if (!category.isNullOrBlank()) {
                append("/$category")
            }
        }

        // Save for preview (non-blocking)
        viewModelScope.launch(ioDispatcher) {
            previewDao.insertPreview(previewEntity)
        }

        // UI feedback //  TODO()
        _createScoutingState.value = CreateScoutingState.Loading

        // ------------------ Step 1: TaskUploadWorker ------------------
        val taskUploadData = workDataOf(
            "taskType" to "SCOUTING",
            "description" to description,
            "imagesJson" to Json.encodeToString(imagesJson),
            "detailsJson" to Json.encodeToString(scoutingDetails),
            "assignedToId" to assignedToId
        )
        Log.d("WORKER", "TaskUploadData: $taskUploadData")

        val taskUploadRequest = OneTimeWorkRequestBuilder<TaskUploadWorker>()
            .setInputData(taskUploadData)
            .addTag(TaskUploadWorker.UPLOAD_TAG)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        // ------------------ Step 2: FileUploadWorker ------------------
        val fileUploadData = workDataOf(
            "image_paths_input" to imagesJson?.toTypedArray(),
            "folder_input" to folder,
            "enqueued_at" to System.currentTimeMillis()
        )

        val fileUploadRequest = OneTimeWorkRequestBuilder<FileUploadWorker>()
            .setInputData(fileUploadData)
            .addTag(FileUploadWorker.UPLOAD_TAG)
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

        // Optional: show pending state to UI immediately
        //_createScoutingState.value = CreateScoutingState.Success(TaskResponce(0, "Pending", "Task is queued for upload"))
    }



    fun clearCreateScoutingState() {
        _createScoutingState.value = CreateScoutingState.Idle
    }

    // Add this sealed class inside your existing TasksState sealed class:
    sealed class CreateScoutingState {
        object Idle : CreateScoutingState()
        object Loading : CreateScoutingState()
        data class Success(val task: TaskResponce) : CreateScoutingState()
        data class Error(val message: String) : CreateScoutingState()
    }
    fun loadLastPreview() {
        viewModelScope.launch {
            val lastTask = previewDao.getLastPreviewByType("SCOUTING")
            _previewData.value = lastTask
        }
    }
}