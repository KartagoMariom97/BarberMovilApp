package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.BarberResponse
import retrofit2.http.GET
import retrofit2.http.Path

interface BarberApi {

    @GET("barbers/active")
    suspend fun getActiveBarbers(): List<BarberResponse>

    @GET("barbers/{id}")
    suspend fun getBarberById(@Path("id") id: Long): BarberResponse
}
