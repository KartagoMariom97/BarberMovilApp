package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.data.remote.api.ClientApi
import com.barber.app.data.remote.dto.CreateClientUserRequest
import com.barber.app.domain.model.Client
import com.barber.app.domain.repository.AuthRepository
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val clientApi: ClientApi,
    private val userPreferencesRepository: UserPreferencesRepository,
) : AuthRepository {

    override suspend fun register(
        nombres: String,
        fechaNacimiento: String,
        dni: String,
        genero: String,
        email: String,
        telefono: String,
    ): Resource<Client> {
        return try {
            val response = clientApi.createClientUser(
                CreateClientUserRequest(
                    nombres = nombres,
                    fechaNacimiento = fechaNacimiento,
                    dni = dni,
                    genero = genero,
                    email = email,
                    telefono = telefono,
                )
            )
            val client = response.toDomain()
            userPreferencesRepository.saveSession(
                clientId = client.codigoCliente,
                userId = client.userId,
                nombres = client.nombres,
                email = client.email,
                telefono = telefono,
                dni = dni,
            )
            Resource.Success(client)
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                400 -> "Datos inválidos o duplicados. Verifica tu información."
                else -> "Error del servidor (${e.code()})"
            }
            Resource.Error(msg)
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al registrar usuario")
        }
    }

    override suspend fun login(email: String): Resource<Client> {
        return try {
            val profile = clientApi.loginByEmail(email)
            val domainProfile = profile.toDomain()

            userPreferencesRepository.saveSession(
                clientId = domainProfile.id,
                userId = domainProfile.id,
                nombres = domainProfile.nombres,
                email = domainProfile.email,
                telefono = domainProfile.telefono,
                dni = domainProfile.dni,
            )

            Resource.Success(
                Client(
                    codigoCliente = domainProfile.id,
                    userId = domainProfile.id,
                    nombres = domainProfile.nombres,
                    email = domainProfile.email,
                    createdAt = "",
                )
            )
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                404 -> "No se encontró un cliente con ese email"
                else -> "Error del servidor (${e.code()})"
            }
            Resource.Error(msg)
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet. Verifica tu red e intenta de nuevo.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al iniciar sesión")
        }
    }

    override suspend fun logout() {
        userPreferencesRepository.clearSession()
    }
}
