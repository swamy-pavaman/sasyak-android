package com.kapilagro.sasyak.data.repositories

import android.content.SharedPreferences
import android.util.Log
import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.data.api.mappers.toEntityModel
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
import androidx.core.content.edit

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository,
    private val localDataSource: LocalDataSource,
    private val sharedPreferences: SharedPreferences
) : UserRepository {

    companion object {
        private const val TAG = "UserRepository"
    }

    override suspend fun getCurrentUser(): ApiResponse<User> {
        return try {
            // Retrieve user ID from SharedPreferences
            val currentUserId = sharedPreferences.getInt("current_user_id", -1).takeIf { it != -1 }
            Log.d(TAG, "Current user ID from SharedPreferences: $currentUserId")

            if (currentUserId != null) {
                Log.d(TAG, "Trying to get cached user first")
                // Try to get from cache first
                val cachedUser = localDataSource.getUserById(currentUserId).firstOrNull()
                Log.d(TAG, "Cached user: $cachedUser")

                // Check if cache is valid (not expired)
                if (cachedUser != null) {
                    Log.d(TAG, "Cache hit - returning cached user")
                    return ApiResponse.Success(cachedUser.toDomainModel())
                }
            }

            Log.d(TAG, "Cache miss or no user ID - fetching from API")
            // Cache miss or no user ID - fetch from API
            val response = apiService.getCurrentUser()
            Log.d(TAG, "API call made, response: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                Log.d(TAG, "API call successful")
                val user = response.body()!!.toDomainModel()

                // Save user ID to SharedPreferences
                sharedPreferences.edit {
                    putInt("current_user_id", user.id)
                }

                // Save user role for future use
                authRepository.saveUserRole(user.role)

                Log.d(TAG, "Saving user to cache")
                // Cache the user data
                localDataSource.insertUser(user.toEntityModel().copy(lastSyncedAt = Date()))

                ApiResponse.Success(user)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get user")
            }
        } catch (e: Exception) {
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
                localDataSource.insertUser(updatedUser.toEntityModel().copy(lastSyncedAt = Date()))

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
            // Retrieve manager ID from SharedPreferences
            val managerId = sharedPreferences.getInt("manager_id", -1).takeIf { it != -1 }
            Log.d(TAG, "Manager ID from SharedPreferences: $managerId")

            if (managerId != null) {
                Log.d(TAG, "Trying to get cached manager first")
                // Try to get manager from cache first
                val cachedManager = localDataSource.getUserById(managerId).firstOrNull()
                Log.d(TAG, "Cached manager: $cachedManager")

                // Check if cache is valid
                if (cachedManager != null) {
                    Log.d(TAG, "Cache hit - returning cached manager")
                    return ApiResponse.Success(cachedManager.toDomainModel())
                }
            }

            Log.d(TAG, "Cache miss or no manager ID - fetching from API")
            // Cache miss or no manager ID - fetch from API
            val response = apiService.getSupervisorManager()
            Log.d(TAG, "API call made for manager, response: ${response.body()}")

            if (response.isSuccessful && response.body() != null) {
                val manager = response.body()!!.toDomainModel()

                // Save manager ID to SharedPreferences
                sharedPreferences.edit {
                    putInt("manager_id", manager.id)
                }

                // Cache the manager data
                localDataSource.insertUser(manager.toEntityModel().copy(lastSyncedAt = Date()))

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
                ApiResponse.Success(response.body()!!.employees.map {
                    TeamMember(
                        id = it.id,
                        name = it.name,
                        email = it.email,
                        role = it.role,
                        profileImageUrl = it.profile,
                        phoneNumber = it.phoneNumber,
                        location = it.location
                    )
                })
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get team members")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getAdminTeam(): ApiResponse<List<TeamMember>> {
        return try {
            val response = apiService.getAdminTeam()
            Log.d(TAG, "API call made for team, response: ${response.body()}")
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.employees.map {
                    TeamMember(
                        id = it.id,
                        name = it.name,
                        email = it.email,
                        role = it.role,
                        profileImageUrl = it.profile,
                        phoneNumber = it.phoneNumber,
                        location = it.location
                    )
                })
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get team members")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getAdminUserById(userId: Int): ApiResponse<User>{
        return try {
            val response = apiService.getAdminUserById(userId)
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.toDomainModel())
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get user")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }

    override suspend fun getAllSupervisors(): ApiResponse<List<TeamMember>> {
        return try {
            val response = apiService.getAllSupervisors()
            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.employees.map {
                    TeamMember(
                        id = it.id,
                        name = it.name,
                        email = it.email,
                        role = it.role,
                        profileImageUrl = it.profile,
                        phoneNumber = it.phoneNumber,
                        location = it.location
                    )
                })
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
                ApiResponse.Success(response.body()!!.toDomainModel())
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to update team member")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }
}