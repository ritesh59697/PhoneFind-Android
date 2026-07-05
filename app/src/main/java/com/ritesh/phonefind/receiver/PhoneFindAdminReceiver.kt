package com.ritesh.phonefind.receiver

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent
import com.ritesh.phonefind.data.local.SecurePreferences

class PhoneFindAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
    }

    override fun onDisableRequested(context: Context, intent: Intent): CharSequence {
        val prefs = SecurePreferences.getInstance(context)
        val savedPin = prefs.pin

        return if (!savedPin.isNullOrEmpty()) {
            "SECURITY WARNING: Deactivating PhoneFind Device Admin requires entering your PhoneFind PIN inside the application. Deactivation removes anti-theft protection."
        } else {
            "SECURITY WARNING: Deactivating PhoneFind Device Admin removes anti-theft protection and remote lock features."
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
    }
}
