package com.ritesh.phonefind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.ritesh.phonefind.data.local.SecurePreferences
import com.ritesh.phonefind.ui.auth.AuthScreen
import com.ritesh.phonefind.ui.auth.AuthViewModel
import com.ritesh.phonefind.ui.dashboard.DashboardScreen
import com.ritesh.phonefind.ui.pin.PinSetupScreen
import com.ritesh.phonefind.ui.pin.PinViewModel
import com.ritesh.phonefind.ui.registration.DeviceRegistrationScreen
import com.ritesh.phonefind.ui.registration.RegistrationViewModel
import com.ritesh.phonefind.ui.theme.PhoneFindTheme
import com.ritesh.phonefind.worker.WorkManagerScheduler

enum class NavDestination {
    AUTH,
    PIN_SETUP,
    REGISTRATION,
    DASHBOARD
}

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val pinViewModel: PinViewModel by viewModels()
    private val registrationViewModel: RegistrationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = SecurePreferences.getInstance(this)

        setContent {
            PhoneFindTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentDestination by remember {
                        mutableStateOf(
                            when {
                                prefs.userToken.isNullOrEmpty() -> NavDestination.AUTH
                                prefs.pin.isNullOrEmpty() -> NavDestination.PIN_SETUP
                                prefs.deviceId.isNullOrEmpty() || prefs.deviceKey.isNullOrEmpty() -> NavDestination.REGISTRATION
                                else -> {
                                    WorkManagerScheduler.schedulePeriodicJobs(applicationContext)
                                    NavDestination.DASHBOARD
                                }
                            }
                        )
                    }

                    when (currentDestination) {
                        NavDestination.AUTH -> {
                            AuthScreen(
                                viewModel = authViewModel,
                                onAuthSuccess = {
                                    currentDestination = if (prefs.pin.isNullOrEmpty()) {
                                        NavDestination.PIN_SETUP
                                    } else if (prefs.deviceId.isNullOrEmpty() || prefs.deviceKey.isNullOrEmpty()) {
                                        NavDestination.REGISTRATION
                                    } else {
                                        WorkManagerScheduler.schedulePeriodicJobs(applicationContext)
                                        NavDestination.DASHBOARD
                                    }
                                }
                            )
                        }

                        NavDestination.PIN_SETUP -> {
                            PinSetupScreen(
                                viewModel = pinViewModel,
                                onPinCreated = {
                                    currentDestination = if (prefs.deviceId.isNullOrEmpty() || prefs.deviceKey.isNullOrEmpty()) {
                                        NavDestination.REGISTRATION
                                    } else {
                                        WorkManagerScheduler.schedulePeriodicJobs(applicationContext)
                                        NavDestination.DASHBOARD
                                    }
                                }
                            )
                        }

                        NavDestination.REGISTRATION -> {
                            DeviceRegistrationScreen(
                                viewModel = registrationViewModel,
                                onRegistrationComplete = {
                                    WorkManagerScheduler.schedulePeriodicJobs(applicationContext)
                                    currentDestination = NavDestination.DASHBOARD
                                }
                            )
                        }

                        NavDestination.DASHBOARD -> {
                            DashboardScreen(
                                onLogout = {
                                    WorkManagerScheduler.cancelAllJobs(applicationContext)
                                    currentDestination = NavDestination.AUTH
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
