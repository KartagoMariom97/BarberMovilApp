package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Booking
import com.barber.app.domain.repository.BookingRepository
import javax.inject.Inject

class GetClientBookingsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(clientId: Long): Resource<List<Booking>> {
        return bookingRepository.getClientBookings(clientId)
    }
}
