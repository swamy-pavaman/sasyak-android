package com.kapilagro.sasyak.domain.usecases

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TeamMember
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.domain.repositories.UserRepository
import javax.inject.Inject

class GetTeamMembersUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(): ApiResponse<List<TeamMember>> {
        return userRepository.getTeamMembers()
    }
}