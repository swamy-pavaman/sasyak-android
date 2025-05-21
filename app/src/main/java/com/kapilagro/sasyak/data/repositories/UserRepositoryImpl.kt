package com.kapilagro.sasyak.data.repositories

import com.kapilagro.sasyak.data.api.ApiService
import com.kapilagro.sasyak.data.api.mappers.toDomainModel
import com.kapilagro.sasyak.data.api.models.requests.UpdateProfileRequest
import com.kapilagro.sasyak.data.api.models.requests.UpdateTeamMemberRequest
import com.kapilagro.sasyak.data.api.models.responses.SupervisorListResponse
import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.TeamMember
import com.kapilagro.sasyak.domain.models.User
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import com.kapilagro.sasyak.domain.repositories.UserRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val apiService: ApiService,
    private val authRepository: AuthRepository
) : UserRepository {

    override suspend fun getCurrentUser(): ApiResponse<User> {
        return try {
            val response = apiService.getCurrentUser()

            if (response.isSuccessful && response.body() != null) {
                val user = response.body()!!.toDomainModel()
                // Save user role for future use
                authRepository.saveUserRole(user.role)
                ApiResponse.Success(user)
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to get user")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }


//    override suspend fun updateProfile(
//        name: String?,
//        phoneNumber: String?,
//        password: String?
//    ): ApiResponse<User> {
//        return try {
//            val response = apiService.updateUserProfile(
//                UpdateProfileRequest(
//                    name = name,
//                    phoneNumber = phoneNumber,
//                    password = password
//                )
//            )
//
//            if (response.isSuccessful && response.body() != null) {
//                ApiResponse.Success(response.body()!!.toDomainModel())
//            } else {
//                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to update profile")
//            }
//        } catch (e: Exception) {
//            ApiResponse.Error(e.message ?: "An unknown error occurred")
//        }
//    }

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
                ApiResponse.Success(response.body()!!.toDomainModel())
            } else {
                ApiResponse.Error(response.errorBody()?.string() ?: "Failed to update profile")
            }
        } catch (e: Exception) {
            ApiResponse.Error(e.message ?: "An unknown error occurred")
        }
    }


    override suspend fun getSupervisorManager(): ApiResponse<User> {
        return try {
            val response = apiService.getSupervisorManager()

            if (response.isSuccessful && response.body() != null) {
                ApiResponse.Success(response.body()!!.toDomainModel())
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
                ApiResponse.Success(response.body()!!.toDomainModel())
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