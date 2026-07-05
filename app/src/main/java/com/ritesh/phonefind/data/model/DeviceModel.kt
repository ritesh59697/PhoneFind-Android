package com.ritesh.phonefind.data.model

import com.google.gson.annotations.SerializedName

data class DeviceRegisterRequest(
    @SerializedName("imei") val imei: String?,
    @SerializedName("deviceModel") val deviceModel: String,
    @SerializedName("fcmToken") val fcmToken: String?,
    @SerializedName("simSerialHash") val simSerialHash: String?
)

data class DeviceRegisterResponse(
    @SerializedName("deviceId") val deviceId: String,
    @SerializedName("deviceKey") val deviceKey: String
)

data class LocationUpdateRequest(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("batteryPct") val batteryPct: Int,
    @SerializedName("capturedAt") val capturedAt: String
)

data class SimCheckRequest(
    @SerializedName("simSerialHash") val simSerialHash: String
)
