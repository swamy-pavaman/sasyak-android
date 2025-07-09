package com.kapilagro.sasyak.presentation.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.data.api.models.responses.TeamMemberResponse
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val taskAdviceRepository: TaskAdviceRepository,
    private val authRepository: AuthRepository,
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

    private val _selectedTab = MutableStateFlow(TaskTab.SUPERVISORS) // Default to SUPERVISORS
    val selectedTab: StateFlow<TaskTab> = _selectedTab.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    // Task counts for each tab
    private val _supervisorTaskCount = MutableStateFlow(0)
    val supervisorTaskCount: StateFlow<Int> = _supervisorTaskCount.asStateFlow()

    private val _createdTaskCount = MutableStateFlow(0)
    val createdTaskCount: StateFlow<Int> = _createdTaskCount.asStateFlow()

    private val _assignedTaskCount = MutableStateFlow(0)
    val assignedTaskCount: StateFlow<Int> = _assignedTaskCount.asStateFlow()

    // Admin-specific states
    private val _managersList = MutableStateFlow<List<TeamMemberResponse>>(emptyList())
    val managersList: StateFlow<List<TeamMemberResponse>> = _managersList.asStateFlow()

    private val _supervisorsList = MutableStateFlow<List<TeamMemberResponse>>(emptyList())
    val supervisorsList: StateFlow<List<TeamMemberResponse>> = _supervisorsList.asStateFlow()

    private val _selectedUserId = MutableStateFlow<Int?>(null)
    val selectedUserId: StateFlow<Int?> = _selectedUserId.asStateFlow()

    private var currentPage = 0
    private val pageSize = 10
    private val accumulatedTasks = mutableListOf<Task>()

    init {
        getCurrentUserRole()
        if (_userRole.value == "ADMIN") {
            fetchManagers()
            fetchSupervisors()
        }
    }

    fun getCurrentUserRole() {
        viewModelScope.launch {
            authRepository.getUserRole().collect { role ->
                _userRole.value = role
                // Set default tab based on role
                val newTab = when (role) {
                    "MANAGER" -> TaskTab.ME
                    "SUPERVISOR" -> TaskTab.ASSIGNED // Default to ASSIGNED for supervisors
                    "ADMIN" -> TaskTab.MANAGERS
                    else -> TaskTab.SUPERVISORS
                }
                if (_selectedTab.value != newTab) {
                    _selectedTab.value = newTab
                    accumulatedTasks.clear()
                    currentPage = 0
                    loadTasks(currentPage, pageSize)
                }
            }
        }
    }

    // New function to reset tab when "Tasks" is selected in bottom navigation
    fun resetTabForNavigation() {
        viewModelScope.launch {
            val role = _userRole.value
            val newTab = when (role) {
                "MANAGER" -> TaskTab.ME
                "SUPERVISOR" -> TaskTab.ASSIGNED
                "ADMIN" -> TaskTab.MANAGERS
                else -> TaskTab.SUPERVISORS
            }
            if (_selectedTab.value != newTab) {
                _selectedTab.value = newTab
                accumulatedTasks.clear()
                currentPage = 0
                loadTasks(currentPage, pageSize)
            }
        }
    }

    fun onTabSelected(tab: TaskTab) {
        if (_selectedTab.value != tab) {
            _selectedTab.value = tab
            _selectedUserId.value = null
            accumulatedTasks.clear()
            currentPage = 0
            loadTasks(currentPage, pageSize)
        }
    }

    fun fetchManagers() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val response = taskRepository.getUsersByRole("MANAGER") // Implement in TaskRepository
                when (response) {
                    is ApiResponse.Success -> {
                        _managersList.value = response.data.employees
                    }
                    is ApiResponse.Error -> {
                        // Handle error (e.g., show toast in UI)
                    }
                    is ApiResponse.Loading -> {}
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun fetchSupervisors() {
        viewModelScope.launch(ioDispatcher) {
            try {
                val response = taskRepository.getUsersByRole("SUPERVISOR") // Implement in TaskRepository
                when (response) {
                    is ApiResponse.Success -> {
                        _supervisorsList.value = response.data.employees
                    }
                    is ApiResponse.Error -> {
                        // Handle error
                    }
                    is ApiResponse.Loading -> {}
                }
            } catch (e: Exception) {
                // Handle exception
            }
        }
    }

    fun selectUser(userId: Int) {
        _selectedUserId.value = userId
        accumulatedTasks.clear()
        currentPage = 0
        loadTasks(currentPage, pageSize)
    }

    fun loadTasks(page: Int, size: Int) {
        if (page == 0) {
            accumulatedTasks.clear()
            _taskListState.value = TaskListState.Loading
        }
        _refreshing.value = page == 0
        viewModelScope.launch(ioDispatcher) {
            val response = when (_selectedTab.value) {
                TaskTab.SUPERVISORS -> taskRepository.getTasksBySupervisors(page, size)
                TaskTab.ME -> taskRepository.getCreatedTasks(page, size)
                TaskTab.ASSIGNED -> taskRepository.getAssignedTasks(page, size)
                TaskTab.CREATED -> taskRepository.getCreatedTasks(page, size)
                TaskTab.MANAGERS -> if (_selectedUserId.value != null) taskRepository.getTasksByUserId(_selectedUserId.value!!, page, size) else ApiResponse.Success(Pair(emptyList<Task>(), 0))
                TaskTab.SUPERVISOR_LIST -> if (_selectedUserId.value != null) taskRepository.getTasksByUserId(_selectedUserId.value!!, page, size) else ApiResponse.Success(Pair(emptyList<Task>(), 0))
            }

            when (response) {
                is ApiResponse.Success -> {
                    accumulatedTasks.addAll(response.data.first)
                    when (_selectedTab.value) {
                        TaskTab.SUPERVISORS -> _supervisorTaskCount.value = response.data.second
                        TaskTab.ME -> _createdTaskCount.value = response.data.second
                        TaskTab.ASSIGNED -> _assignedTaskCount.value = response.data.second
                        TaskTab.CREATED -> _createdTaskCount.value = response.data.second
                        TaskTab.MANAGERS -> {} // No count update for Admin tabs
                        TaskTab.SUPERVISOR_LIST -> {} // No count update for Supervisor List tabs
                    }
                    _taskListState.value = TaskListState.Success(
                        tasks = accumulatedTasks.toList(),
                        isLastPage = response.data.second <= (page + 1) * size
                    )
                    _refreshing.value = false
                }
                is ApiResponse.Error -> {
                    _taskListState.value = TaskListState.Error(response.errorMessage)
                    _refreshing.value = false
                }
                is ApiResponse.Loading -> {
                    if (page == 0) {
                        _taskListState.value = TaskListState.Loading
                    }
                }
            }
        }
    }

    fun loadMoreTasks() {
        currentPage++
        loadTasks(currentPage, pageSize)
    }

    fun refreshTasks() {
        currentPage = 0
        loadTasks(currentPage, pageSize)
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
                    refreshTasks()
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
                    refreshTasks()
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

    fun addTaskImplementation(taskId: Int, implementationText: String, imageUrls: List<String> = emptyList(), implementation: String? = null) {
        _updateTaskState.value = UpdateTaskState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                // Parse existing implementations from implementation parameter
                val existingImplementations = implementation?.let {
                    try {
                        if (it == "{}" || it.isEmpty()) {
                            JSONArray()
                        } else {
                            JSONArray(it)
                        }
                    } catch (e: Exception) {
                        // If it's a single JSONObject, wrap it in a JSONArray
                        try {
                            JSONArray().put(JSONObject(it))
                        } catch (e2: Exception) {
                            JSONArray()
                        }
                    }
                } ?: JSONArray()

                // Create new implementation
                val newImplementation = JSONObject().apply {
                    put("text", implementationText)
                    put("timestamp", System.currentTimeMillis())
                    put("imageUrls", JSONArray(imageUrls))
                }

                // Append new implementation to existing ones
                existingImplementations.put(newImplementation)
                val updatedImplementationJson = existingImplementations.toString()

                // Update repository with combined JSON
                when (val response = taskRepository.updateTaskImplementation(taskId, updatedImplementationJson)) {
                    is ApiResponse.Success -> {
                        _updateTaskState.value = UpdateTaskState.Success(response.data)
                        loadTaskDetail(taskId)
                    }
                    is ApiResponse.Error -> {
                        _updateTaskState.value = UpdateTaskState.Error(
                            response.errorMessage ?: "Failed to submit implementation"
                        )
                    }
                    is ApiResponse.Loading -> {
                        // No-op; loading state already set
                    }
                }
            } catch (e: Exception) {
                _updateTaskState.value = UpdateTaskState.Error("Failed to submit implementation: ${e.message}")
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
            val isLastPage: Boolean
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

    enum class TaskTab {
        SUPERVISORS, ME, ASSIGNED, CREATED, MANAGERS , SUPERVISOR_LIST
    }

    data class Implementation(
        val text: String,
        val timestamp: Long,
        val supervisorName: String,
        val imageUrls: List<String>? = null
    )
}