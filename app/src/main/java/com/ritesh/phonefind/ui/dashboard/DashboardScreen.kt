package com.ritesh.phonefind.ui.dashboard

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.location.Location
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.ritesh.phonefind.data.local.SecurePreferences
import com.ritesh.phonefind.data.model.CommandStatusRequest
import com.ritesh.phonefind.data.model.LocationUpdateRequest
import com.ritesh.phonefind.data.remote.ApiClient
import com.ritesh.phonefind.receiver.PhoneFindAdminReceiver
import com.ritesh.phonefind.util.PhoneUtils
import com.ritesh.phonefind.util.SoundUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun DashboardScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { SecurePreferences.getInstance(context) }
    val scope = rememberCoroutineScope()

    val deviceId = prefs.deviceId ?: "Not Registered"
    val deviceModel = remember { PhoneUtils.getDeviceModel() }

    var showPinDialog by remember { mutableStateOf(false) }
    var inputPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, PhoneFindAdminReceiver::class.java)
    var isAdminActive by remember { mutableStateOf(dpm.isAdminActive(adminComponent)) }

    // Automatic 5-second polling loop to poll GET /api/devices/:id/commands and execute pending commands
    LaunchedEffect(Unit) {
        while (true) {
            checkAndExecutePendingCommands(context, prefs) { msg ->
                statusMessage = msg
            }
            delay(5000)
        }
    }

    if (showPinDialog) {
        AlertDialog(
            onDismissRequest = {
                showPinDialog = false
                inputPin = ""
                pinError = null
            },
            title = { Text("Enter Security PIN") },
            text = {
                Column {
                    Text("Enter your PhoneFind security PIN to deactivate Device Admin protection:")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputPin,
                        onValueChange = { inputPin = it },
                        label = { Text("PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                    pinError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (prefs.pin == inputPin) {
                            dpm.removeActiveAdmin(adminComponent)
                            isAdminActive = false
                            showPinDialog = false
                            inputPin = ""
                            pinError = null
                        } else {
                            pinError = "Incorrect PIN."
                        }
                    }
                ) {
                    Text("Deactivate Admin")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    inputPin = ""
                    pinError = null
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Device Protection Active",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "PhoneFind client background monitoring is running.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            statusMessage?.let {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Device Info", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Model: $deviceModel", style = MaterialTheme.typography.bodyMedium)
                    Text("Device ID: $deviceId", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Device Admin: ", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = if (isAdminActive) "ACTIVE" else "INACTIVE",
                            fontWeight = FontWeight.Bold,
                            color = if (isAdminActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Background Services", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Periodic Location Check (Every 5 mins)", style = MaterialTheme.typography.bodySmall)
                    Text("• Periodic SIM Check (Every 15 mins)", style = MaterialTheme.typography.bodySmall)
                    Text("• Auto Pending Command Sync (Every 5 secs)", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    scope.launch {
                        checkAndExecutePendingCommands(context, prefs) { msg ->
                            statusMessage = msg
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sync & Run Web Commands Now")
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = { SoundUtils.stopAlarm() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Stop Active Alarm Sound")
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isAdminActive) {
                Button(
                    onClick = { showPinDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Deactivate Device Admin (PIN Required)")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextButton(
                onClick = {
                    prefs.clear()
                    onLogout()
                }
            ) {
                Text("Log Out / Reset App Data", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun checkAndExecutePendingCommands(
    context: Context,
    prefs: SecurePreferences,
    onStatusMessage: (String) -> Unit
) {
    val deviceId = prefs.deviceId ?: return
    val deviceKey = prefs.deviceKey ?: return
    val bearerKey = if (deviceKey.startsWith("Bearer ")) deviceKey else "Bearer $deviceKey"

    try {
        val response = ApiClient.api.getPendingCommands(bearerKey, deviceId)
        for (cmd in response.commands) {
            val success = executeCommand(context, prefs, cmd.type)
            ApiClient.api.updateCommandStatus(
                deviceKey = bearerKey,
                commandId = cmd.id,
                request = CommandStatusRequest(if (success) "executed" else "failed")
            )
            onStatusMessage("Executed web command '${cmd.type}' successfully.")
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@SuppressLint("MissingPermission")
private suspend fun executeCommand(context: Context, prefs: SecurePreferences, type: String): Boolean {
    val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val adminComponent = ComponentName(context, PhoneFindAdminReceiver::class.java)

    return when (type.lowercase()) {
        "lock" -> {
            try {
                if (dpm.isAdminActive(adminComponent)) {
                    dpm.lockNow()
                    true
                } else false
            } catch (e: Exception) { false }
        }
        "alarm" -> {
            try {
                SoundUtils.playAlarmAtMaxVolume(context)
                true
            } catch (e: Exception) { false }
        }
        "wipe" -> {
            try {
                if (dpm.isAdminActive(adminComponent)) {
                    dpm.wipeData(0)
                    true
                } else false
            } catch (e: Exception) { false }
        }
        "locate" -> {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                val cancellationToken = CancellationTokenSource()

                var location: Location? = try {
                    fusedLocationClient.getCurrentLocation(
                        Priority.PRIORITY_HIGH_ACCURACY,
                        cancellationToken.token
                    ).await()
                } catch (e: Exception) { null }

                if (location == null) {
                    location = try {
                        fusedLocationClient.lastLocation.await()
                    } catch (e: Exception) { null }
                }

                if (location != null) {
                    val batteryPct = PhoneUtils.getBatteryPercentage(context)
                    val isoFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }
                    val capturedAt = isoFormatter.format(Date(location.time))

                    val deviceId = prefs.deviceId ?: return false
                    val deviceKey = prefs.deviceKey ?: return false
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
                } else false
            } catch (e: Exception) { false }
        }
        else -> false
    }
}
