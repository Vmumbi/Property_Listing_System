package com.example.property_listing_system.data.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume

class PropertyLocationManager(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    /**
     * Retrieves the current device coordinates using Google Play Services Location.
     * Requires ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION.
     */
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = withContext(Dispatchers.IO) {
        val cts = CancellationTokenSource()
        suspendCancellableCoroutine { continuation ->
            try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cts.token
                ).addOnSuccessListener { location ->
                    if (continuation.isActive) continuation.resume(location)
                }.addOnFailureListener {
                    // Try last location fallback
                    fusedLocationClient.lastLocation.addOnSuccessListener { fallback ->
                        if (continuation.isActive) continuation.resume(fallback)
                    }.addOnFailureListener {
                        if (continuation.isActive) continuation.resume(null)
                    }
                }
            } catch (e: SecurityException) {
                if (continuation.isActive) continuation.resume(null)
            } catch (e: Exception) {
                if (continuation.isActive) continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cts.cancel()
            }
        }
    }

    /**
     * Converts GPS coordinates into a street/city address.
     */
    suspend fun getAddressFromCoordinates(latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)

                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val line = address.getAddressLine(0)
                    if (!line.isNullOrBlank()) line else "${address.locality ?: ""}, ${address.countryName ?: ""}"
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }

    /**
     * Geocodes an address string into latitude and longitude coordinates.
     */
    suspend fun getCoordinatesFromAddress(addressString: String): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) return@withContext null
                val geocoder = Geocoder(context, Locale.getDefault())

                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(addressString, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    Pair(address.latitude, address.longitude)
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
}
