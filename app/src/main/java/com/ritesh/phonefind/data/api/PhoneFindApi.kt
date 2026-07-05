package com.ritesh.phonefind.data.api

import com.ritesh.phonefind.data.model.AuthRequest
import com.ritesh.phonefind.data.model.AuthResponse
import com.ritesh.phonefind.data.model.CommandListResponse
import com.ritesh.phonefind.data.model.CommandResponse
import com.ritesh.phonefind.data.model.CommandStatusRequest
import com.ritesh.phonefind.data.model.DeviceRegisterRequest
import com.ritesh.phonefind.data.model.DeviceRegisterResponse
import com.ritesh.phonefind.data.model.LocationUpdateRequest
import com.ritesh.phonefind.data.model.SimCheckRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface PhoneFindApi {

    @POST("api/auth/signup")
    suspend fun signup(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("api/auth/login")
    suspend fun login(
        @Body request: AuthRequest
    ): AuthResponse

    @POST("api/devices/register")
    suspend fun registerDevice(
        @Header("Authorization") userToken: String,
        @Body request: DeviceRegisterRequest
    ): DeviceRegisterResponse

    @POST("api/devices/{id}/location")
    suspend fun updateLocation(
        @Header("Authorization") deviceKey: String,
        @Path("id") deviceId: String,
        @Body request: LocationUpdateRequest
    ): Response<Unit>

    @POST("api/devices/{id}/sim-check")
    suspend fun checkSim(
        @Header("Authorization") deviceKey: String,
        @Path("id") deviceId: String,
        @Body request: SimCheckRequest
    ): Response<Unit>

    @GET("api/devices/{id}/commands")
    suspend fun getPendingCommands(
        @Header("Authorization") deviceKey: String,
        @Path("id") deviceId: String
    ): CommandListResponse

    @POST("api/commands/{id}/status")
    suspend fun updateCommandStatus(
        @Header("Authorization") deviceKey: String,
        @Path("id") commandId: String,
        @Body request: CommandStatusRequest
    ): Response<Unit>
}
