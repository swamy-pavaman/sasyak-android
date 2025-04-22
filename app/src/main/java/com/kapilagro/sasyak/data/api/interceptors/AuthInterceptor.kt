package com.kapilagro.sasyak.data.api.interceptors

import com.kapilagro.sasyak.domain.repositories.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authRepository: dagger.Lazy<AuthRepository>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        val token = authRepository.get().getAccessToken()
        if (!token.isNullOrBlank()) {
            request.addHeader("Authorization", "Bearer $token")
        }
        return chain.proceed(request.build())
    }
}
