package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.remote.api.ClientApi
import com.barber.app.data.remote.dto.UpdateClientProfileRequest
import com.barber.app.domain.model.ClientProfile
import com.barber.app.domain.repository.ClientRepository
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class ClientRepositoryImpl @Inject constructor(
    private val clientApi: ClientApi
) : ClientRepository {

    override suspend fun getClientProfile(clientId: Long): Resource<ClientProfile> {
        return try {
            val response = clientApi.getClientProfile(clientId)
            Resource.Success(response.toDomain())
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al obtener el perfil")
        }
    }

    override suspend fun updateClientProfile(
        clientId: Long,
        nombres: String,
        genero: String,
        email: String,
        telefono: String,
        dni: String,
    ): Resource<ClientProfile> {
        return try {
            val request = UpdateClientProfileRequest(
                nombres = nombres,
                genero = genero,
                email = email,
                telefono = telefono,
                dni = dni,
            )
            val response = clientApi.updateClientProfile(clientId, request)
            Resource.Success(response.toDomain())
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string()
            val message = try {
                val json = org.json.JSONObject(errorBody ?: "")
                json.optString("message", "Error al actualizar el perfil")
            } catch (_: Exception) {
                "Error al actualizar el perfil"
            }
            Resource.Error(message)
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar el perfil")
        }
    }
}
