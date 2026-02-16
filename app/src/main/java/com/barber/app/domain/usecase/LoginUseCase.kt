package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Client
import com.barber.app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String): Resource<Client> {
        if (email.isBlank()) return Resource.Error("El email no puede estar vacío")
        return authRepository.login(email)
    }
}
