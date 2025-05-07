package com.kapilagro.sasyak.data.api.interceptors

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ResponseInterceptor @Inject constructor(
    private val authRepository: dagger.Lazy<AuthRepository>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Skip token refresh for refresh-token requests to avoid loops
        if (request.url.toString().contains("refresh-token")) {
            return chain.proceed(request)
        }

        var response = chain.proceed(request)

        // Check if we received a 401 or 403 response
        if (response.code == 401 || response.code == 403) {
            println("ResponseInterceptor: Received ${response.code}, attempting token refresh")

            // Try to get a new access token using the refresh token
            val tokenRefreshResult = runBlocking {
                authRepository.get().refreshToken()
            }

            when (tokenRefreshResult) {
                is ApiResponse.Success -> {
                    println("ResponseInterceptor: Token refresh successful, got new token")

                    // Close the previous response
                    response.close()

                    // Get the new token that was saved to SharedPreferences
                    val newToken = authRepository.get().getAccessToken()

                    if (!newToken.isNullOrBlank()) {
                        // Create a new request with the fresh token
                        val newRequest = request.newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer $newToken")
                            .build()

                        println("ResponseInterceptor: Retrying request with new token")
                        // Retry the request with the new token
                        return chain.proceed(newRequest)
                    } else {
                        println("ResponseInterceptor: New token is null or blank after refresh")
                    }
                }
                else -> {
                    println("ResponseInterceptor: Token refresh failed: ${(tokenRefreshResult as? ApiResponse.Error)?.errorMessage ?: "Unknown error"}")
                }
            }
        }

        return response
    }
}