package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.remote.api.ServiceApi
import com.barber.app.domain.model.Service
import com.barber.app.domain.repository.ServiceRepository
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ServiceRepositoryImpl @Inject constructor(
    private val serviceApi: ServiceApi
) : ServiceRepository {

    override suspend fun getAllServices(): Resource<List<Service>> {
        return try {
            val response = serviceApi.getAllServices()
            Resource.Success(response.map { it.toDomain() })
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener los servicios")
        }
    }
}
