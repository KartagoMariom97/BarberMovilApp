package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.remote.api.AdminClientApi
import com.barber.app.data.remote.dto.AdminCreateClientRequest
import com.barber.app.data.remote.dto.AdminUpdateClientRequest
import com.barber.app.domain.model.AdminClient
import com.barber.app.domain.repository.AdminClientRepository
import retrofit2.HttpException
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

class AdminClientRepositoryImpl @Inject constructor(
    private val api: AdminClientApi,
) : AdminClientRepository {

    override suspend fun getAllClients(): Resource<List<AdminClient>> {
        return try {
            Resource.Success(api.getAllClients().map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al obtener los clientes"))
        }
    }

    /** Crea cliente llamando POST /admin/clients; retorna Unit y el VM recarga la lista */
    override suspend fun createClient(
        nombres: String, fechaNacimiento: String, dni: String, genero: String,
        email: String?, telefono: String, password: String?,
    ): Resource<Unit> {
        return try {
            api.createClient(
                AdminCreateClientRequest(
                    nombres = nombres,
                    fechaNacimiento = fechaNacimiento,
                    dni = dni,
                    genero = genero,
                    email = email,
                    telefono = telefono,
                    password = password,
                ),
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al crear el cliente"))
        }
    }

    override suspend fun updateClient(
        id: Long,
        nombres: String?,
        email: String?,
        telefono: String?,
        password: String?,
        dni: String?,
        genero: String?,
        fechaNacimiento: String?,
    ): Resource<AdminClient> {
        return try {
            val response = api.updateClient(
                id,
                AdminUpdateClientRequest(
                    nombres = nombres,
                    email = email,
                    telefono = telefono,
                    password = password,
                    dni = dni,
                    genero = genero,
                    fechaNacimiento = fechaNacimiento,
                ),
            )
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al actualizar el cliente"))
        }
    }
}
