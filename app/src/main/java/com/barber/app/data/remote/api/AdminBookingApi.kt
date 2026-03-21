package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.AdminBookingResponse
import com.barber.app.data.remote.dto.AdminChangeStatusRequest
import com.barber.app.data.remote.dto.AdminUpdateBookingRequest
import com.barber.app.data.remote.dto.ApiResponse
import com.barber.app.data.remote.dto.BookingResponse
import com.barber.app.data.remote.dto.CreateBookingRequest
import com.barber.app.data.remote.dto.PagedResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AdminBookingApi {

    /** Crea una reserva nueva; reutiliza el endpoint público POST /bookings */
    @POST("bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): ApiResponse<BookingResponse>

    // [F5] Agrega page y size para paginación; retorna PagedResponse en lugar de List
    @GET("admin/bookings")
    suspend fun getAllBookings(
        @Query("status")   status:   String? = null,
        @Query("barberId") barberId: Long?   = null,
        @Query("clientId") clientId: Long?   = null,
        @Query("page")     page:     Int     = 0,
        @Query("size")     size:     Int     = 20,
    ): ApiResponse<PagedResponse<AdminBookingResponse>>

    @GET("admin/bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): ApiResponse<AdminBookingResponse>

    @PUT("admin/bookings/{id}/status")
    suspend fun changeStatus(
        @Path("id") id: Long,
        @Body request: AdminChangeStatusRequest,
    ): ApiResponse<AdminBookingResponse>

    @PUT("admin/bookings/{id}")
    suspend fun updateBooking(
        @Path("id") id: Long,
        @Body request: AdminUpdateBookingRequest,
    ): ApiResponse<AdminBookingResponse>
}
