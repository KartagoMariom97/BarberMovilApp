package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.remote.api.AdminBarberApi
import com.barber.app.data.remote.dto.AdminCreateBarberRequest
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

    /** Crea barbero llamando POST /barbers/user con todos los campos requeridos */
    override suspend fun createBarber(
        nombres: String,
        fechaNacimiento: String,
        dni: String,
        genero: String,
        email: String,
        password: String,
        telefono: String?,
        active: Boolean,
    ): Resource<AdminBarber> {
        return try {
            val response = api.createBarber(
                AdminCreateBarberRequest(
                    nombres = nombres,
                    fechaNacimiento = fechaNacimiento,
                    dni = dni,
                    genero = genero,
                    email = email,
                    password = password,
                    telefono = telefono,
                    active = active,
                ),
            )
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            Resource.Success(response.data?.toDomain() ?: throw Exception("Error al crear barbero"))
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al crear el barbero"))
        }
    }

    override suspend fun getAllBarbers(): Resource<List<AdminBarber>> {
        return try {
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            Resource.Success(api.getAllBarbers().data?.map { it.toDomain() } ?: emptyList())
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al obtener los barberos"))
        }
    }

    override suspend fun updateBarber(
        id: Long,
        nombres: String?,
        email: String?,
        telefono: String?,
        password: String?,
        dni: String?,
        genero: String?,
        fechaNacimiento: String?,
    ): Resource<AdminBarber> {
        return try {
            val response = api.updateBarber(
                id,
                AdminUpdateBarberRequest(
                    nombres = nombres,
                    email = email,
                    telefono = telefono,
                    password = password,
                    dni = dni,
                    genero = genero,
                    fechaNacimiento = fechaNacimiento,
                ),
            )
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            Resource.Success(response.data?.toDomain() ?: throw Exception("Error al actualizar barbero"))
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al actualizar el barbero"))
        }
    }

    override suspend fun toggleActive(id: Long): Resource<AdminBarber> {
        return try {
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            Resource.Success(api.toggleActive(id).data?.toDomain() ?: throw Exception("Error al cambiar estado"))
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
