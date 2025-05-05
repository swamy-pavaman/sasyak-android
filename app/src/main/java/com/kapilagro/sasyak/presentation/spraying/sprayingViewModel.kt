package com.kapilagro.sasyak.presentation.spraying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.SprayingDetails
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
class SprayingViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _createSprayingState = MutableStateFlow<CreateSprayingState>(CreateSprayingState.Idle)
    val createSprayingState: StateFlow<CreateSprayingState> = _createSprayingState.asStateFlow()

    fun createSprayingTask(
        sprayingDetails: SprayingDetails,
        description: String
    ) {
        _createSprayingState.value = CreateSprayingState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val detailsJson = Json.encodeToString(sprayingDetails)

                when (val response = taskRepository.createTask(
                    taskType = "SPRAYING",
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = null,  // TODO: Handle file uploads
                    assignedToId = null
                )) {
                    is ApiResponse.Success -> {
                        _createSprayingState.value = CreateSprayingState.Success(response.data)
                    }
                    is ApiResponse.Error -> {
                        _createSprayingState.value = CreateSprayingState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _createSprayingState.value = CreateSprayingState.Loading
                    }
                }
            } catch (e: Exception) {
                _createSprayingState.value = CreateSprayingState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearCreateSprayingState() {
        _createSprayingState.value = CreateSprayingState.Idle
    }

    sealed class CreateSprayingState {
        object Idle : CreateSprayingState()
        object Loading : CreateSprayingState()
        data class Success(val task: Task) : CreateSprayingState()
        data class Error(val message: String) : CreateSprayingState()
    }
}