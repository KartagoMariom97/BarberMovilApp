package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.AdminBarberResponse
import com.barber.app.data.remote.dto.AdminCreateBarberRequest
import com.barber.app.data.remote.dto.AdminUpdateBarberRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AdminBarberApi {

    @GET("admin/barbers")
    suspend fun getAllBarbers(): List<AdminBarberResponse>

    /** Crea un nuevo barbero con cuenta de usuario */
    @POST("barbers/user")
    suspend fun createBarber(@Body request: AdminCreateBarberRequest): AdminBarberResponse

    @PUT("admin/barbers/{id}")
    suspend fun updateBarber(
        @Path("id") id: Long,
        @Body request: AdminUpdateBarberRequest,
    ): AdminBarberResponse

    @PUT("admin/barbers/{id}/toggle-active")
    suspend fun toggleActive(@Path("id") id: Long): AdminBarberResponse
}
