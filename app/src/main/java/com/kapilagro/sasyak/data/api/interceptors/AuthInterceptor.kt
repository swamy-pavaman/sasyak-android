package com.kapilagro.sasyak.data.api.interceptors

import com.kapilagro.sasyak.domain.repositories.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authRepository: AuthRepository // Use interface instead of implementation
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip token for authentication endpoints
        if (originalRequest.url.encodedPath.contains("auth")) {
            return chain.proceed(originalRequest)
        }

        val accessToken = authRepository.getAccessToken()

        // If we have a token, add it to the request
        return if (!accessToken.isNullOrEmpty()) {
            val authenticatedRequest = originalRequest.newBuilder()
                .header("Authorization", "Bearer $accessToken")
                .build()

            chain.proceed(authenticatedRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
}