package com.fishbuddy.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.fishbuddy.app.domain.model.UserLocation
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Wraps FusedLocationProvider for async location fetching. */
class LocationService(private val context: Context) {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentLocation(): UserLocation? {
        return try {
            suspendCancellableCoroutine { cont ->
                client.lastLocation.addOnSuccessListener { loc: Location? ->
                    if (loc != null) cont.resume(UserLocation(loc.latitude, loc.longitude, null))
                    else cont.resume(null)
                }.addOnFailureListener { cont.resumeWithException(it) }
            }
        } catch (e: Exception) { null }
    }
}
