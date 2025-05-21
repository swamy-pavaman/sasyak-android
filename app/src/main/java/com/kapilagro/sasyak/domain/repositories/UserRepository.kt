package com.kapilagro.sasyak.domain.repositories

import com.kapilagro.sasyak.data.api.models.responses.SupervisorListResponse
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TeamMember
import com.kapilagro.sasyak.domain.models.User

interface UserRepository {
    suspend fun getCurrentUser(): ApiResponse<User>
//    suspend fun updateProfile(name: String?, phoneNumber: String?, password: String?): ApiResponse<User>
suspend fun updateProfile(name: String?, phoneNumber: String?, password: String?, location: String?, profileImageUrl: String?
): ApiResponse<User>

    suspend fun getSupervisorManager(): ApiResponse<User>
    suspend fun getSupervisorProfile(): ApiResponse<User>

    //suspend fun getTeamMemberById(userId: Int): ApiResponse<User>
    suspend fun updateTeamMember(userId: Int, name: String?, phoneNumber: String?): ApiResponse<User>
    suspend fun getTeamMembers(): ApiResponse<List<TeamMember>>
    suspend fun getAllSupervisors(): ApiResponse<List<TeamMember>>
    suspend fun getSupervisorsList(): ApiResponse<List<SupervisorListResponse>>
}