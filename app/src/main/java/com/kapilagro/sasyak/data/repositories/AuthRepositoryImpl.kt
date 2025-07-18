package com.kapilagro.sasyak.data.repositories

import android.content.SharedPreferences
import android.util.Log
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.data.api.models.requests.RefreshTokenRequest
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.LoginRequest
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit
import com.kapilagro.sasyak.data.local.LocalDataSource

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val sharedPreferences: SharedPreferences,
    private val localDataSource: LocalDataSource
) : AuthRepository {

    private val authStateFlow = MutableStateFlow(isLoggedIn())
    private val userRoleFlow = MutableStateFlow<String?>(getUserRoleFromPrefs())
    private val userIdFlow = MutableStateFlow<Int?>(getUserIdFromPrefs())


    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
        const val KEY_USER_EMAIL = "user_email"
        const val KEY_USER_NAME = "user_name"
        const val KEY_USER_ROLE = "user_role"
    }

    override suspend fun login(email: String, password: String): ApiResponse<com.kapilagro.sasyak.domain.models.AuthResponse> {
        return try {
            val response = apiService.login(LoginRequest(email, password))

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!.toDomainModel()
                saveAuthTokens(authResponse)
                ApiResponse.Success(authResponse)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Login failed")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun refreshToken(): ApiResponse<com.kapilagro.sasyak.domain.models.AuthResponse> {
        val refreshToken = sharedPreferences.getString(KEY_REFRESH_TOKEN, null)
            ?: return ApiResponse.Error("No refresh token found")

        return try {
            val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
            Log.d("AuthRepositoryImpl", "Refresh token response: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!.toDomainModel()
                saveAuthTokensForRefresh(authResponse)
                ApiResponse.Success(authResponse)
            } else {
                // Clear tokens if refresh fails
                clearAuthTokens()
                ApiResponse.Error(response.errorBody()?.string() ?: "Token refresh failed")
            }
        } catch (e: Exception) {
            clearAuthTokens()
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun logout() {
        clearAuthTokens()
        localDataSource.deleteAllUsers()
        localDataSource.deleteAllNotifications()
        sharedPreferences.edit().apply{
            remove("current_user_id")
            remove("manager_id")
            apply()
        }
    }

    override fun getAuthState(): Flow<Boolean> = authStateFlow.asStateFlow()

    override fun getUserRole(): Flow<String?> = userRoleFlow.asStateFlow()

    override fun getUserId(): Flow<Int?> = userIdFlow.asStateFlow()

    override suspend fun saveAuthTokens(authResponse: com.kapilagro.sasyak.domain.models.AuthResponse) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, authResponse.accessToken)
            putString(KEY_REFRESH_TOKEN, authResponse.refreshToken)
            putInt(KEY_USER_ID, authResponse.userId)
            putString(KEY_USER_EMAIL, authResponse.email)
            putString(KEY_USER_NAME, authResponse.name)
            putString(KEY_USER_ROLE, authResponse.role)  // Save the role
            apply()
        }

        authStateFlow.value = true
        userRoleFlow.value = authResponse.role  // Update the role flow
        userIdFlow.value = authResponse.userId
    }

    override suspend fun saveAuthTokensForRefresh(authResponse: com.kapilagro.sasyak.domain.models.AuthResponse) {
        sharedPreferences.edit().apply {
            putString(KEY_ACCESS_TOKEN, authResponse.accessToken)
            putString(KEY_REFRESH_TOKEN, authResponse.refreshToken)
            apply()
        }
        authStateFlow.value = true
        userRoleFlow.value = sharedPreferences.getString(KEY_USER_ROLE, null)
    }

    override suspend fun clearAuthTokens() {
        sharedPreferences.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_USER_ID)
            remove(KEY_USER_EMAIL)
            remove(KEY_USER_NAME)
            remove(KEY_USER_ROLE)
            apply()
        }

        authStateFlow.value = false
        userRoleFlow.value = null
    }


    // Moved from implementation method to interface implementation
    override fun getAccessToken(): String? {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null)
    }

    // Moved from implementation method to interface implementation
    override fun saveUserRole(role: String) {
        Log.d("AuthRepositoryImpl", "Saving user role: $role")
        sharedPreferences.edit{ putString(KEY_USER_ROLE, role) }
        Log.d("AuthRepositoryImpl", "User role saved: ${sharedPreferences.getString(KEY_USER_ROLE, null)}")
        userRoleFlow.value = role
        Log.d("AuthRepositoryImpl", "User role saved: ${userRoleFlow.value}")
    }

    private fun isLoggedIn(): Boolean {
        return !sharedPreferences.getString(KEY_ACCESS_TOKEN, null).isNullOrEmpty()
    }

    private fun getUserRoleFromPrefs(): String? {
        return sharedPreferences.getString(KEY_USER_ROLE, null)
    }
    private fun getUserIdFromPrefs(): Int? {
        val userId = sharedPreferences.getInt(KEY_USER_ID, -1)
        return if (userId == -1) null else userId
    }
}