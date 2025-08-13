package com.kapilagro.sasyak.data.api.interceptors

import okhttp3.Interceptor
import okhttp3.Response

class NetworkCacheInterceptor : Interceptor {
    // Endpoints allowed for caching
    private val cacheAllowedEndpoints = listOf(
        "api/catalog/Valve",
        "api/manager/users/supervisor-list",
        "api/admin/users/by-role/MANAGER",
        "api/admin/users/by-role/SUPERVISOR",
        "api/catalog/Driver",
        "api/catalog/Vehicle"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val originalResponse = chain.proceed(request)
        val path = request.url.encodedPath

        return if (cacheAllowedEndpoints.any { path.contains(it, ignoreCase = true) }) {
            val maxAge = 60 * 5 // cache for 5 Min's
            originalResponse.newBuilder()
                .removeHeader("Pragma")
                .removeHeader("Cache-Control")
                .header("Cache-Control", "public, max-age=$maxAge")
                .build()
        } else {
            originalResponse
        }
    }
}
