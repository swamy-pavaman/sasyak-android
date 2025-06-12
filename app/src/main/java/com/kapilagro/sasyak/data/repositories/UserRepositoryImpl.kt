package com.kapilagro.sasyak.data.repositories

import android.util.Log
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.data.api.mappers.toEntity
import com.kapilagro.sasyak.data.api.models.requests.UpdateProfileRequest
import com.kapilagro.sasyak.data.api.models.requests.UpdateTeamMemberRequest
import com.kapilagro.sasyak.data.api.models.responses.SupervisorListResponse
import com.kapilagro.sasyak.data.local.LocalDataSource
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TeamMember
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import com.kapilagro.sasyak.domain.repositories.UserRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val localDataSource: LocalDataSource
) : UserRepository {

    companion object {
        private const val TAG = "UserRepository"
        private const val CACHE_EXPIRY_HOURS = 1 // Cache expires after 1 hour
    }

    override suspend fun getCurrentUser(): ApiResponse<User> {
        return try {
            // Get current user ID from auth repository
            val currentUserId = authRepository.getCurrentUserId() // You'll need to implement this

            if (currentUserId != null) {
                Log.d(TAG, "Trying to get cached user first")
                // Try to get from cache first
                val cachedUser = localDataSource.getUserById(currentUserId).firstOrNull()

                // Check if cache is valid (not expired)
                if (cachedUser != null) {
                    Log.d(TAG, "Cache hit - returning cached user")
                    return ApiResponse.Success(cachedUser.toDomainModel())
                }
            }

            Log.d(TAG, "Cache miss or expired - fetching from API")
            // Cache miss or expired - fetch from API
            val response = apiService.getCurrentUser()

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "API call successful")
                val user = response.body()!!.toDomainModel()

                // Save user role for future use
                authRepository.saveUserRole(user.role)

                Log.d(TAG, "Saving user to cache")
                // Cache the user data
                localDataSource.insertUser(user.toEntity().copy(lastSyncedAt = Date()))

                ApiResponse.Success(user)
            } else {
                // If API fails, try to return cached data even if expired
                Log.d(TAG, "API call failed, trying to return cached user")
                if (currentUserId != null) {
                    val cachedUser = localDataSource.getUserById(currentUserId).firstOrNull()
                    if (cachedUser != null) {
                        return ApiResponse.Success(cachedUser.toDomainModel())
                    }
                }
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get user")
            }
        } catch (e: Exception) {
            // If network fails, try to return cached data
            try {
                val currentUserId = authRepository.getCurrentUserId()
                if (currentUserId != null) {
                    val cachedUser = localDataSource.getUserById(currentUserId).firstOrNull()
                    if (cachedUser != null) {
                        return ApiResponse.Success(cachedUser.toDomainModel())
                    }
                }
            } catch (cacheException: Exception) {
                // Ignore cache exception and return original error
            }
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun updateProfile(
        name: String?,
        phoneNumber: String?,
        password: String?,
        location: String?,
        profileImageUrl: String?
    ): ApiResponse<User> {
        return try {
            val response = apiService.updateUserProfile(
                UpdateProfileRequest(
                    name = name,
                    phoneNumber = phoneNumber,
                    password = password,
                    location = location,
                    profileImageUrl = profileImageUrl
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!.toDomainModel()

                // Update cache with new data
                localDataSource.insertUser(updatedUser.toEntity().copy(lastSyncedAt = Date()))

                ApiResponse.Success(updatedUser)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getSupervisorManager(): ApiResponse<User> {
        return try {
            // Get current user to find their manager
            val currentUser = getCurrentUser()
            if (currentUser is ApiResponse.Success && currentUser.data.managerId != null) {
                val managerId = currentUser.data.managerId

                // Try to get manager from cache first
                val cachedManager = localDataSource.getUserById(managerId).firstOrNull()

                // Check if cache is valid
                if (cachedManager != null) {
                    return ApiResponse.Success(cachedManager.toDomainModel())
                }
            }

            // Cache miss or expired - fetch from API
            val response = apiService.getSupervisorManager()

            if (response.isSuccessful && response.body() != null) {
                val manager = response.body()!!.toDomainModel()

                // Cache the manager data
                localDataSource.insertUser(manager.toEntity().copy(lastSyncedAt = Date()))

                ApiResponse.Success(manager)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get manager")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getSupervisorProfile(): ApiResponse<User> {
        return try {
            val response = apiService.getSupervisorProfile()

            if (response.isSuccessful && response.body() != null) {
                val supervisor = response.body()!!.toDomainModel()

                // Cache the supervisor data
                localDataSource.insertUser(supervisor.toEntity().copy(lastSyncedAt = Date()))

                ApiResponse.Success(supervisor)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get profile")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getTeamMembers(): ApiResponse<List<TeamMember>> {
        return try {
            val response = apiService.getTeamMembers()
            if (response.isSuccessful && response.body() != null) {
                val teamMembers = response.body()!!.employees.map {
                    TeamMember(
                        id = it.id,
                        name = it.name,
                        email = it.email,
                        role = it.role,
                        profileImageUrl = it.profile,
                        phoneNumber = it.phoneNumber,
                        location = it.location
                    )
                }

                // Cache team members as users
                val userEntities = response.body()!!.employees.map { employee ->
                    employee.toDomainModel().toEntity().copy(lastSyncedAt = Date())
                }
                localDataSource.insertUsers(userEntities)

                ApiResponse.Success(teamMembers)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get team members")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getAllSupervisors(): ApiResponse<List<TeamMember>> {
        return try {
            val response = apiService.getAllSupervisors()
            if (response.isSuccessful && response.body() != null) {
                val supervisors = response.body()!!.employees.map {
                    TeamMember(
                        id = it.id,
                        name = it.name,
                        email = it.email,
                        role = it.role,
                        profileImageUrl = it.profile,
                        phoneNumber = it.phoneNumber,
                        location = it.location
                    )
                }

                // Cache supervisors as users
                val userEntities = response.body()!!.employees.map { employee ->
                    employee.toDomainModel().toEntity().copy(lastSyncedAt = Date())
                }
                localDataSource.insertUsers(userEntities)

                ApiResponse.Success(supervisors)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get supervisors")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getSupervisorsList(): ApiResponse<List<SupervisorListResponse>> {
        return try {
            val response = apiService.getSupervisorsList()
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get supervisors list")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun updateTeamMember(
        userId: Int,
        name: String?,
        phoneNumber: String?
    ): ApiResponse<User> {
        return try {
            val response = apiService.updateTeamMember(
                userId,
                UpdateTeamMemberRequest(
                    name = name,
                    phone_number = phoneNumber
                )
            )

            if (response.isSuccessful && response.body() != null) {
                val updatedUser = response.body()!!.toDomainModel()

                // Update cache with new data
                localDataSource.insertUser(updatedUser.toEntity().copy(lastSyncedAt = Date()))

                ApiResponse.Success(updatedUser)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to update team member")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    // Helper method to check if cache is still valid
    private fun isCacheValid(lastSyncedAt: Date): Boolean {
        val currentTime = System.currentTimeMillis()
        val cacheTime = lastSyncedAt.time
        val timeDifference = currentTime - cacheTime
        return timeDifference < TimeUnit.HOURS.toMillis(CACHE_EXPIRY_HOURS.toLong())
    }

    // Method to force refresh cache (useful for pull-to-refresh)
    suspend fun refreshCurrentUser(): ApiResponse<User> {
        return try {
            val response = apiService.getCurrentUser()

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.toDomainModel()

                // Save user role for future use
                authRepository.saveUserRole(user.role)

                // Update cache with fresh data
                localDataSource.insertUser(user.toEntity().copy(lastSyncedAt = Date()))

                ApiResponse.Success(user)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to refresh user")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    // Method to clear user cache
    suspend fun clearUserCache() {
        localDataSource.deleteAllUsers()
    }
}