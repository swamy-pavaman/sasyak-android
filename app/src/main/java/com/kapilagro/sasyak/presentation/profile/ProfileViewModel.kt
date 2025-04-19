package com.kapilagro.sasyak.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.sasyak.di.IoDispatcher
import com.example.sasyak.domain.models.ApiResponse
import com.example.sasyak.domain.models.User
import com.example.sasyak.domain.repositories.AuthRepository
import com.example.sasyak.domain.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Loading)
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _managerState = MutableStateFlow<ManagerState>(ManagerState.Loading)
    val managerState: StateFlow<ManagerState> = _managerState.asStateFlow()

    private val _updateProfileState = MutableStateFlow<UpdateProfileState>(UpdateProfileState.Idle)
    val updateProfileState: StateFlow<UpdateProfileState> = _updateProfileState.asStateFlow()

    private val _userRole = MutableStateFlow<String?>(null)
    val userRole: StateFlow<String?> = _userRole.asStateFlow()

    init {
        loadUserProfile()
        viewModelScope.launch {
            authRepository.getUserRole().collect { role ->
                _userRole.value = role
                if (role == "SUPERVISOR") {
                    loadManagerData()
                }
            }
        }
    }

    fun loadUserProfile() {
        _profileState.value = ProfileState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = userRepository.getCurrentUser()) {
                is ApiResponse.Success -> {
                    _profileState.value = ProfileState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    _profileState.value = ProfileState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _profileState.value = ProfileState.Loading
                }
            }
        }
    }

    fun loadManagerData() {
        _managerState.value = ManagerState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = userRepository.getSupervisorManager()) {
                is ApiResponse.Success -> {
                    _managerState.value = ManagerState.Success(response.data)
                }
                is ApiResponse.Error -> {
                    _managerState.value = ManagerState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _managerState.value = ManagerState.Loading
                }
            }
        }
    }

    fun updateProfile(name: String?, phoneNumber: String?, password: String?) {
        _updateProfileState.value = UpdateProfileState.Loading
        viewModelScope.launch(ioDispatcher) {
            when (val response = userRepository.updateProfile(name, phoneNumber, password)) {
                is ApiResponse.Success -> {
                    _updateProfileState.value = UpdateProfileState.Success(response.data)
                    loadUserProfile() // Refresh profile data
                }
                is ApiResponse.Error -> {
                    _updateProfileState.value = UpdateProfileState.Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    _updateProfileState.value = UpdateProfileState.Loading
                }
            }
        }
    }

    fun clearUpdateProfileState() {
        _updateProfileState.value = UpdateProfileState.Idle
    }

    fun logout() {
        viewModelScope.launch(ioDispatcher) {
            authRepository.logout()
        }
    }

    sealed class ProfileState {
        object Loading : ProfileState()
        data class Success(val user: User) : ProfileState()
        data class Error(val message: String) : ProfileState()
    }

    sealed class ManagerState {
        object Loading : ManagerState()
        data class Success(val manager: User) : ManagerState()
        data class Error(val message: String) : ManagerState()
    }

    sealed class UpdateProfileState {
        object Idle : UpdateProfileState()
        object Loading : UpdateProfileState()
        data class Success(val user: User) : UpdateProfileState()
        data class Error(val message: String) : UpdateProfileState()
    }
}
