package com.kapilagro.sasyak.presentation.common.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.models.responses.CropsResponce
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import javax.inject.Inject

@HiltViewModel
class CropViewModel @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _cropsState = MutableStateFlow<CropsState>(CropsState.Idle)
    val cropsState: StateFlow<CropsState> = _cropsState

    private var isFetching = false // Prevent redundant calls

    init {
        fetchCrops()
    }

    fun fetchCrops() {
        if (isFetching) return
        viewModelScope.launch {
            isFetching = true
            _cropsState.value = CropsState.Loading
            try {
                val crops = apiService.cropService()
                _cropsState.value = CropsState.Success(crops)
            } catch (e: HttpException) {
                if (e.code() == 403 || e.code() == 401) {
                    _cropsState.value = CropsState.Error("Authentication failed: Invalid or missing access token")
                } else {
                    _cropsState.value = CropsState.Error("HTTP error: ${e.code()} - ${e.message()}")
                }
            } catch (e: Exception) {
                _cropsState.value = CropsState.Error("Network error: ${e.message ?: "Unknown error"}")
            } finally {
                isFetching = false
            }
        }
    }
}

sealed class CropsState {
    object Idle : CropsState()
    object Loading : CropsState()
    data class Success(val crops: List<CropsResponce>) : CropsState()
    data class Error(val message: String) : CropsState()
}