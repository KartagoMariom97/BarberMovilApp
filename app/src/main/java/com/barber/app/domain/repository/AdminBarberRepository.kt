package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminBarber

interface AdminBarberRepository {
    suspend fun getAllBarbers(): Resource<List<AdminBarber>>
    /** Crea un barbero nuevo vía POST /barbers/user con todos los campos requeridos */
    suspend fun createBarber(
        nombres: String,
        fechaNacimiento: String,
        dni: String,
        genero: String,
        email: String,
        password: String,
        telefono: String?,
        active: Boolean,
    ): Resource<AdminBarber>
    suspend fun updateBarber(id: Long, nombres: String?, email: String?, telefono: String?): Resource<AdminBarber>
    suspend fun toggleActive(id: Long): Resource<AdminBarber>
}
