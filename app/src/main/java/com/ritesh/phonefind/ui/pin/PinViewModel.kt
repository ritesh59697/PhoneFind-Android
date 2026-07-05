package com.ritesh.phonefind.ui.pin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ritesh.phonefind.data.local.SecurePreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PinViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = SecurePreferences.getInstance(application)

    private val _isPinSet = MutableStateFlow(!prefs.pin.isNullOrEmpty())
    val isPinSet: StateFlow<Boolean> = _isPinSet.asStateFlow()

    fun savePin(pin: String): Boolean {
        if (pin.length < 4 || pin.length > 6) return false
        prefs.pin = pin
        _isPinSet.value = true
        return true
    }

    fun validatePin(inputPin: String): Boolean {
        return prefs.pin == inputPin
    }
}
