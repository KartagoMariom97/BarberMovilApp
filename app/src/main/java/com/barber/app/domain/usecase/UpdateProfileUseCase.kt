package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.ClientProfile
import com.barber.app.domain.repository.ClientRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val clientRepository: ClientRepository
) {
    suspend operator fun invoke(
        clientId: Long,
        nombres: String,
        genero: String,
        email: String,
        telefono: String,
        dni: String,
    ): Resource<ClientProfile> {
        if (nombres.isBlank()) return Resource.Error("El nombre no puede estar vacio")
        return clientRepository.updateClientProfile(clientId, nombres, genero, email, telefono, dni)
    }
}
