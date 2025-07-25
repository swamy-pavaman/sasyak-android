package com.kapilagro.sasyak.presentation.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TeamMember
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import com.kapilagro.sasyak.domain.repositories.UserRepository
import com.kapilagro.sasyak.presentation.team.TeamMemberDetailViewModel.TeamMemberState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamMemberDetailViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _teamMemberState = MutableStateFlow<TeamMemberState>(TeamMemberState.Loading)
    val teamMemberState: StateFlow<TeamMemberState> = _teamMemberState
    val userRole : MutableStateFlow<String?> = MutableStateFlow(null)

    init {
        viewModelScope.launch {
            authRepository.getUserRole().collect { role ->
                userRole.value = role
            }
        }
    }

    fun loadTeamMemberDetails(teamMemberId: Int) {
        viewModelScope.launch {
            _teamMemberState.value = TeamMemberState.Loading

            // Note: We'll need to add a getTeamMemberById method to UserRepository
            // For now, let's simulate it by filtering from getTeamMembers()

            when (val response = userRepository.getTeamMembers()) {
                is ApiResponse.Success -> {
                    val teamMember = response.data.find { it.id == teamMemberId }
                    if (teamMember != null) {
                        _teamMemberState.value = Success(teamMember)
                    } else {
                        _teamMemberState.value = Error("Team member not found")
                    }
                }
                is ApiResponse.Error -> {
                    _teamMemberState.value = Error(response.errorMessage)
                }

                ApiResponse.Loading -> TODO()
            }
        }
    }

    fun loadAdminTeamMemberDetails(teamMemberId: Int) {
        viewModelScope.launch {
            _teamMemberState.value = TeamMemberState.Loading

            when (val response = userRepository.getAdminUserById(teamMemberId)) {
                is ApiResponse.Success -> {
                    val user = response.data
                    val teamMember = TeamMember(
                        id = user.id,
                        name = user.name,
                        email = user.email,
                        role = user.role,
                        phoneNumber = user.phoneNumber,
                        location = user.location,
                    )
                    _teamMemberState.value = Success(teamMember)
                }
                is ApiResponse.Error -> {
                    _teamMemberState.value = Error(response.errorMessage)
                }
                is ApiResponse.Loading -> {
                    // Do nothing, already set to Loading
                }
            }
        }
    }

    sealed class TeamMemberState {
        object Loading : TeamMemberState()
        data class Success(val teamMember: TeamMember) : TeamMemberState()
        data class Error(val message: String) : TeamMemberState()
    }
}