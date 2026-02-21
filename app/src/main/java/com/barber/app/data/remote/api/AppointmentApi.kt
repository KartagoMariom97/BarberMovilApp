package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.BookingDetailResponse
import com.barber.app.data.remote.dto.BookingResponse
import com.barber.app.data.remote.dto.BookingWithServicesResponse
import com.barber.app.data.remote.dto.CreateBookingRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AppointmentApi {

    @POST("bookings")
    suspend fun createBooking(@Body request: CreateBookingRequest): BookingResponse

    @GET("bookings")
    suspend fun getAllBookings(): List<BookingWithServicesResponse>

    @GET("bookings/{id}")
    suspend fun getBookingById(@Path("id") id: Long): BookingDetailResponse

    @GET("bookings/status/{status}")
    suspend fun getBookingsByStatus(@Path("status") status: String): List<BookingWithServicesResponse>

    @PUT("bookings/{id}/cancel")
    suspend fun cancelBooking(@Path("id") id: Long): BookingResponse

    @PUT("bookings/{id}/complete")
    suspend fun completeBooking(@Path("id") id: Long): BookingResponse

    @PUT("bookings/{id}")
    suspend fun updateBooking(@Path("id") bookingId: Long,@Body request: CreateBookingRequest)
}
