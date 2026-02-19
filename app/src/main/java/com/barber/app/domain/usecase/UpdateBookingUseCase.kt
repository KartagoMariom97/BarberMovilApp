package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.repository.BookingRepository
import javax.inject.Inject

class UpdateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {

    suspend operator fun invoke(
        bookingId: Long,
        clientId: Long,
        barberId: Long,
        fecha: String,
        hora: String,
        serviceIds: List<Long>
    ): Resource<Unit> {

        return bookingRepository.updateBooking(
            bookingId = bookingId,
            clientId = clientId,
            barberId = barberId,
            fecha = fecha,
            hora = hora,
            serviceIds = serviceIds
        )
    }
}