package com.kapilagro.sasyak.data.api.interceptors

import android.content.SharedPreferences
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
        // Don't attempt to refresh if we're already trying to refresh the token
        if (response.request.url.toString().contains("refresh-token")) {
            return null
        }

        // Check if we received a 401 (Unauthorized) or 403 (Forbidden) response
        if (response.code != 401 && response.code != 403) {
            return null
        }

        // Try to get a new access token using the refresh token
        val newAccessToken = runBlocking {
            when (val result = authRepository.get().refreshToken()) {
                is ApiResponse.Success -> result.data.accessToken
                else -> null
            }
        } ?: return null

        // Add the new access token to the failed request and retry
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccessToken")
            .build()
    }
}