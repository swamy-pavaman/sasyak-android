package com.kapilagro.sasyak.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.data.api.models.responses.SupervisorListResponse
import com.kapilagro.sasyak.domain.repositories.WeatherRepository
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.Task
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.domain.models.WeatherInfo
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import com.kapilagro.sasyak.domain.repositories.TaskRepository
import com.kapilagro.sasyak.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val taskRepository: TaskRepository,
    private val weatherRepository: WeatherRepository,
    private val authRepository: AuthRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _newTasksState = MutableStateFlow<TasksState>(TasksState.Loading)
    val newTasksState: StateFlow<TasksState> =_newTasksState.asStateFlow()

    private val _userState = MutableStateFlow<UserState>(UserState.Loading)
    val userState: StateFlow<UserState> = _userState.asStateFlow()

    private val _weatherState = MutableStateFlow<WeatherState>(WeatherState.Loading)
    val weatherState: StateFlow<WeatherState> = _weatherState.asStateFlow()

    private val _tasksState = MutableStateFlow<TasksState>(TasksState.Loading)
    val tasksState: StateFlow<TasksState> = _tasksState.asStateFlow()
    private val _teamMembersState = MutableStateFlow<TeamMembersState>(TeamMembersState.Loading)
    val teamMembersState: StateFlow<TeamMembersState> = _teamMembersState.asStateFlow()

    private val _supervisorsListState = MutableStateFlow<SupervisorsListState>(SupervisorsListState.Idle)
    val supervisorsListState: StateFlow<SupervisorsListState> = _supervisorsListState.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()
    private val _userId = MutableStateFlow<Int?>(null)
    val userId: StateFlow<Int?> = _userId.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getUserRole().collect { role ->
                _userRole.value = role
            }
        }
        viewModelScope.launch {
            authRepository.getUserId().collect { userId ->
                _userId.value = userId
            }
        }
    }

    init {
        loadUserData()
        //loadWeatherData()

        // Listen for role changes to load appropriate tasks
//        viewModelScope.launch {
//            userRole.collectLatest { role ->
//                loadTasksData()
//            }
//        }
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

    fun loadHotNewTasks(){
        _newTasksState.value = TasksState.Loading
        viewModelScope.launch(ioDispatcher){
            try {
                val managerId = if (userRole.value == "MANAGER") {
                    userId.value
                } else {
                    null
                }
                val response = taskRepository.getTasksByFilter(status = "submitted", page = 0, size = 3, sortBy = "createdAt", sortDirection = "DESC", managerId = managerId)
                when(response){is ApiResponse.Success ->{
                        _newTasksState.value = TasksState.Success(response.data.first)
                    }
                    is ApiResponse.Error -> {
                        _newTasksState.value = TasksState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _newTasksState.value = TasksState.Loading
                    }

                }
            }
            catch (e: Exception){
                _newTasksState.value = TasksState.Error("Error loading new tasks: ${e.message}")
            }
        }
    }

    fun loadHotMyTasks() {
        _tasksState.value = TasksState.Loading
        viewModelScope.launch(ioDispatcher) {
            val response = taskRepository.getCreatedTasks(0, 3) // Fetch 3 tasks
            when (response) {
                is ApiResponse.Success -> {
                    _tasksState.value = TasksState.Success(response.data.first)
                }
                is ApiResponse.Error -> {
                    _tasksState.value = TasksState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _tasksState.value = TasksState.Loading
                }
            }
        }
    }

    fun isSubscribed(): Boolean {
        return false  // Todo add logic to check subscription status
    }

    sealed class TeamMembersState {
        object Loading : TeamMembersState()
        data class Success(val members: List<User>) : TeamMembersState()
        data class Error(val message: String) : TeamMembersState()
    }

    fun loadUserData() {
        _userState.value = UserState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = userRepository.getCurrentUser()) {
                is ApiResponse.Success -> {
                    _userState.value = UserState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    _userState.value = UserState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _userState.value = UserState.Loading
                }
            }
        }
    }

    // Function to load assigned tasks
    fun loadAssignedTasks() {
        _tasksState.value = TasksState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.getAssignedTasks()) {
                is ApiResponse.Success -> {
                    // Extract just the list of tasks from the pair
                    _tasksState.value = TasksState.Success(response.data.first)
                }
                is ApiResponse.Error -> {
                    _tasksState.value = TasksState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _tasksState.value = TasksState.Loading
                }
            }
        }
    }

    fun loadHotAssignedTasks() {
        _tasksState.value = TasksState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = taskRepository.getAssignedTasks(0,3)) {
                is ApiResponse.Success -> {
                    // Extract just the list of tasks from the pair
                    _tasksState.value = TasksState.Success(response.data.first)
                }
                is ApiResponse.Error -> {
                    _tasksState.value = TasksState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _tasksState.value = TasksState.Loading
                }
            }
        }
    }

    // In HomeViewModel.kt, update the loadWeatherData function:

    fun loadWeatherData() {
        _weatherState.value = WeatherState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = weatherRepository.getWeatherForLocation("")) {
                is ApiResponse.Success -> {
                    _weatherState.value = WeatherState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    _weatherState.value = WeatherState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _weatherState.value = WeatherState.Loading
                }
            }
        }
    }



    sealed class UserState {
        object Loading : UserState()
        data class Success(val user: User) : UserState()
        data class Error(val message: String) : UserState()
    }

    sealed class WeatherState {
        object Loading : WeatherState()
        data class Success(val weatherInfo: WeatherInfo) : WeatherState()
        data class Error(val message: String) : WeatherState()
    }

    sealed class TasksState {
        object Loading : TasksState()
        data class Success(val tasks: List<Task>) : TasksState()
        data class Error(val message: String) : TasksState()
    }


    fun loadTasksByStatus(status: String) {
        _tasksState.value = TasksState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val response = taskRepository.getTasksByStatus(status, 0, 10)

                when (response) {
                    is ApiResponse.Success -> {
                        _tasksState.value = TasksState.Success(response.data.first)
                    }
                    is ApiResponse.Error -> {
                        _tasksState.value = TasksState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _tasksState.value = TasksState.Loading
                    }
                }
            } catch (e: Exception) {
                _tasksState.value = TasksState.Error("Error loading tasks: ${e.message}")
            }
        }
    }

    fun loadHotTasksByStatus(status: String) {
        _tasksState.value = TasksState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val response = taskRepository.getTasksByStatus(status, 0, 3)
                when (response) {
                    is ApiResponse.Success -> {
                        _tasksState.value = TasksState.Success(response.data.first)
                    }
                    is ApiResponse.Error -> {
                        _tasksState.value = TasksState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _tasksState.value = TasksState.Loading
                    }
                }
            } catch (e: Exception) {
                _tasksState.value = TasksState.Error("Error loading tasks: ${e.message}")
            }
        }
    }

    // Keep existing loadTasksData function
    fun loadTasksData() {
        _tasksState.value = TasksState.Loading
        viewModelScope.launch(ioDispatcher) {
            val currentRole = userRole.value

            val response = if (currentRole == "MANAGER") {
                // For managers, get tasks created by them
                taskRepository.getCreatedTasks(0, 5)
            } else {
                // For supervisors, get tasks assigned to them
                taskRepository.getAssignedTasks(0, 5)
            }

            when (response) {
                is ApiResponse.Success -> {
                    _tasksState.value = TasksState.Success(response.data.first)
                }
                is ApiResponse.Error -> {
                    _tasksState.value = TasksState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _tasksState.value = TasksState.Loading
                }
            }
        }
    }

    sealed class SupervisorsListState {
        object Idle : SupervisorsListState()
        object Loading : SupervisorsListState()
        data class Success(val supervisors: List<SupervisorListResponse>) : SupervisorsListState()
        data class Error(val message: String) : SupervisorsListState()
    }

}