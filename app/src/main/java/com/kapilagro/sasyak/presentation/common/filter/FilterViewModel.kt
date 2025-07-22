package com.kapilagro.sasyak.presentation.common.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilterViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _tasksStateOfApproved = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksStateOfApproved: StateFlow<TasksState> = _tasksStateOfApproved.asStateFlow()

    private val _tasksStateOfRejected = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksStateOfRejected: StateFlow<TasksState> = _tasksStateOfRejected.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    private val _approvedTaskCount = MutableStateFlow(0)
    val ApprovedTaskCount: StateFlow<Int> = _approvedTaskCount.asStateFlow()

    private val _rejectedTaskCount = MutableStateFlow(0)
    val RejectedTaskCount: StateFlow<Int> = _rejectedTaskCount.asStateFlow()

    // Map to store pagination state for each tab
    private data class PaginationState(
        var currentPage: Int = 0,
        var totalItems: Int = 0,
        var isLastPage: Boolean = false
    )

    private val paginationStates = mutableMapOf(
        "approved" to PaginationState(),
        "rejected" to PaginationState()
    )

    fun pagesToZero(status: String) {
        val paginationState = paginationStates[status] ?: return
        paginationState.currentPage = 0
        paginationState.totalItems = 0
        paginationState.isLastPage = false
    }

    fun loadTasksByFilter(
        status: String,
        taskType: String,
        refresh: Boolean = false
    ) {
        val paginationState = paginationStates[status] ?: return
        val _tasksState = if (status == "rejected") {
            _tasksStateOfRejected
        } else {
            _tasksStateOfApproved
        }

        if (refresh) {
            pagesToZero(status)
            _refreshing.value = true
        }

        // Avoid loading again if already at last page
        if (paginationState.isLastPage && !refresh) return

        viewModelScope.launch(ioDispatcher) {
            if (paginationState.currentPage == 0) {
                _tasksState.value = TasksState.Loading
            }

            try {
                when (val response = taskRepository.getTasksByFilter(
                    status = status,
                    taskType = taskType,
                    page = paginationState.currentPage,
                    size = 10,
                    currentUserTasks = true
                )) {
                    is ApiResponse.Success -> {
                        val (tasks, total) = response.data
                        paginationState.totalItems = total
                        if (status == "rejected") {
                            _rejectedTaskCount.value = total
                        } else {
                            _approvedTaskCount.value = total
                        }

                        val allTasks = if (refresh || paginationState.currentPage == 0) {
                            tasks
                        } else {
                            val existing = (_tasksState.value as? TasksState.Success)?.tasks ?: emptyList()
                            existing + tasks
                        }

                        paginationState.isLastPage = tasks.isEmpty() || (paginationState.currentPage + 1) * 10 >= paginationState.totalItems
                        _tasksState.value = TasksState.Success(allTasks, paginationState.isLastPage)
                        paginationState.currentPage++
                    }

                    is ApiResponse.Error -> {
                        _tasksState.value = TasksState.Error(response.errorMessage)
                    }

                    is ApiResponse.Loading -> {
                        // Already handled
                    }
                }
            } catch (e: Exception) {
                _tasksState.value = TasksState.Error("Error loading tasks: ${e.message}")
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun refreshTasks(status: String, taskType: String) {
        loadTasksByFilter(status, taskType, refresh = true)
    }

    fun loadMoreTasksOfApproved() {
        val paginationState = paginationStates["approved"] ?: return
        if (paginationState.isLastPage || _tasksStateOfApproved.value !is TasksState.Success) return
        loadTasksByFilter(status = "approved", taskType = "SCOUTING", refresh = false)
    }

    fun loadMoreTasksOfRejected() {
        val paginationState = paginationStates["rejected"] ?: return
        if (paginationState.isLastPage || _tasksStateOfRejected.value !is TasksState.Success) return
        loadTasksByFilter(status = "rejected", taskType = "SCOUTING", refresh = false)
    }

    sealed class TasksState {
        object Loading : TasksState()
        data class Success(val tasks: List<Task>, val isLastPage: Boolean) : TasksState()
        data class Error(val message: String) : TasksState()
    }
}