package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminClient

interface AdminClientRepository {
    suspend fun getAllClients(): Resource<List<AdminClient>>
    suspend fun updateClient(
        id: Long,
        nombres: String? = null,
        email: String? = null,
        telefono: String? = null,
        password: String? = null,
        dni: String? = null,
        genero: String? = null,
        fechaNacimiento: String? = null,
    ): Resource<AdminClient>
    /** Crea un cliente nuevo vía POST /admin/clients; retorna Unit (el VM recarga la lista) */
    suspend fun createClient(
        nombres: String, fechaNacimiento: String, dni: String, genero: String,
        email: String?, telefono: String, password: String?,
    ): Resource<Unit>
}
