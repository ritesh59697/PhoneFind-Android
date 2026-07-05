package com.ritesh.phonefind.worker

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.ritesh.phonefind.data.local.SecurePreferences
import com.ritesh.phonefind.data.model.LocationUpdateRequest
import com.ritesh.phonefind.data.remote.ApiClient
import com.ritesh.phonefind.util.PhoneUtils
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class LocationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @SuppressLint("MissingPermission")
    override suspend fun doWork(): Result {
        val prefs = SecurePreferences.getInstance(applicationContext)
        val deviceId = prefs.deviceId
        val deviceKey = prefs.deviceKey

        if (deviceId.isNullOrEmpty() || deviceKey.isNullOrEmpty()) {
            return Result.failure()
        }

        return try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
            val cancellationToken = CancellationTokenSource()

            var location: Location? = try {
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                    cancellationToken.token
                ).await()
            } catch (e: Exception) {
                null
            }

            if (location == null) {
                location = try {
                    fusedLocationClient.lastLocation.await()
                } catch (e: Exception) {
                    null
                }
            }

            if (location == null) {
                return Result.retry()
            }

            val batteryPct = PhoneUtils.getBatteryPercentage(applicationContext)
            val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val capturedAt = isoFormatter.format(Date(location.time))

            val bearerKey = if (deviceKey.startsWith("Bearer ")) deviceKey else "Bearer $deviceKey"

            val response = ApiClient.api.updateLocation(
                deviceKey = bearerKey,
                deviceId = deviceId,
                request = LocationUpdateRequest(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    batteryPct = batteryPct,
                    capturedAt = capturedAt
                )
            )

            if (response.isSuccessful) {
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
