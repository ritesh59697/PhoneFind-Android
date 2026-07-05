package com.ritesh.phonefind.ui.registration

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ritesh.phonefind.receiver.PhoneFindAdminReceiver

@Composable
fun DeviceRegistrationScreen(
    viewModel: RegistrationViewModel,
    onRegistrationComplete: () -> Unit
) {
    val context = LocalContext.current
    val registrationState by viewModel.state.collectAsState()

    var hasForegroundLocation by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }
    var hasBackgroundLocation by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var hasPhoneState by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        )
    }
    var isDeviceAdminActive by remember {
        mutableStateOf(
            (context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager)
                .isAdminActive(ComponentName(context, PhoneFindAdminReceiver::class.java))
        )
    }
    var isIgnoringBatteryOptimizations by remember {
        mutableStateOf(
            (context.getSystemService(Context.POWER_SERVICE) as PowerManager)
                .isIgnoringBatteryOptimizations(context.packageName)
        )
    }

    var showRationaleDialog by remember { mutableStateOf(false) }
    var rationaleText by remember { mutableStateOf("") }

    val foregroundPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasForegroundLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        hasPhoneState = permissions[Manifest.permission.READ_PHONE_STATE] == true ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED

        val denied = permissions.filter { !it.value }
        if (denied.isNotEmpty()) {
            rationaleText = "Foreground location and phone state permissions are required for location tracking and SIM monitoring. Please grant them to continue."
            showRationaleDialog = true
        }
    }

    val backgroundLocationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasBackgroundLocation = isGranted ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!isGranted) {
            rationaleText = "Background location allows PhoneFind to track your lost device periodically even when the app is closed. You can grant 'Allow all the time' in System Settings."
            showRationaleDialog = true
        }
    }

    val deviceAdminLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        isDeviceAdminActive = dpm.isAdminActive(ComponentName(context, PhoneFindAdminReceiver::class.java))
    }

    LaunchedEffect(registrationState) {
        if (registrationState is RegistrationState.Success) {
            onRegistrationComplete()
        }
    }

    if (showRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showRationaleDialog = false },
            title = { Text("Permission Required", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold) },
            text = { Text(rationaleText, fontFamily = FontFamily.Monospace, fontSize = 12.sp) },
            confirmButton = {
                TextButton(onClick = {
                    showRationaleDialog = false
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }) {
                    Text("Open Settings", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRationaleDialog = false }) {
                    Text("Dismiss", fontFamily = FontFamily.Monospace)
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Device Registration",
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
                    text = "HARDWARE ONBOARDING STEPS",
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black,
                    color = Color.Black
                )
            }

            // Step 1: Foreground Permissions
            NeoPermissionStepCard(
                title = "1. FOREGROUND PERMISSIONS",
                description = "Location & Read Phone State (SIM serial check)",
                isGranted = hasForegroundLocation && hasPhoneState,
                onRequest = {
                    val perms = mutableListOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.READ_PHONE_STATE
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        perms.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    foregroundPermissionsLauncher.launch(perms.toTypedArray())
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step 2: Background Location
            NeoPermissionStepCard(
                title = "2. BACKGROUND LOCATION",
                description = "Track lost device location even when app is closed",
                isGranted = hasBackgroundLocation,
                onRequest = {
                    if (!hasForegroundLocation) {
                        rationaleText = "You must grant foreground location permission first before enabling background location."
                        showRationaleDialog = true
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step 3: Battery Optimization Exemption
            NeoPermissionStepCard(
                title = "3. BATTERY EXEMPTION",
                description = "Ensures background SIM & GPS checks run reliably 24/7",
                isGranted = isIgnoringBatteryOptimizations,
                onRequest = {
                    try {
                        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                            data = Uri.parse("package:${context.packageName}")
                        }
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        try {
                            val fallbackIntent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                            context.startActivity(fallbackIntent)
                        } catch (ex: Exception) {
                            ex.printStackTrace()
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Step 4: Device Admin Activation
            NeoPermissionStepCard(
                title = "4. DEVICE ADMIN ACTIVATION",
                description = "Enables anti-tamper, remote lock, and wipe protection",
                isGranted = isDeviceAdminActive,
                onRequest = {
                    val adminComponent = ComponentName(context, PhoneFindAdminReceiver::class.java)
                    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN).apply {
                        putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent)
                        putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "PhoneFind requires Device Admin privileges to lock or wipe your phone if stolen.")
                    }
                    deviceAdminLauncher.launch(intent)
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (registrationState is RegistrationState.Error) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(Color(0xFFFFD1D1), shape = RoundedCornerShape(6.dp))
                        .border(2.dp, Color(0xFFDC2626), shape = RoundedCornerShape(6.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = (registrationState as RegistrationState.Error).message,
                        color = Color(0xFFDC2626),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Registration Action Button with Hard Black Offset Shadow
            Box(modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .offset(x = 4.dp, y = 4.dp)
                        .background(Color.Black, shape = RoundedCornerShape(8.dp))
                )

                Button(
                    onClick = { viewModel.registerDevice() },
                    modifier = Modifier
                        .fillMaxSize()
                        .border(2.5.dp, Color.Black, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color.Black,
                        disabledContainerColor = Color(0xFF00E676).copy(alpha = 0.5f)
                    ),
                    enabled = registrationState !is RegistrationState.InProgress
                ) {
                    if (registrationState is RegistrationState.InProgress) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = "REGISTER DEVICE WITH BACKEND",
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NeoPermissionStepCard(
    title: String,
    description: String,
    isGranted: Boolean,
    onRequest: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        // Hard Black Offset Shadow
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(x = 4.dp, y = 4.dp)
                .background(Color.Black, shape = RoundedCornerShape(10.dp))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(10.dp))
                .border(2.5.dp, Color.Black, shape = RoundedCornerShape(10.dp))
                .padding(16.dp)
        ) {
            Text(
                text = title,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Black,
                fontSize = 12.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = Color(0xFF555555),
                lineHeight = 15.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (isGranted) {
                Box(
                    modifier = Modifier
                        .background(Color(0xFF00E676), shape = RoundedCornerShape(6.dp))
                        .border(2.dp, Color.Black, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "✓ PERMISSION GRANTED",
                        color = Color.Black,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Black,
                        fontSize = 10.sp
                    )
                }
            } else {
                Box(modifier = Modifier.height(36.dp)) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .offset(x = 2.dp, y = 2.dp)
                            .background(Color.Black, shape = RoundedCornerShape(6.dp))
                    )
                    Button(
                        onClick = onRequest,
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFE600),
                            contentColor = Color.Black
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, Color.Black, RoundedCornerShape(6.dp))
                    ) {
                        Text(
                            text = "GRANT PERMISSION",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }
}
