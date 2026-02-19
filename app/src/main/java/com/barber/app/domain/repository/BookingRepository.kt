package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Booking

interface BookingRepository {
    suspend fun createBooking(
        clientId: Long,
        barberId: Long,
        fechaReserva: String,
        startTime: String,
        serviceIds: List<Long>,
    ): Resource<Booking>

    suspend fun getClientBookings(clientId: Long): Resource<List<Booking>>
    suspend fun getBookingById(id: Long): Resource<Booking>
    suspend fun cancelBooking(id: Long): Resource<Booking>
    suspend fun getAllBookings(): Resource<List<Booking>>
    suspend fun updateBooking(
    bookingId: Long,
    clientId: Long,
    barberId: Long,
    fecha: String,
    hora: String,
    serviceIds: List<Long>
    ): Resource<Unit>
}
