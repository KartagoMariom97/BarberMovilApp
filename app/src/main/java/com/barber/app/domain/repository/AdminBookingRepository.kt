package com.barber.app.domain.repository

import androidx.paging.PagingData
import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminBooking
import kotlinx.coroutines.flow.Flow

interface AdminBookingRepository {
    // [F5] Devuelve Flow<PagingData> para que el ViewModel construya el Pager reactivo al filtro
    fun getPagedBookings(statusFilter: String?): Flow<PagingData<AdminBooking>>
    suspend fun getBookingById(id: Long): Resource<AdminBooking>
    suspend fun changeStatus(id: Long, status: String): Resource<AdminBooking>
    /** Crea una reserva nueva vía POST /bookings */
    suspend fun createBooking(clientId: Long, barberId: Long, fechaReserva: String, startTime: String, serviceIds: List<Long>): Resource<Unit>
    /** Edita una reserva existente (solo PENDING o CONFIRMED) vía PUT /admin/bookings/{id} */
    suspend fun updateBooking(id: Long, barberId: Long, fechaReserva: String, startTime: String, serviceIds: List<Long>): Resource<AdminBooking>
}
