package com.kapilagro.sasyak.data.api.interceptors

import android.content.Context
import com.kapilagro.sasyak.utils.NetworkUtils.isNetworkAvailable
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class OfflineCacheInterceptor @Inject constructor(
    private val context: Context
): Interceptor {
    // Endpoints allowed for offline cache
    private val cacheAllowedEndpoints = listOf(
        "api/catalog/Valve",
        "api/manager/users/supervisor-list",
        "api/admin/users/by-role/MANAGER",
        "api/admin/users/by-role/SUPERVISOR"
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()

        // Only apply cache policy to allowed endpoints
        if (cacheAllowedEndpoints.any { request.url.toString().contains(it, ignoreCase = true) }) {
            if (!isNetworkAvailable(context)) {
                // Serve cached data up to 7 days old
                val maxStale = 60 * 60 * 24 * 7 // 7 days in seconds
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                    .build()
            }
        }

        return chain.proceed(request)
    }
}