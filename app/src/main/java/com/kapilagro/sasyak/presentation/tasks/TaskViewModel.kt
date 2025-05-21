package com.kapilagro.sasyak.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.data.api.models.responses.SupervisorListResponse
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import com.kapilagro.sasyak.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskAdviceRepository: TaskAdviceRepository,
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
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

    private val _addAdviceState = MutableStateFlow<AddAdviceState>(AddAdviceState.Idle)
    val addAdviceState: StateFlow<AddAdviceState> = _addAdviceState.asStateFlow()

    private val _selectedTab = MutableStateFlow(TaskTab.ASSIGNED)
    val selectedTab: StateFlow<TaskTab> = _selectedTab.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _supervisorsListState = MutableStateFlow<SupervisorsListState>(SupervisorsListState.Idle)
    val supervisorsListState: StateFlow<SupervisorsListState> = _supervisorsListState.asStateFlow()

    fun getCurrentUserRole() {
        viewModelScope.launch {
            authRepository.getUserRole().collect { role ->
                _userRole.value = role
            }
        }
    }

    fun loadSupervisorsList() {
        _supervisorsListState.value = SupervisorsListState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = userRepository.getSupervisorsList()) {
                is ApiResponse.Success -> {
                    _supervisorsListState.value = SupervisorsListState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    _supervisorsListState.value = SupervisorsListState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _supervisorsListState.value = SupervisorsListState.Loading
                }
            }
        }
    }

    fun onTabSelected(tab: TaskTab) {
        _selectedTab.value = tab
        loadTasks(0, 10)
    }

    fun loadTasks(page: Int, size: Int) {
        _taskListState.value = TaskListState.Loading
        viewModelScope.launch(ioDispatcher) {
            val response = when (_selectedTab.value) {
                TaskTab.SUPERVISORS -> taskRepository.getTasksBySupervisors(page, size)
                TaskTab.ME -> taskRepository.getCreatedTasks(page, size)
                TaskTab.ASSIGNED -> taskRepository.getAssignedTasks(page, size)
                TaskTab.BY_STATUS -> taskRepository.getTasksByStatus("all", page, size)
                TaskTab.CREATED -> taskRepository.getCreatedTasks(page, size)
            }

            when (response) {
                is ApiResponse.Success -> {
                    _taskListState.value = TaskListState.Success(
                        tasks = response.data.first,
                        totalCount = response.data.second,
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
                    loadTasks(0, 10)
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

    fun updateTaskStatus(taskId: Int, status: String) {
        _updateTaskState.value = UpdateTaskState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.updateTaskStatus(taskId, status, null)) {
                is ApiResponse.Success -> {
                    _updateTaskState.value = UpdateTaskState.Success(response.data)
                    loadTaskDetail(taskId)
                    loadTasks(0, 10)
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

    fun addTaskImplementation(taskId: Int, implementationText: String) {
        _updateTaskState.value = UpdateTaskState.Loading
        viewModelScope.launch(ioDispatcher) {
            val implementationJson = JSONObject().apply {
                put("text", implementationText)
                put("timestamp", System.currentTimeMillis())
            }.toString()

            when (val response = taskRepository.updateTaskImplementation(taskId, implementationJson)) {
                is ApiResponse.Success -> {
                    _updateTaskState.value = UpdateTaskState.Success(response.data)
                    loadTaskDetail(taskId)
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
        _addAdviceState.value = AddAdviceState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskAdviceRepository.createTaskAdvice(taskId, adviceText)) {
                is ApiResponse.Success -> {
                    _addAdviceState.value = AddAdviceState.Success
                    loadTaskDetail(taskId)
                }
                is ApiResponse.Error -> {
                    _addAdviceState.value = AddAdviceState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _addAdviceState.value = AddAdviceState.Loading
                }
            }
        }
    }

    fun clearCreateTaskState() {
        _createTaskState.value = CreateTaskState.Idle
    }

    fun clearUpdateTaskState() {
        _updateTaskState.value = UpdateTaskState.Idle
    }

    fun clearAddAdviceState() {
        _addAdviceState.value = AddAdviceState.Idle
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

    sealed class AddAdviceState {
        object Idle : AddAdviceState()
        object Loading : AddAdviceState()
        object Success : AddAdviceState()
        data class Error(val message: String) : AddAdviceState()
    }

    sealed class SupervisorsListState {
        object Idle : SupervisorsListState()
        object Loading : SupervisorsListState()
        data class Success(val supervisors: List<SupervisorListResponse>) : SupervisorsListState()
        data class Error(val message: String) : SupervisorsListState()
    }

    enum class TaskTab {
        SUPERVISORS, ME, ASSIGNED, BY_STATUS, CREATED
    }
}