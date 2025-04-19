package com.kapilagro.sasyak.domain.usecases

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.models.AuthResponse
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import javax.inject.Inject
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): ApiResponse<AuthResponse> {
        return authRepository.login(email, password)
    }
}
