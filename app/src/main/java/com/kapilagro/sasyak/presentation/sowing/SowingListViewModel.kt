package com.kapilagro.sasyak.presentation.sowing

import android.R.string
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.SowingDetails
import com.kapilagro.sasyak.domain.models.Task
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
class SowingListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
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

    fun loadSowingTasks(refresh: Boolean = false) {
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
                when (val response = taskRepository.getTasksByType("SOWING", currentPage, 10)) {
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
        loadSowingTasks(true)
    }

    sealed class TasksState {
        object Loading : TasksState()
        data class Success(val tasks: List<Task>, val isLastPage: Boolean) : TasksState()
        data class Error(val message: String) : TasksState()
    }

    fun loadMoreTasks() {
        if (isLastPage || _tasksState.value !is TasksState.Success) return

        loadSowingTasks(false)
    }

    // Add task creation functionality
    private val _createSowingState = MutableStateFlow<CreateSowingState>(CreateSowingState.Idle)
    val createSowingState: StateFlow<CreateSowingState> = _createSowingState.asStateFlow()

//    fun createSowingTask(
//        sowingDetails: SowingDetails,
//        description: String,
//        assignedToId: Int? =null
//    ) {
//        _createSowingState.value = CreateSowingState.Loading
//        viewModelScope.launch(ioDispatcher) {
//            try {
//                val detailsJson = Json.encodeToString(sowingDetails)
//
//                when (val response = taskRepository.createTask(
//                    taskType = "SOWING",
//                    description = description,
//                    detailsJson = detailsJson,
//                    imagesJson = null,  // TODO: Handle file uploads
//                    assignedToId = assignedToId
//                )) {
//                    is ApiResponse.Success -> {
//                        _createSowingState.value = CreateSowingState.Success(response.data)
//                        // Refresh the task list after successful creation
//                        refreshTasks()
//                    }
//                    is ApiResponse.Error -> {
//                        _createSowingState.value = CreateSowingState.Error(response.errorMessage)
//                    }
//                    is ApiResponse.Loading -> {
//                        _createSowingState.value = CreateSowingState.Loading
//                    }
//                }
//            } catch (e: Exception) {
//                _createSowingState.value = CreateSowingState.Error(e.message ?: "Unknown error")
//            }
//        }
//    }


    // File: app/src/main/java/com/kapilagro/sasyak/presentation/sowing/SowingListViewModel.kt
    fun createSowingTask(
        sowingDetails: SowingDetails,
        description: String,
        imagesJson:List<String>,
        assignedToId: Int? = null
    ) {
        _createSowingState.value = CreateSowingState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val detailsJson = Json.encodeToString(sowingDetails)
                val imagesJson = Json.encodeToString(imagesJson)

                when (val response = taskRepository.createTask(
                    taskType = "SOWING",
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = imagesJson,
                    assignedToId = assignedToId
                )) {
                    is ApiResponse.Success -> {
                        _createSowingState.value = CreateSowingState.Success(response.data)
                        //refreshTasks()
                    }
                    is ApiResponse.Error -> {
                        _createSowingState.value = CreateSowingState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _createSowingState.value = CreateSowingState.Loading
                    }
                }
            } catch (e: Exception) {
                _createSowingState.value = CreateSowingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearCreateSowingState() {
        _createSowingState.value = CreateSowingState.Idle
    }

    sealed class CreateSowingState {
        object Idle : CreateSowingState()
        object Loading : CreateSowingState()
        data class Success(val task: Task) : CreateSowingState()
        data class Error(val message: String) : CreateSowingState()
    }
}