package com.kapilagro.sasyak.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.net.URL
import java.net.HttpURLConnection

object NetworkUtils {
    /**
     * Check if device has internet connectivity
     */
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }
    /**
     * Get network connection type
     */
    fun getNetworkType(context: Context): String {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return "None"
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return "Unknown"

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Cellular"
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            else -> "Other"
        }
    }

    /**
     * Check if current connection is high-speed
     */
    fun isConnectionFast(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                // Check if it's at least 4G
                activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED) &&
                        (activeNetwork.linkDownstreamBandwidthKbps >= 1000)
            }
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    /**
     * Observe network connectivity changes
     */
    fun observeNetworkState(context: Context): Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(networkRequest, callback)
        // Set initial value
        val isConnected = isNetworkAvailable(context)
        trySend(isConnected)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    suspend fun checkNetworkSpeed(): Long {
        return try {
            val url = URL("https://www.google.com") // lightweight endpoint
            val startTime = System.currentTimeMillis()

            val connection = withContext(Dispatchers.IO) {
                url.openConnection() as HttpURLConnection
            }
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            val bytesRead = connection.inputStream.use { it.readBytes().size }

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime // ms
            Log.d("NETWORK SPEED", "Duration: $duration, Bytes: $bytesRead")

            if (duration > 0) {
                ((bytesRead.toDouble() / duration) * 1000 / 1024).toLong() // KB/s
            } else 0
        } catch (e: Exception) {
            0
        }
    }
    suspend fun checkNetworkLatency(): Long {
        return try {
            val url = URL("https://clients3.google.com/generate_204")
            // this returns 204 No Content with almost zero bytes

            val startTime = System.currentTimeMillis()

            val connection = withContext(Dispatchers.IO) {
                url.openConnection() as HttpURLConnection
            }
            connection.connectTimeout = 3000
            connection.readTimeout = 3000
            connection.inputStream.use { it.readBytes() } // tiny response

            val endTime = System.currentTimeMillis()
            val duration = endTime - startTime // ms

            duration // just return latency in ms
        } catch (e: Exception) {
            Long.MAX_VALUE // treat as failed
        }
    }
}
