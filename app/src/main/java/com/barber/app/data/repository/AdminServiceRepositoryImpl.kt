package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.remote.api.AdminServiceApi
import com.barber.app.data.remote.dto.AdminCreateServiceRequest
import com.barber.app.data.remote.dto.AdminUpdateServiceRequest
import com.barber.app.domain.model.Service
import com.barber.app.domain.repository.AdminServiceRepository
import retrofit2.HttpException
import java.math.BigDecimal
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

private fun mapError(e: Exception, fallback: String): String = when (e) {
    is SocketTimeoutException -> "Tiempo de espera agotado."
    is UnknownHostException   -> "Sin conexión a internet."
    is HttpException          -> {
        val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
        val msg  = try { com.google.gson.JsonParser.parseString(body).asJsonObject.get("message")?.asString } catch (_: Exception) { null }
        msg ?: "Error del servidor (${e.code()})"
    }
    else -> e.message ?: fallback
}

class AdminServiceRepositoryImpl @Inject constructor(
    private val api: AdminServiceApi,
) : AdminServiceRepository {

    override suspend fun getAllServices(): Resource<List<Service>> {
        return try {
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            Resource.Success(api.getAllServices().data?.map { it.toDomain() } ?: emptyList())
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al obtener los servicios"))
        }
    }

    override suspend fun createService(
        name: String,
        description: String?,
        estimatedMinutes: Int,
        price: BigDecimal,
    ): Resource<Service> {
        return try {
            val response = api.createService(AdminCreateServiceRequest(name, description, estimatedMinutes, price))
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            Resource.Success(response.data?.toDomain() ?: throw Exception("Error al crear servicio"))
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al crear el servicio"))
        }
    }

    override suspend fun updateService(
        id: Long,
        name: String?,
        description: String?,
        estimatedMinutes: Int?,
        price: BigDecimal?,
    ): Resource<Service> {
        return try {
            val response = api.updateService(id, AdminUpdateServiceRequest(name, description, estimatedMinutes, price))
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            Resource.Success(response.data?.toDomain() ?: throw Exception("Error al actualizar servicio"))
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al actualizar el servicio"))
        }
    }

    override suspend fun deactivateService(id: Long): Resource<Service> {
        return try {
            // Soft delete: backend marca active = false, preserva historial
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            Resource.Success(api.deactivateService(id).data?.toDomain() ?: throw Exception("Error al desactivar servicio"))
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al desactivar el servicio"))
        }
    }

    override suspend fun activateService(id: Long): Resource<Service> {
        return try {
            // Reactivar servicio previamente desactivado
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            Resource.Success(api.activateService(id).data?.toDomain() ?: throw Exception("Error al activar servicio"))
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al activar el servicio"))
        }
    }
}
