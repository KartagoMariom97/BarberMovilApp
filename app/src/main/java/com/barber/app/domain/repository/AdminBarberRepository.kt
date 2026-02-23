package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminBarber

interface AdminBarberRepository {
    suspend fun getAllBarbers(): Resource<List<AdminBarber>>
    suspend fun updateBarber(id: Long, nombres: String?, email: String?, telefono: String?): Resource<AdminBarber>
    suspend fun toggleActive(id: Long): Resource<AdminBarber>
}
