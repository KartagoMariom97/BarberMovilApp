package com.barber.app.domain.usecase

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Client
import com.barber.app.domain.repository.AuthRepository
import javax.inject.Inject

class RegisterUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(
        nombres: String,
        fechaNacimiento: String,
        dni: String,
        genero: String,
        email: String,
        telefono: String,
    ): Resource<Client> {
        if (nombres.isBlank()) return Resource.Error("El nombre no puede estar vacío")
        if (email.isBlank()) return Resource.Error("El email no puede estar vacío")
        if (dni.isBlank()) return Resource.Error("El DNI no puede estar vacío")
        if (telefono.isBlank()) return Resource.Error("El teléfono no puede estar vacío")

        return authRepository.register(
            nombres = nombres,
            fechaNacimiento = fechaNacimiento,
            dni = dni,
            genero = genero,
            email = email,
            telefono = telefono,
        )
    }
}
