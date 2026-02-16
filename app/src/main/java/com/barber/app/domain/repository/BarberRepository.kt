package com.barber.app.domain.repository

import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Barber

interface BarberRepository {
    suspend fun getActiveBarbers(): Resource<List<Barber>>
    suspend fun getBarberById(id: Long): Resource<Barber>
}
