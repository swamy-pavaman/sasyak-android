package com.kapilagro.sasyak.domain.repositories

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.User

interface UserRepository {
    suspend fun getCurrentUser(): ApiResponse<User>
    suspend fun updateProfile(name: String?, phoneNumber: String?, password: String?): ApiResponse<User>
    suspend fun getSupervisorManager(): ApiResponse<User>
    suspend fun getSupervisorProfile(): ApiResponse<User>
    suspend fun getTeamMembers(): ApiResponse<List<User>>
    suspend fun getAllSupervisors(): ApiResponse<List<User>>
    suspend fun getTeamMemberById(userId: Int): ApiResponse<User>
    suspend fun updateTeamMember(userId: Int, name: String?, phoneNumber: String?): ApiResponse<User>
}