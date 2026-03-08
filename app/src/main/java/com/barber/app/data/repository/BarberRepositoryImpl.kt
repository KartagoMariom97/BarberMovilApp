package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.local.dao.BarberDao
import com.barber.app.data.local.entity.toDomain
import com.barber.app.data.remote.api.BarberApi
import com.barber.app.domain.model.Barber
import com.barber.app.domain.repository.BarberRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class BarberRepositoryImpl @Inject constructor(
    private val barberApi: BarberApi,
    private val barberDao: BarberDao,
) : BarberRepository {

    override suspend fun getActiveBarbers(): Resource<List<Barber>> {
        val cached = barberDao.getActiveBarbers()
        if (cached.isNotEmpty()) {
            // Devuelve caché inmediatamente y sincroniza en background
            CoroutineScope(Dispatchers.IO).launch { syncBarbers() }
            return Resource.Success(cached.map { it.toDomain() })
        }
        return syncBarbers()
    }

    override suspend fun getBarberById(id: Long): Resource<Barber> {
        val cached = barberDao.getBarberById(id)
        if (cached != null) {
            CoroutineScope(Dispatchers.IO).launch { syncBarbers() }
            return Resource.Success(cached.toDomain())
        }
        return try {
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            val response = barberApi.getBarberById(id).data ?: throw Exception("Barbero no encontrado")
            barberDao.upsertAll(listOf(response.toEntity()))
            Resource.Success(response.toDomain())
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener el barbero")
        }
    }

    private suspend fun syncBarbers(): Resource<List<Barber>> {
        return try {
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            val result = barberApi.getActiveBarbers().data ?: emptyList()
            barberDao.upsertAll(result.map { it.toEntity() })
            Resource.Success(result.map { it.toDomain() })
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener los barberos")
        }
    }
}
