package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Service
import java.math.BigDecimal

interface AdminServiceRepository {
    suspend fun getAllServices(): Resource<List<Service>>
    suspend fun createService(name: String, description: String?, estimatedMinutes: Int, price: BigDecimal): Resource<Service>
    suspend fun updateService(id: Long, name: String?, description: String?, estimatedMinutes: Int?, price: BigDecimal?): Resource<Service>
    suspend fun deleteService(id: Long): Resource<Unit>
}
