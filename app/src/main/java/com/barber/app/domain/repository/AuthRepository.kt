package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Client

interface AuthRepository {
    suspend fun register(
        nombres: String,
        fechaNacimiento: String,
        dni: String,
        genero: String,
        email: String,
        telefono: String,
    ): Resource<Client>

    suspend fun login(email: String): Resource<Client>

    suspend fun logout()
}
