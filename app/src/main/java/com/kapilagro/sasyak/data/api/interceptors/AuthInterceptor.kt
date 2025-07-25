package com.kapilagro.sasyak.data.api.interceptors

import com.kapilagro.sasyak.domain.repositories.AuthRepository
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val authRepository: dagger.Lazy<AuthRepository>
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val request = originalRequest.newBuilder(
        )

        val url = originalRequest.url.toString()

        // Skip adding auth token for MinIO presigned URLs
        if (url.contains("https://minio.kapilagro.com:9000/")) { // || url.contains("/sasyak/")
            return chain.proceed(originalRequest)

        }

        val token = authRepository.get().getAccessToken()
        if (!token.isNullOrBlank()) {
            println("AuthInterceptor: Adding token to request: ${originalRequest.url}")
            request.addHeader("Authorization", "Bearer $token")
        } else {
            println("AuthInterceptor: No token available for request: ${originalRequest.url}")
        }

        return chain.proceed(request.build())
    }
}