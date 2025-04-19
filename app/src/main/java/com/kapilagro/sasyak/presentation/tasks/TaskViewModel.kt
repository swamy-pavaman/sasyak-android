package com.kapilagro.sasyak.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskAdviceRepository: TaskAdviceRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _taskListState = MutableStateFlow<TaskListState>(TaskListState.Loading)
    val taskListState: StateFlow<TaskListState> = _taskListState.asStateFlow()

    private val _taskDetailState = MutableStateFlow<TaskDetailState>(TaskDetailState.Loading)
    val taskDetailState: StateFlow<TaskDetailState> = _taskDetailState.asStateFlow()

    private val _createTaskState = MutableStateFlow<CreateTaskState>(CreateTaskState.Idle)
    val createTaskState: StateFlow<CreateTaskState> = _createTaskState.asStateFlow()

    private val _updateTaskState = MutableStateFlow<UpdateTaskState>(UpdateTaskState.Idle)
    val updateTaskState: StateFlow<UpdateTaskState> = _updateTaskState.asStateFlow()

    private val _selectedTab = MutableStateFlow(TaskTab.PENDING)
    val selectedTab: StateFlow<TaskTab> = _selectedTab.asStateFlow()

    fun onTabSelected(tab: TaskTab) {
        _selectedTab.value = tab
        loadTasks(0, 10)
    }

    fun loadTasks(page: Int, size: Int) {
        _taskListState.value = TaskListState.Loading
        viewModelScope.launch(ioDispatcher) {
            val response = when (_selectedTab.value) {
                TaskTab.PENDING, TaskTab.APPROVED, TaskTab.REJECTED -> {
                    taskRepository.getAssignedTasks(page, size)
                }
            }

            when (response) {
                is ApiResponse.Success -> {
                    val filteredTasks = response.data.first.filter { task ->
                        when (_selectedTab.value) {
                            TaskTab.PENDING -> task.status.equals("pending", ignoreCase = true)
                            TaskTab.APPROVED -> task.status.equals("approved", ignoreCase = true)
                            TaskTab.REJECTED -> task.status.equals("rejected", ignoreCase = true)
                        }
                    }
                    _taskListState.value = TaskListState.Success(
                        tasks = filteredTasks,
                        totalCount = filteredTasks.size,
                        hasMore = response.data.second > (page + 1) * size
                    )
                }
                is ApiResponse.Error -> {
                    _taskListState.value = TaskListState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _taskListState.value = TaskListState.Loading
                }
            }
        }
    }

    fun loadTaskDetail(taskId: Int) {
        _taskDetailState.value = TaskDetailState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.getTaskById(taskId)) {
                is ApiResponse.Success -> {
                    _taskDetailState.value = TaskDetailState.Success(
                        task = response.data.first,
                        advices = response.data.second
                    )
                }
                is ApiResponse.Error -> {
                    _taskDetailState.value = TaskDetailState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _taskDetailState.value = TaskDetailState.Loading
                }
            }
        }
    }

    fun createTask(
        taskType: String,
        description: String,
        detailsJson: String? = null,
        imagesJson: String? = null,
        assignedToId: Int? = null
    ) {
        _createTaskState.value = CreateTaskState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.createTask(
                taskType = taskType,
                description = description,
                detailsJson = detailsJson,
                imagesJson = imagesJson,
                assignedToId = assignedToId
            )) {
                is ApiResponse.Success -> {
                    _createTaskState.value = CreateTaskState.Success(response.data)
                    loadTasks(0, 10) // Refresh task list
                }
                is ApiResponse.Error -> {
                    _createTaskState.value = CreateTaskState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _createTaskState.value = CreateTaskState.Loading
                }
            }
        }
    }

    fun updateTaskStatus(taskId: Int, status: String, comment: String? = null) {
        _updateTaskState.value = UpdateTaskState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.updateTaskStatus(taskId, status, comment)) {
                is ApiResponse.Success -> {
                    _updateTaskState.value = UpdateTaskState.Success(response.data)
                    loadTaskDetail(taskId) // Refresh task detail
                    loadTasks(0, 10) // Refresh task list
                }
                is ApiResponse.Error -> {
                    _updateTaskState.value = UpdateTaskState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _updateTaskState.value = UpdateTaskState.Loading
                }
            }
        }
    }

    fun updateTaskImplementation(taskId: Int, implementationJson: String) {
        _updateTaskState.value = UpdateTaskState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.updateTaskImplementation(taskId, implementationJson)) {
                is ApiResponse.Success -> {
                    _updateTaskState.value = UpdateTaskState.Success(response.data)
                    loadTaskDetail(taskId) // Refresh task detail
                }
                is ApiResponse.Error -> {
                    _updateTaskState.value = UpdateTaskState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _updateTaskState.value = UpdateTaskState.Loading
                }
            }
        }
    }

    fun addTaskAdvice(taskId: Int, adviceText: String) {
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskAdviceRepository.createTaskAdvice(taskId, adviceText)) {
                is ApiResponse.Success -> {
                    loadTaskDetail(taskId) // Refresh task detail to show new advice
                }
                is ApiResponse.Error -> {
                    // Handle error
                }
                else -> {}
            }
        }
    }

    fun clearCreateTaskState() {
        _createTaskState.value = CreateTaskState.Idle
    }

    fun clearUpdateTaskState() {
        _updateTaskState.value = UpdateTaskState.Idle
    }

    sealed class TaskListState {
        object Loading : TaskListState()
        data class Success(
            val tasks: List<Task>,
            val totalCount: Int,
            val hasMore: Boolean
        ) : TaskListState()
        data class Error(val message: String) : TaskListState()
    }

    sealed class TaskDetailState {
        object Loading : TaskDetailState()
        data class Success(val task: Task, val advices: List<TaskAdvice>) : TaskDetailState()
        data class Error(val message: String) : TaskDetailState()
    }

    sealed class CreateTaskState {
        object Idle : CreateTaskState()
        object Loading : CreateTaskState()
        data class Success(val task: Task) : CreateTaskState()
        data class Error(val message: String) : CreateTaskState()
    }

    sealed class UpdateTaskState {
        object Idle : UpdateTaskState()
        object Loading : UpdateTaskState()
        data class Success(val task: Task) : UpdateTaskState()
        data class Error(val message: String) : UpdateTaskState()
    }

    enum class TaskTab {
        PENDING, APPROVED, REJECTED
    }
}