package com.kapilagro.sasyak.presentation.sowing

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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

@HiltViewModel
class SowingViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _createSowingState = MutableStateFlow<CreateSowingState>(CreateSowingState.Idle)
    val createSowingState: StateFlow<CreateSowingState> = _createSowingState.asStateFlow()

    fun createSowingTask(
        sowingDetails: SowingDetails,
        description: String
    ) {
        _createSowingState.value = CreateSowingState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val detailsJson = Json.encodeToString(sowingDetails)

                when (val response = taskRepository.createTask(
                    taskType = "SOWING",
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = null,  // TODO: Handle file uploads
                    assignedToId = null
                )) {
                    is ApiResponse.Success -> {
                        _createSowingState.value = CreateSowingState.Success(response.data)
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