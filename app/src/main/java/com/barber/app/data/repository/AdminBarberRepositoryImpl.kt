package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.remote.api.AdminBarberApi
import com.barber.app.data.remote.dto.AdminUpdateBarberRequest
import com.barber.app.domain.model.AdminBarber
import com.barber.app.domain.repository.AdminBarberRepository
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class AdminBarberRepositoryImpl @Inject constructor(
    private val api: AdminBarberApi,
) : AdminBarberRepository {

    override suspend fun getAllBarbers(): Resource<List<AdminBarber>> {
        return try {
            Resource.Success(api.getAllBarbers().map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al obtener los barberos"))
        }
    }

    override suspend fun updateBarber(
        id: Long,
        nombres: String?,
        email: String?,
        telefono: String?,
    ): Resource<AdminBarber> {
        return try {
            val response = api.updateBarber(id, AdminUpdateBarberRequest(nombres = nombres, email = email, telefono = telefono))
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al actualizar el barbero"))
        }
    }

    override suspend fun toggleActive(id: Long): Resource<AdminBarber> {
        return try {
            Resource.Success(api.toggleActive(id).toDomain())
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al cambiar estado del barbero"))
        }
    }
}

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
