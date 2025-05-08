package com.kapilagro.sasyak.presentation.scouting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.ScoutingDetails
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
class ScoutingListViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _tasksState = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksState: StateFlow<TasksState> = _tasksState.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private var currentPage = 0
    private var totalItems = 0
    private var isLastPage = false

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
        description: String
    ) {
        _createScoutingState.value = CreateScoutingState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
//                val detailsJson = Json.encodeToString(scoutingDetails)
                val detailsJson = Json.encodeToString(scoutingDetails)

                when (val response = taskRepository.createTask(
                    taskType = "SCOUTING",
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = null,  // TODO: Handle file uploads
                    assignedToId = null
                )) {
                    is ApiResponse.Success -> {
                        _createScoutingState.value = CreateScoutingState.Success(response.data)
                        // Refresh the task list after successful creation
                        refreshTasks()
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

    fun clearCreateScoutingState() {
        _createScoutingState.value = CreateScoutingState.Idle
    }

    // Add this sealed class inside your existing TasksState sealed class:
    sealed class CreateScoutingState {
        object Idle : CreateScoutingState()
        object Loading : CreateScoutingState()
        data class Success(val task: Task) : CreateScoutingState()
        data class Error(val message: String) : CreateScoutingState()
    }
}