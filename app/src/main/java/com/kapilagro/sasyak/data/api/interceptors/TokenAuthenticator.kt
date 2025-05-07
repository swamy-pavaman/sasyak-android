package com.kapilagro.sasyak.data.api.interceptors

import com.kapilagro.sasyak.domain.models.ApiResponse
import com.kapilagro.sasyak.domain.repositories.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class TokenAuthenticator @Inject constructor(
    private val authRepository: dagger.Lazy<AuthRepository>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        println("TokenAuthenticator: Received response code: ${response.code}")

        // Don't attempt to refresh if we're already trying to refresh the token
        if (response.request.url.toString().contains("refresh-token")) {
            println("TokenAuthenticator: Skipping refresh for refresh-token endpoint")
            return null
        }

        // Only proceed if we received a 401 (Unauthorized) or 403 (Forbidden) response
        if (response.code == 401 || response.code == 403) {
            println("TokenAuthenticator: Token expired (${response.code}), attempting to refresh")

            // Try to get a new access token using the refresh token
            val tokenRefreshResult = runBlocking {
                authRepository.get().refreshToken()
            }

            when (tokenRefreshResult) {
                is ApiResponse.Success -> {
                    println("TokenAuthenticator: Token refresh successful")

                    // Get the new token from the repository (it should be saved already)
                    val newToken = authRepository.get().getAccessToken()

                    if (!newToken.isNullOrBlank()) {
                        // Add the new access token to the failed request and retry
                        println("TokenAuthenticator: Retrying request with new token for URL: ${response.request.url}")
                        return response.request.newBuilder()
                            .removeHeader("Authorization")
                            .header("Authorization", "Bearer $newToken")
                            .build()
                    } else {
                        println("TokenAuthenticator: New token is null or blank after refresh")
                        return null
                    }
                }
                else -> {
                    println("TokenAuthenticator: Token refresh failed: ${(tokenRefreshResult as? ApiResponse.Error)?.errorMessage ?: "Unknown error"}")
                    return null
                }
            }
        } else {
            println("TokenAuthenticator: Response code ${response.code} does not require token refresh")
            return null
        }
    }
}