package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Service

interface ServiceRepository {
    suspend fun getAllServices(): Resource<List<Service>>
}
