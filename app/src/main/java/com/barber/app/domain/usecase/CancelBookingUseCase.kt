package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Booking
import com.barber.app.domain.repository.BookingRepository
import javax.inject.Inject

class CancelBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(bookingId: Long): Resource<Booking> {
        return bookingRepository.cancelBooking(bookingId)
    }
}
