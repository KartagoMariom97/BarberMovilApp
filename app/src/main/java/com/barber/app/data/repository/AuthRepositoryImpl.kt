package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.core.network.TokenHolder
import com.barber.app.data.remote.api.AuthApi
import com.barber.app.data.remote.api.ClientApi
import com.barber.app.data.remote.api.UserApi
import com.barber.app.data.remote.dto.ChangePasswordRequest
import com.barber.app.data.remote.dto.CreateClientUserRequest
import com.barber.app.data.remote.dto.LoginRequest
import com.barber.app.data.remote.dto.UpdateUserRequest
import com.barber.app.domain.model.Client
import com.barber.app.domain.repository.AuthRepository
import com.barber.app.data.local.DatabaseCleaner
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val clientApi: ClientApi,
    private val authApi: AuthApi,
    private val userApi: UserApi,
    private val tokenHolder: TokenHolder,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val databaseCleaner: DatabaseCleaner,   // 🔥 NUEVO
) : AuthRepository {

    override suspend fun register(
        nombres: String,
        fechaNacimiento: String,
        dni: String,
        genero: String,
        email: String,
        telefono: String,
        password: String
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
                    password = password
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

    override suspend fun login(email: String, password: String): Resource<Unit> {
        return try {
            val response = authApi.login(
                LoginRequest(
                    email = email.trim().lowercase(),
                    password = password
                )
            )

            userPreferencesRepository.saveAdminSession(
                token = response.token,
                role = response.role,
                userId = response.userId,
                entityId = response.entityId,
                nombres = response.nombres,
                email = response.email,
            )

            tokenHolder.accessToken = response.token

            Resource.Success(Unit)

        } catch (e: HttpException) {
            val msg = when (e.code()) {
                400 -> "Credenciales inválidas."
                401 -> "No autorizado."
                // 403: cuenta deshabilitada — parsea el JSON del backend para obtener el mensaje
                403 -> {
                    val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
                    try {
                        com.google.gson.JsonParser.parseString(body).asJsonObject.get("message")?.asString
                            ?: "Tu cuenta ha sido deshabilitada. Comunícate con el administrador."
                    } catch (_: Exception) {
                        "Tu cuenta ha sido deshabilitada. Comunícate con el administrador."
                    }
                }
                404 -> "No se encontró usuario con ese email."
                else -> "Error del servidor (${e.code()})"
            }
            Resource.Error(msg)

        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al iniciar sesión")
        }
    }

    override suspend fun logout() {

        /**
         * 🔐 1. Limpiar token en memoria
         */
        tokenHolder.clear()

        /**
         * 🔐 2. Limpiar sesión en DataStore
         */
        userPreferencesRepository.clearSession()

        /**
         * 🔥 3. Limpiar completamente Room
         * Esto fuerza que al volver a iniciar sesión
         * todo se vuelva a sincronizar desde el servidor.
         */
        databaseCleaner.clearAll()
    }

    override suspend fun updateAdminProfile(
        userId: Long,
        nombres: String,
        email: String,
        password: String?,
    ): Resource<Unit> {
        return try {
            userApi.updateUser(userId, UpdateUserRequest(nombres = nombres, email = email, password = password))
            userPreferencesRepository.updateNombresEmail(nombres, email)
            Resource.Success(Unit)
        } catch (e: HttpException) {
            val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            val msg  = try { com.google.gson.JsonParser.parseString(body).asJsonObject.get("message")?.asString } catch (_: Exception) { null }
            Resource.Error(msg ?: "Error del servidor (${e.code()})")
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al actualizar el perfil")
        }
    }

    override suspend fun changePassword(userId: Long, newPassword: String): Resource<Unit> {
        return try {
            userApi.changePassword(userId, ChangePasswordRequest(newPassword))
            Resource.Success(Unit)
        } catch (e: HttpException) {
            val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
            val msg  = try { com.google.gson.JsonParser.parseString(body).asJsonObject.get("message")?.asString } catch (_: Exception) { null }
            Resource.Error(msg ?: "Error del servidor (${e.code()})")
        } catch (e: SocketTimeoutException) {
            Resource.Error("Tiempo de espera agotado.")
        } catch (e: UnknownHostException) {
            Resource.Error("Sin conexión a internet.")
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Error al cambiar la contraseña")
        }
    }
}
