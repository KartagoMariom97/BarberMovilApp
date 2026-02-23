package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminBooking

interface AdminBookingRepository {
    suspend fun getAllBookings(status: String? = null, barberId: Long? = null, clientId: Long? = null): Resource<List<AdminBooking>>
    suspend fun getBookingById(id: Long): Resource<AdminBooking>
    suspend fun changeStatus(id: Long, status: String): Resource<AdminBooking>
}
