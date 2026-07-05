package com.ritesh.phonefind.data.model

import com.google.gson.annotations.SerializedName

data class CommandResponse(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("status") val status: String,
    @SerializedName("issuedAt") val issuedAt: String?,
    @SerializedName("executedAt") val executedAt: String?
)

data class CommandListResponse(
    @SerializedName("commands") val commands: List<CommandResponse>
)

data class CommandStatusRequest(
    @SerializedName("status") val status: String // "executed" | "failed"
)
