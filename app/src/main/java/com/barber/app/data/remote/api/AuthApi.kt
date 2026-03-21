package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.ApiResponse
import com.barber.app.data.remote.dto.LoginRequest
import com.barber.app.data.remote.dto.LoginResponse
import com.barber.app.data.remote.dto.RefreshTokenRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    // [F1] Renueva el access token sin re-login usando el refresh token
    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshTokenRequest): ApiResponse<LoginResponse>
}
