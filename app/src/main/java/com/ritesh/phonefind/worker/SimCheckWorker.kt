package com.ritesh.phonefind.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.ritesh.phonefind.data.local.SecurePreferences
import com.ritesh.phonefind.data.model.SimCheckRequest
import com.ritesh.phonefind.data.remote.ApiClient
import com.ritesh.phonefind.util.PhoneUtils

class SimCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = SecurePreferences.getInstance(applicationContext)
        val deviceId = prefs.deviceId
        val deviceKey = prefs.deviceKey

        if (deviceId.isNullOrEmpty() || deviceKey.isNullOrEmpty()) {
            return Result.failure()
        }

        return try {
            val currentSimHash = PhoneUtils.getSimSerialHash(applicationContext) ?: "UNKNOWN"
            val bearerKey = if (deviceKey.startsWith("Bearer ")) deviceKey else "Bearer $deviceKey"

            val response = ApiClient.api.checkSim(
                deviceKey = bearerKey,
                deviceId = deviceId,
                request = SimCheckRequest(simSerialHash = currentSimHash)
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
