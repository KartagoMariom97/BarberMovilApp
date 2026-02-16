package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Booking
import com.barber.app.domain.repository.BookingRepository
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(
        clientId: Long,
        barberId: Long,
        fechaReserva: String,
        startTime: String,
        serviceIds: List<Long>,
    ): Resource<Booking> {
        if (serviceIds.isEmpty()) return Resource.Error("Debe seleccionar al menos un servicio")
        return bookingRepository.createBooking(clientId, barberId, fechaReserva, startTime, serviceIds)
    }
}
