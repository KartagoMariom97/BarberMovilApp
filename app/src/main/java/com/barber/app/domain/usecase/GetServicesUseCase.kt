package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Service
import com.barber.app.domain.repository.ServiceRepository
import javax.inject.Inject

class GetServicesUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository
) {
    suspend operator fun invoke(): Resource<List<Service>> {
        return serviceRepository.getAllServices()
    }
}
