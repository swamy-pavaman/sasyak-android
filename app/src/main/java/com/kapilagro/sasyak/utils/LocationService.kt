package com.kapilagro.sasyak.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class LocationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    data class LocationResult(
        val latitude: Double,
        val longitude: Double
    )

    sealed class LocationError {
        object PermissionDenied : LocationError()
        object LocationDisabled : LocationError()
        data class Error(val message: String) : LocationError()
    }

    suspend fun getLocation(): Result<LocationResult> {
        Log.d("LocationService", "Getting location...")

        // Check permissions
        if (!hasLocationPermission()) {
            Log.e("LocationService", "Location permission denied")
            return Result.failure(Exception("Location permission denied"))
        }

        // Check if location is enabled
        if (!isLocationEnabled()) {
            Log.e("LocationService", "Location services disabled")
            return Result.failure(Exception("Location services disabled"))
        }

        return try {
            val location = getCurrentLocation()
            if (location != null) {
                Log.d("LocationService", "Location obtained: $location")
                Result.success(location)
            } else {
                Log.e("LocationService", "Could not get location")
                Result.failure(Exception("Could not get location"))
            }
        } catch (e: Exception) {
            Log.e("LocationService", "Error getting location", e)
            Result.failure(e)
        }
    }

    fun hasLocationPermission(): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        Log.d("LocationService", "Fine location permission: $fineLocation, Coarse location permission: $coarseLocation")
        return fineLocation || coarseLocation
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        Log.d("LocationService", "GPS enabled: $isGpsEnabled, Network enabled: $isNetworkEnabled")
        return isGpsEnabled || isNetworkEnabled
    }

    private suspend fun getCurrentLocation(): LocationResult? = suspendCancellableCoroutine { continuation ->
        try {
            if (!hasLocationPermission()) {
                Log.e("LocationService", "No location permission in getCurrentLocation")
                continuation.resume(null)
                return@suspendCancellableCoroutine
            }

            val cancellationTokenSource = CancellationTokenSource()

            Log.d("LocationService", "Requesting current location...")

            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    Log.d("LocationService", "Current location: ${location.latitude}, ${location.longitude}")
                    continuation.resume(LocationResult(location.latitude, location.longitude))
                } else {
                    Log.d("LocationService", "Current location null, trying last known location...")
                    // Try to get last known location
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLocation ->
                        if (lastLocation != null) {
                            Log.d("LocationService", "Last known location: ${lastLocation.latitude}, ${lastLocation.longitude}")
                            continuation.resume(LocationResult(lastLocation.latitude, lastLocation.longitude))
                        } else {
                            Log.e("LocationService", "No last known location available")
                            continuation.resume(null)
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("LocationService", "Error getting last location", exception)
                        continuation.resumeWithException(exception)
                    }
                }
            }.addOnFailureListener { exception ->
                Log.e("LocationService", "Error getting current location", exception)
                continuation.resumeWithException(exception)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        } catch (e: SecurityException) {
            Log.e("LocationService", "Security exception", e)
            continuation.resumeWithException(e)
        } catch (e: Exception) {
            Log.e("LocationService", "Unexpected error", e)
            continuation.resumeWithException(e)
        }
    }
}