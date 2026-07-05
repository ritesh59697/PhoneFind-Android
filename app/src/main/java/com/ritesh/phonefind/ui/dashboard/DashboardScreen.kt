package com.ritesh.phonefind.ui.dashboard

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.location.Location
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
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

    val deviceId = prefs.deviceId ?: "NOT REGISTERED"
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
            title = { Text("ENTER SECURITY PIN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black) },
            text = {
                Column {
                    Text(
                        "Enter your PhoneFind security PIN to deactivate Device Admin protection on this phone:",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputPin,
                        onValueChange = { inputPin = it },
                        placeholder = { Text("PIN", fontFamily = FontFamily.Monospace) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF9F9F7),
                            unfocusedContainerColor = Color(0xFFF9F9F7),
                            focusedBorderColor = Color.Black,
                            unfocusedBorderColor = Color.Black,
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, Color.Black, RoundedCornerShape(8.dp))
                    )
                    pinError?.let {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(it, color = Color(0xFFDC2626), fontFamily = FontFamily.Monospace, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (prefs.pin == inputPin) {
                            dpm.removeActiveAdmin(adminComponent)
                            isAdminActive = false
                            showPinDialog = false
                            inputPin = ""
                            pinError = null
                        } else {
                            pinError = "INCORRECT PIN."
                        }
                    }
                ) {
                    Text("DEACTIVATE ADMIN", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Black, color = Color(0xFFDC2626))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showPinDialog = false
                    inputPin = ""
                    pinError = null
                }) {
                    Text("CANCEL", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F4EE))
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Device Protection",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 26.sp,
                    color = Color.Black
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Box(
                modifier = Modifier
                    .padding(bottom = 20.dp)
                    .background(Color(0xFFFFE600), shape = RoundedCornerShape(6.dp))
                    .border(2.dp, Color.Black, shape = RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "BACKGROUND MONITORING ACTIVE",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }

            statusMessage?.let { msg ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(Color(0xFFD1FAE5), shape = RoundedCornerShape(8.dp))
                        .border(2.dp, Color(0xFF059669), shape = RoundedCornerShape(8.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = msg,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                }
            }

            // Neo-Brutalist Device Info Card
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color.Black, shape = RoundedCornerShape(12.dp))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .border(2.5.dp, Color.Black, shape = RoundedCornerShape(12.dp))
                        .padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DEVICE METRICS",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp,
                            color = Color.Black
                        )
                        Box(
                            modifier = Modifier
                                .background(if (isAdminActive) Color(0xFF00E676) else Color(0xFFFFD1D1), shape = RoundedCornerShape(4.dp))
                                .border(1.5.dp, Color.Black, shape = RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isAdminActive) "ADMIN ACTIVE" else "ADMIN INACTIVE",
                                fontSize = 9.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Black,
                                color = Color.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("MODEL: $deviceModel", fontFamily = FontFamily.Monospace, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("DEVICE ID: $deviceId", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF666666))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Background Services Card
            Box(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color.Black, shape = RoundedCornerShape(12.dp))
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(12.dp))
                        .border(2.5.dp, Color.Black, shape = RoundedCornerShape(12.dp))
                        .padding(18.dp)
                ) {
                    Text(
                        text = "BACKGROUND SERVICE DAEMONS",
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• PERIODIC GPS LOCATION (EVERY 5 MINS)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF444444))
                    Text("• PERIODIC SIM SERIAL CHECK (EVERY 15 MINS)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF444444))
                    Text("• WEB COMMAND AUTO-SYNC (EVERY 5 SECS)", fontFamily = FontFamily.Monospace, fontSize = 10.sp, color = Color(0xFF444444))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Manual Sync Button
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 3.dp, y = 3.dp)
                        .background(Color.Black, shape = RoundedCornerShape(8.dp))
                )

                Button(
                    onClick = {
                        scope.launch {
                            checkAndExecutePendingCommands(context, prefs) { msg ->
                                statusMessage = msg
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "SYNC & RUN WEB COMMANDS NOW",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stop Active Alarm Button
            Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 3.dp, y = 3.dp)
                        .background(Color.Black, shape = RoundedCornerShape(8.dp))
                )

                Button(
                    onClick = { SoundUtils.stopAlarm() },
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFE600),
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        text = "STOP ALARM SOUND",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        color = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Deactivate Admin Button
            if (isAdminActive) {
                Box(modifier = Modifier.fillMaxWidth().height(48.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 3.dp, y = 3.dp)
                            .background(Color.Black, shape = RoundedCornerShape(8.dp))
                    )

                    Button(
                        onClick = { showPinDialog = true },
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp)),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF4D4D),
                            contentColor = Color.Black
                        )
                    ) {
                        Text(
                            text = "DEACTIVATE ADMIN (PIN REQUIRED)",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Logout Link Button
            TextButton(
                onClick = {
                    prefs.clear()
                    onLogout()
                }
            ) {
                Text(
                    text = "[ LOG OUT & RESET LOCAL CLIENT ]",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    color = Color(0xFFDC2626)
                )
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
