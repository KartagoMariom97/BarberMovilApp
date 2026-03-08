package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.local.dao.ServiceDao
import com.barber.app.data.local.entity.toDomain
import com.barber.app.data.remote.api.ServiceApi
import com.barber.app.domain.model.Service
import com.barber.app.domain.repository.ServiceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ServiceRepositoryImpl @Inject constructor(
    private val serviceApi: ServiceApi,
    private val serviceDao: ServiceDao,
) : ServiceRepository {

    override suspend fun getAllServices(): Resource<List<Service>> {
        val cached = serviceDao.getAllServices()
        if (cached.isNotEmpty()) {
            // Devuelve caché inmediatamente y sincroniza en background
            CoroutineScope(Dispatchers.IO).launch { syncServices() }
            return Resource.Success(cached.map { it.toDomain() })
        }
        return syncServices()
    }

    private suspend fun syncServices(): Resource<List<Service>> {
        return try {
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            val result = serviceApi.getAllServices().data ?: emptyList()
            serviceDao.upsertAll(result.map { it.toEntity() })
            Resource.Success(result.map { it.toDomain() })
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener los servicios")
        }
    }
}
