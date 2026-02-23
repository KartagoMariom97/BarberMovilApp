package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.AdminBookingResponse
import com.barber.app.data.remote.dto.AdminChangeStatusRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminBookingApi {

    @GET("admin/bookings")
    suspend fun getAllBookings(
        @Query("status") status: String? = null,
        @Query("barberId") barberId: Long? = null,
        @Query("clientId") clientId: Long? = null,
    ): List<AdminBookingResponse>

    @GET("admin/bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): AdminBookingResponse

    @PUT("admin/bookings/{id}/status")
    suspend fun changeStatus(
        @Path("id") id: Long,
        @Body request: AdminChangeStatusRequest,
    ): AdminBookingResponse
}
