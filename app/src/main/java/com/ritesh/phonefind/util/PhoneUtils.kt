package com.ritesh.phonefind.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.telephony.TelephonyManager
import java.security.MessageDigest

object PhoneUtils {

    fun getDeviceModel(): String {
        val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
        val model = Build.MODEL
        return if (model.startsWith(manufacturer, ignoreCase = true)) {
            model
        } else {
            "$manufacturer $model"
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    fun getSimSerialHash(context: Context): String? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            val simSerial = telephonyManager?.simSerialNumber
            if (!simSerial.isNullOrEmpty()) {
                sha256(simSerial)
            } else {
                // Fallback to subscriber ID or default placeholder if SIM serial unavailable on Android 10+ without privileged access
                val operator = telephonyManager?.simOperator
                if (!operator.isNullOrEmpty()) {
                    sha256(operator)
                } else {
                    sha256("NO_SIM_DETECTED")
                }
            }
        } catch (e: SecurityException) {
            sha256("PERMISSION_DENIED_SIM")
        } catch (e: Exception) {
            sha256("UNKNOWN_SIM_STATE")
        }
    }

    @SuppressLint("HardwareIds", "MissingPermission")
    fun getImei(context: Context): String? {
        return try {
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ restricts direct IMEI access for non-system apps
                "DEVICE_ID_${Build.MODEL.hashCode()}"
            } else {
                @Suppress("DEPRECATION")
                telephonyManager?.deviceId ?: telephonyManager?.imei
            }
        } catch (e: SecurityException) {
            null
        } catch (e: Exception) {
            null
        }
    }

    fun getBatteryPercentage(context: Context): Int {
        return try {
            val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
            val batteryStatus: Intent? = context.registerReceiver(null, intentFilter)
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level >= 0 && scale > 0) {
                ((level / scale.toFloat()) * 100).toInt()
            } else {
                50
            }
        } catch (e: Exception) {
            50
        }
    }

    fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
