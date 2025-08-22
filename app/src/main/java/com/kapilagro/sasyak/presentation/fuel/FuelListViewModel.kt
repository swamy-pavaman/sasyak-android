package com.kapilagro.sasyak.presentation.fuel

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
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.FuelDetails
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
import com.kapilagro.sasyak.data.db.dao.WorkerDao
import com.kapilagro.sasyak.data.db.entities.WorkJobEntity

@HiltViewModel
class FuelListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val workerDao: WorkerDao
) : ViewModel() {

    private val _tasksState = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksState: StateFlow<TasksState> = _tasksState.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _taskCount = MutableStateFlow(0)
    val taskCount: StateFlow<Int> = _taskCount.asStateFlow()

    private var currentPage = 0
    private var totalItems = 0
    private var isLastPage = false

    fun loadFuelTasks(refresh: Boolean = false) {
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
                when (val response = taskRepository.getTasksByType("FUEL", currentPage, 10)) {
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
        loadFuelTasks(true)
    }

    sealed class TasksState {
        object Loading : TasksState()
        data class Success(val tasks: List<Task>, val isLastPage: Boolean) : TasksState()
        data class Error(val message: String) : TasksState()
    }

    fun loadMoreTasks() {
        if (isLastPage || _tasksState.value !is TasksState.Success) return

        loadFuelTasks(false)
    }

    // Add task creation functionality
    private val _createFuelState = MutableStateFlow<CreateFuelState>(CreateFuelState.Idle)
    val createFuelState: StateFlow<CreateFuelState> = _createFuelState.asStateFlow()

    fun createFuelTask(
        fuelDetails: FuelDetails,
        description: String,
        imagesJson:List<String>?=null,
        assignedToId : Int? =null
    ) {
        _createFuelState.value = CreateFuelState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val detailsJson = Json.encodeToString(fuelDetails)
                val imagesJson= Json.encodeToString(imagesJson)

                when (val response = taskRepository.createTask(
                    taskType = "FUEL",
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = imagesJson,
                    assignedToId = assignedToId
                )) {
                    is ApiResponse.Success -> {
                        _createFuelState.value = CreateFuelState.Success(response.data)
                        // Refresh the task list after successful creation
                        //refreshTasks()
                    }
                    is ApiResponse.Error -> {
                        _createFuelState.value = CreateFuelState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _createFuelState.value = CreateFuelState.Loading
                    }
                }
            } catch (e: Exception) {
                _createFuelState.value = CreateFuelState.Error(e.message ?: "Unknown error")
            }
        }
    }
    fun workerFuelTask(
        context: Context,
        fuelDetails: FuelDetails,
        description: String,
        imagesJson: List<String>? = null,
        assignedToId: Int? = null
    ) {

        // UI feedback //  TODO()
        _createFuelState.value = CreateFuelState.Loading

        // ------------------ Step 1: TaskUploadWorker ------------------
        val taskUploadData = workDataOf(
            "taskType" to "FUEL",
            "description" to description,
            "imagesJson" to Json.encodeToString(imagesJson),
            "detailsJson" to Json.encodeToString(fuelDetails),
            "assignedToId" to assignedToId
        )
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val folder = buildString {
            append("FUEL")
            val number = fuelDetails.vehicleNumber
            if (!number.isNullOrBlank()) {
                append("/$number")
            }
        }
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

        val workRequest = WorkJobEntity(
            workId = taskUploadRequest.id,
            taskType = "FUEL",
            description = description,
            enqueuedAt = System.currentTimeMillis()
        )
        viewModelScope.launch(ioDispatcher) {
            workerDao.insert(workRequest)
        }

        // ------------------ Step 2: FileUploadWorker ------------------
        val fileUploadData = workDataOf(
            "image_paths_input" to imagesJson?.toTypedArray(),
            "folder_input" to folder,
            "enqueued_at" to System.currentTimeMillis()
        )
        Log.d("WORKER", "FileUploadData: $fileUploadData")

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

    }

    fun updateToWorker(data:WorkJobEntity){
        viewModelScope.launch(ioDispatcher) {
            workerDao.insert(data)
        }
    }

    fun clearCreateFuelState() {
        _createFuelState.value = CreateFuelState.Idle
    }

    sealed class CreateFuelState {
        object Idle : CreateFuelState()
        object Loading : CreateFuelState()
        data class Success(val task: TaskResponce) : CreateFuelState()
        data class Error(val message: String) : CreateFuelState()
    }
}