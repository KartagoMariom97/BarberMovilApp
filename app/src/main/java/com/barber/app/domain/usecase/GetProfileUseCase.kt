package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.ClientProfile
import com.barber.app.domain.repository.ClientRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(clientId: Long): Resource<ClientProfile> {
        return clientRepository.getClientProfile(clientId)
    }
}
