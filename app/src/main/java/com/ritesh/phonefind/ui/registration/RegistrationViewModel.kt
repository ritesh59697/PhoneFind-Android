package com.ritesh.phonefind.ui.registration

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.ritesh.phonefind.data.local.SecurePreferences
import com.ritesh.phonefind.data.model.DeviceRegisterRequest
import com.ritesh.phonefind.data.remote.ApiClient
import com.ritesh.phonefind.util.PhoneUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class RegistrationState {
    object Idle : RegistrationState()
    object InProgress : RegistrationState()
    data class Success(val deviceId: String, val deviceKey: String) : RegistrationState()
    data class Error(val message: String) : RegistrationState()
}

class RegistrationViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = SecurePreferences.getInstance(application)

    private val _state = MutableStateFlow<RegistrationState>(
        if (!prefs.deviceId.isNullOrEmpty() && !prefs.deviceKey.isNullOrEmpty()) {
            RegistrationState.Success(prefs.deviceId!!, prefs.deviceKey!!)
        } else {
            RegistrationState.Idle
        }
    )
    val state: StateFlow<RegistrationState> = _state.asStateFlow()

    fun registerDevice() {
        val userToken = prefs.userToken
        if (userToken.isNullOrEmpty()) {
            _state.value = RegistrationState.Error("User authentication token missing. Please log in again.")
            return
        }

        viewModelScope.launch {
            _state.value = RegistrationState.InProgress
            try {
                // Fetch FCM Token
                val fcmToken = try {
                    FirebaseMessaging.getInstance().token.await()
                } catch (e: Exception) {
                    null
                }
                if (!fcmToken.isNullOrEmpty()) {
                    prefs.fcmToken = fcmToken
                }

                val imei = PhoneUtils.getImei(getApplication())
                val deviceModel = PhoneUtils.getDeviceModel()
                val simSerialHash = PhoneUtils.getSimSerialHash(getApplication())

                val request = DeviceRegisterRequest(
                    imei = imei,
                    deviceModel = deviceModel,
                    fcmToken = fcmToken ?: prefs.fcmToken,
                    simSerialHash = simSerialHash
                )

                val bearerToken = if (userToken.startsWith("Bearer ")) userToken else "Bearer $userToken"
                val response = ApiClient.api.registerDevice(bearerToken, request)

                prefs.deviceId = response.deviceId
                prefs.deviceKey = response.deviceKey

                _state.value = RegistrationState.Success(response.deviceId, response.deviceKey)
            } catch (e: Exception) {
                _state.value = RegistrationState.Error(e.localizedMessage ?: "Failed to register device with backend.")
            }
        }
    }
}
