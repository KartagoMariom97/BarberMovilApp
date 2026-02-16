package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.remote.api.BarberApi
import com.barber.app.domain.model.Barber
import com.barber.app.domain.repository.BarberRepository
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class BarberRepositoryImpl @Inject constructor(
    private val barberApi: BarberApi
) : BarberRepository {

    override suspend fun getActiveBarbers(): Resource<List<Barber>> {
        return try {
            val response = barberApi.getActiveBarbers()
            Resource.Success(response.map { it.toDomain() })
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener los barberos")
        }
    }

    override suspend fun getBarberById(id: Long): Resource<Barber> {
        return try {
            val response = barberApi.getBarberById(id)
            Resource.Success(response.toDomain())
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener el barbero")
        }
    }
}
