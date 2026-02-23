package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminClient

interface AdminClientRepository {
    suspend fun getAllClients(): Resource<List<AdminClient>>
    suspend fun updateClient(id: Long, nombres: String?, email: String?, telefono: String?): Resource<AdminClient>
}
