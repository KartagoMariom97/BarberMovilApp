package com.barber.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token")         val token: String,
    @SerializedName("tokenType")     val tokenType: String,
    @SerializedName("userId")        val userId: Long,
    @SerializedName("entityId")      val entityId: Long,
    @SerializedName("nombres")       val nombres: String,
    @SerializedName("email")         val email: String,
    @SerializedName("role")          val role: String,
    // [F1] Refresh token para renovar el access token sin re-login
    @SerializedName("refreshToken")  val refreshToken: String? = null,
)

// [F1] Request para el endpoint POST /auth/refresh
data class RefreshTokenRequest(
    @SerializedName("refreshToken") val refreshToken: String
)
