package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.ApiResponse
import com.barber.app.data.remote.dto.ChangePasswordRequest
import com.barber.app.data.remote.dto.CreateUserRequest
import com.barber.app.data.remote.dto.UpdateUserRequest
import com.barber.app.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface UserApi {

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): ApiResponse<UserResponse>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): ApiResponse<UserResponse>

    @GET("users")
    suspend fun getAllUsers(): ApiResponse<List<UserResponse>>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body request: UpdateUserRequest): ApiResponse<UserResponse>

    @PATCH("users/{id}/password")
    suspend fun changePassword(@Path("id") id: Long, @Body request: ChangePasswordRequest): ApiResponse<Unit>
}
