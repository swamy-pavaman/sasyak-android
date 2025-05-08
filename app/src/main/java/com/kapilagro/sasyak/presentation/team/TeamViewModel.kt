package com.kapilagro.sasyak.presentation.team

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TeamMember
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.domain.repositories.UserRepository
import com.kapilagro.sasyak.presentation.team.TeamViewModel.TeamState.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _teamState = MutableStateFlow<TeamState>(TeamState.Loading)
    val teamState: StateFlow<TeamState> = _teamState

    init {
        loadTeamMembers()
    }

    fun loadTeamMembers() {
        viewModelScope.launch {
            _teamState.value = TeamState.Loading

            when (val response = userRepository.getTeamMembers()) {
                is ApiResponse.Success -> {
                    _teamState.value = Success(response.data)
                }
                is ApiResponse.Error -> {
                    _teamState.value = Error(response.errorMessage)
                }

                ApiResponse.Loading -> TODO()
            }
        }
    }

    sealed class TeamState {
        object Loading : TeamState()
        data class Success(val teamMembers: List<TeamMember>) : TeamState()
        data class Error(val message: String) : TeamState()
    }
}