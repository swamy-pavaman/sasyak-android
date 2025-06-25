package com.kapilagro.sasyak.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.di.IoDispatcher
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _loginState = MutableStateFlow<LoginState>(LoginState.Idle)
    val loginState: StateFlow<LoginState> = _loginState.asStateFlow()

    private val _resetState = MutableStateFlow<ResetState>(ResetState.Idle) // Added for forgot password
    val resetState: StateFlow<ResetState> = _resetState.asStateFlow() // Added for forgot password

    private val _authState = MutableStateFlow(false)
    val authState: StateFlow<Boolean> = _authState.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getAuthState().collect { isAuthenticated ->
                _authState.value = isAuthenticated
            }
        }

        viewModelScope.launch {
            authRepository.getUserRole().collect { role ->
                _userRole.value = role
            }
        }
    }

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = LoginState.Error("Email and password cannot be empty")
            return
        }

        _loginState.value = LoginState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = authRepository.login(email, password)) {
                is ApiResponse.Success -> {
                    _loginState.value = LoginState.Success(response.data.userId)
                }
                is ApiResponse.Error -> {
                    _loginState.value = LoginState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _loginState.value = LoginState.Loading
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch(ioDispatcher) {
            authRepository.logout()
            _loginState.value = LoginState.Idle
        }
    }

    fun clearLoginState() {
        _loginState.value = LoginState.Idle
    }

    // Added forgot password functionality
    fun requestPasswordReset(email: String) {
        viewModelScope.launch(ioDispatcher) {
            _resetState.value = ResetState.Loading
            try {
                when (val response = authRepository.requestPasswordReset(email)) {
                    is ApiResponse.Success -> {
                        _resetState.value = ResetState.Success
                    }
                    is ApiResponse.Error -> {
                        _resetState.value = ResetState.Error(response.errorMessage)
                    }
                    is ApiResponse.Loading -> {
                        _resetState.value = ResetState.Loading
                    }
                }
            } catch (e: Exception) {
                _resetState.value = ResetState.Error("Failed to send reset email: ${e.message}")
            }
        }
    }

    sealed class LoginState {
        object Idle : LoginState()
        object Loading : LoginState()
        data class Success(val userId: Int) : LoginState()
        data class Error(val message: String) : LoginState()
    }

    // Added ResetState for forgot password
    sealed class ResetState {
        object Idle : ResetState()
        object Loading : ResetState()
        object Success : ResetState()
        data class Error(val message: String) : ResetState()
    }
}