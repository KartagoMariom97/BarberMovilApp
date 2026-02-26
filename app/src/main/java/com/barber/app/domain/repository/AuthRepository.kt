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
        password: String
    ): Resource<Client>

    /** Login cliente vía JWT — password es opcional para compatibilidad con cuentas sin contraseña */
    suspend fun login(email: String, password: String = ""): Resource<Unit>

    suspend fun adminLogin(email: String, password: String, role: String): Resource<Unit>

    suspend fun logout()

    /** Actualiza nombre, email y contraseña del usuario administrador/barbero */
    suspend fun updateAdminProfile(
        userId: Long,
        nombres: String,
        email: String,
        password: String?,
    ): Resource<Unit>

    /** Cambia la contraseña del usuario vía PATCH /users/{id}/password */
    suspend fun changePassword(userId: Long, newPassword: String): Resource<Unit>
}
