package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.ClientProfile

interface ClientRepository {
    suspend fun getClientProfile(clientId: Long): Resource<ClientProfile>
}
