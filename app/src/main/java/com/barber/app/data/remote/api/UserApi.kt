package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.CreateUserRequest
import com.barber.app.data.remote.dto.UserResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface UserApi {

    @POST("users")
    suspend fun createUser(@Body request: CreateUserRequest): UserResponse

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): UserResponse

    @GET("users")
    suspend fun getAllUsers(): List<UserResponse>
}
