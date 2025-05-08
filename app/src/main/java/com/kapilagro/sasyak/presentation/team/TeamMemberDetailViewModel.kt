package com.kapilagro.sasyak.presentation.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TeamMember
import com.kapilagro.sasyak.domain.repositories.UserRepository
import com.kapilagro.sasyak.presentation.team.TeamMemberDetailViewModel.TeamMemberState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamMemberDetailViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _teamMemberState = MutableStateFlow<TeamMemberState>(TeamMemberState.Loading)
    val teamMemberState: StateFlow<TeamMemberState> = _teamMemberState

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

    sealed class TeamMemberState {
        object Loading : TeamMemberState()
        data class Success(val teamMember: TeamMember) : TeamMemberState()
        data class Error(val message: String) : TeamMemberState()
    }
}