package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Barber
import com.barber.app.domain.repository.BarberRepository
import javax.inject.Inject

class GetBarbersUseCase @Inject constructor(
    private val barberRepository: BarberRepository
) {
    suspend operator fun invoke(): Resource<List<Barber>> {
        return barberRepository.getActiveBarbers()
    }
}
