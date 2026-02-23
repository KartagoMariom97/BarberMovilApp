package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    /** password es opcional — si está vacío el backend permite login sin contraseña (CLIENT role) */
    suspend operator fun invoke(email: String, password: String = ""): Resource<Unit> {
        if (email.isBlank()) return Resource.Error("El email no puede estar vacío")
        return authRepository.login(email, password)
    }
}
