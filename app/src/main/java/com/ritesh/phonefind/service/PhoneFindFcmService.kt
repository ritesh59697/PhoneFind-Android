package com.ritesh.phonefind.service

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ritesh.phonefind.data.local.SecurePreferences
import com.ritesh.phonefind.data.model.CommandStatusRequest
import com.ritesh.phonefind.data.model.LocationUpdateRequest
import com.ritesh.phonefind.data.remote.ApiClient
import com.ritesh.phonefind.receiver.PhoneFindAdminReceiver
import com.ritesh.phonefind.util.PhoneUtils
import com.ritesh.phonefind.util.SoundUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class PhoneFindFcmService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val prefs = SecurePreferences.getInstance(applicationContext)
        prefs.fcmToken = token
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val commandId = data["commandId"] ?: data["id"]
        val type = data["type"] ?: data["action"]

        if (commandId.isNullOrEmpty() || type.isNullOrEmpty()) {
            return
        }

        serviceScope.launch {
            val success = processCommand(type, commandId)
            reportCommandStatus(commandId, if (success) "executed" else "failed")
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun processCommand(type: String, commandId: String): Boolean {
        val prefs = SecurePreferences.getInstance(applicationContext)
        val dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(applicationContext, PhoneFindAdminReceiver::class.java)

        return when (type.lowercase()) {
            "lock" -> {
                try {
                    if (dpm.isAdminActive(adminComponent)) {
                        dpm.lockNow()
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            "alarm" -> {
                try {
                    SoundUtils.playAlarmAtMaxVolume(applicationContext)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            "wipe" -> {
                try {
                    if (dpm.isAdminActive(adminComponent)) {
                        dpm.wipeData(0)
                        true
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            "locate" -> {
                try {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
                    val cancellationToken = CancellationTokenSource()

                    var location: Location? = try {
                        fusedLocationClient.getCurrentLocation(
                            Priority.PRIORITY_HIGH_ACCURACY,
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

                    if (location != null) {
                        val batteryPct = PhoneUtils.getBatteryPercentage(applicationContext)
                        val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                            timeZone = TimeZone.getTimeZone("UTC")
                        }
                        val capturedAt = isoFormatter.format(Date(location.time))

                        val deviceId = prefs.deviceId
                        val deviceKey = prefs.deviceKey

                        if (!deviceId.isNullOrEmpty() && !deviceKey.isNullOrEmpty()) {
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
                            response.isSuccessful
                        } else {
                            false
                        }
                    } else {
                        false
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            else -> false
        }
    }

    private suspend fun reportCommandStatus(commandId: String, status: String) {
        val prefs = SecurePreferences.getInstance(applicationContext)
        val deviceKey = prefs.deviceKey

        if (deviceKey.isNullOrEmpty()) return

        try {
            val bearerKey = if (deviceKey.startsWith("Bearer ")) deviceKey else "Bearer $deviceKey"
            ApiClient.api.updateCommandStatus(
                deviceKey = bearerKey,
                commandId = commandId,
                request = CommandStatusRequest(status = status)
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
