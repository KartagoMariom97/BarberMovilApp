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

    /** Login cliente vía JWT — password es opcional para compatibilidad con cuentas sin contraseña */
    suspend fun login(email: String, password: String = ""): Resource<Unit>

    suspend fun adminLogin(email: String, password: String, role: String): Resource<Unit>

    suspend fun logout()
}
