package com.kapilagro.sasyak.presentation.yield

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.YieldDetails
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
class YieldViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _createYieldState = MutableStateFlow<CreateYieldState>(CreateYieldState.Idle)
    val createYieldState: StateFlow<CreateYieldState> = _createYieldState.asStateFlow()

    fun createYieldTask(
        yieldDetails: YieldDetails,
        description: String
    ) {
        _createYieldState.value = CreateYieldState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val detailsJson = Json.encodeToString(yieldDetails)

                when (val response = taskRepository.createTask(
                    taskType = "YIELD",
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = null,  // TODO: Handle file uploads
                    assignedToId = null
                )) {
                    is ApiResponse.Success -> {
                        _createYieldState.value = CreateYieldState.Success(response.data)
                    }
                    is ApiResponse.Error -> {
                        _createYieldState.value = CreateYieldState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _createYieldState.value = CreateYieldState.Loading
                    }
                }
            } catch (e: Exception) {
                _createYieldState.value = CreateYieldState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearCreateYieldState() {
        _createYieldState.value = CreateYieldState.Idle
    }

    sealed class CreateYieldState {
        object Idle : CreateYieldState()
        object Loading : CreateYieldState()
        data class Success(val task: Task) : CreateYieldState()
        data class Error(val message: String) : CreateYieldState()
    }
}