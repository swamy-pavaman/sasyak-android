package com.kapilagro.sasyak.domain.repositories


import com.kapilagro.sasyak.domain.models.*
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    suspend fun login(email: String, password: String): ApiResponse<AuthResponse>
    suspend fun refreshToken(): ApiResponse<AuthResponse>
    suspend fun logout()
    suspend fun getCurrentUserId(): Int?
    fun getAuthState(): Flow<Boolean>
    fun getUserRole(): Flow<String?>
    suspend fun saveAuthTokens(authResponse: AuthResponse)
    suspend fun clearAuthTokens()
    fun saveUserRole(role: String)
    fun getAccessToken(): String?

}