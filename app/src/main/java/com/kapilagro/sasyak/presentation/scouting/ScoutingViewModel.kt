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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class ScoutingViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _createScoutingState = MutableStateFlow<CreateScoutingState>(CreateScoutingState.Idle)
    val createScoutingState: StateFlow<CreateScoutingState> = _createScoutingState.asStateFlow()

    fun createScoutingTask(
        scoutingDetails: ScoutingDetails,
        description: String
    ) {
        _createScoutingState.value = CreateScoutingState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
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

    sealed class CreateScoutingState {
        object Idle : CreateScoutingState()
        object Loading : CreateScoutingState()
        data class Success(val task: Task) : CreateScoutingState()
        data class Error(val message: String) : CreateScoutingState()
    }
}