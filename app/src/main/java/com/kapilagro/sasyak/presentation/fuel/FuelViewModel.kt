package com.kapilagro.sasyak.presentation.fuel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.FuelEntry
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
class FuelViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _createFuelRequestState = MutableStateFlow<CreateFuelRequestState>(CreateFuelRequestState.Idle)
    val createFuelRequestState: StateFlow<CreateFuelRequestState> = _createFuelRequestState.asStateFlow()

    fun createFuelRequest(
        fuelEntry: FuelEntry,
        description: String
    ) {
        _createFuelRequestState.value = CreateFuelRequestState.Loading
        viewModelScope.launch(ioDispatcher) {
            try {
                val detailsJson = Json.encodeToString(fuelEntry)

                when (val response = taskRepository.createTask(
                    taskType = "FUEL_REQUEST",
                    description = description,
                    detailsJson = detailsJson,
                    imagesJson = null,  // Add image handling later if needed
                    assignedToId = null
                )) {
                    is ApiResponse.Success -> {
                        _createFuelRequestState.value = CreateFuelRequestState.Success(response.data)
                    }
                    is ApiResponse.Error -> {
                        _createFuelRequestState.value = CreateFuelRequestState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _createFuelRequestState.value = CreateFuelRequestState.Loading
                    }
                }
            } catch (e: Exception) {
                _createFuelRequestState.value = CreateFuelRequestState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun clearCreateFuelRequestState() {
        _createFuelRequestState.value = CreateFuelRequestState.Idle
    }

    sealed class CreateFuelRequestState {
        object Idle : CreateFuelRequestState()
        object Loading : CreateFuelRequestState()
        data class Success(val task: Task) : CreateFuelRequestState()
        data class Error(val message: String) : CreateFuelRequestState()
    }
}