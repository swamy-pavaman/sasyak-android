package com.kapilagro.sasyak.presentation.advice

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TaskAdvice
import com.kapilagro.sasyak.domain.repositories.TaskAdviceRepository
import com.kapilagro.sasyak.presentation.advice.AdviceState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdviceViewModel @Inject constructor(
    private val taskAdviceRepository: TaskAdviceRepository
) : ViewModel() {

    private val _adviceState = MutableStateFlow<AdviceState>(AdviceState.Loading)
    val adviceState: StateFlow<AdviceState> = _adviceState.asStateFlow()

    init {
        loadAdvice()
    }

    fun loadAdvice() {
        viewModelScope.launch {
            _adviceState.value = AdviceState.Loading
            try {
                when (val response = taskAdviceRepository.getAdviceProvidedByCurrentManager()) {
                    is ApiResponse.Success -> {
                        _adviceState.value = Success(response.data)
                    }
                    is ApiResponse.Error -> {
                        _adviceState.value = Error(response.errorMessage)
                    }

                    ApiResponse.Loading -> TODO()
                }
            } catch (e: Exception) {
                _adviceState.value = AdviceState.Error("Failed to load advice: ${e.message}")
            }
        }
    }
}

sealed class AdviceState {
    object Loading : AdviceState()
    data class Success(val advices: List<TaskAdvice>) : AdviceState()
    data class Error(val message: String) : AdviceState()
}