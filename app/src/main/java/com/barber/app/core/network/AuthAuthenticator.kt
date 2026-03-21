package com.barber.app.core.network

import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.data.remote.api.AuthApi
import com.barber.app.data.remote.dto.RefreshTokenRequest
import dagger.Lazy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val tokenHolder: TokenHolder,
    private val userPreferencesRepository: UserPreferencesRepository,
    // [F1] Lazy<AuthApi> rompe la dependencia circular:
    //   AuthApi → OkHttpClient → AuthAuthenticator → AuthApi
    // Dagger resuelve AuthApi sólo cuando authenticate() es invocado en runtime,
    // momento en que todos los singletons ya están construidos.
    private val authApi: Lazy<AuthApi>,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Guard: si ya intentamos el refresh y seguimos recibiendo 401, no reintentar.
        // Evita bucle infinito cuando el refresh token también está expirado.
        if (response.responseCount > 1) {
            clearSession()
            return null
        }

        // Recuperar refresh token — primero memoria, luego DataStore (sobrevive reinicios)
        val refreshToken = tokenHolder.refreshToken
            ?: runBlocking { userPreferencesRepository.getRefreshTokenOnce() }

        if (refreshToken.isNullOrBlank()) {
            clearSession()
            return null
        }

        return try {
            // runBlocking es seguro aquí: OkHttp Authenticator siempre corre en hilo IO,
            // nunca en el hilo principal, por lo que no produce ANR.
            val data = runBlocking {
                authApi.get().refresh(RefreshTokenRequest(refreshToken))
            }.data ?: run { clearSession(); return null }

            // Actualizar tokens en memoria
            tokenHolder.accessToken  = data.token
            tokenHolder.refreshToken = data.refreshToken

            // Persistir nuevos tokens en DataStore de forma asíncrona
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                userPreferencesRepository.saveAdminSession(
                    token    = data.token,
                    role     = data.role,
                    userId   = data.userId,
                    entityId = data.entityId,
                    nombres  = data.nombres,
                    email    = data.email,
                )
                data.refreshToken?.let { userPreferencesRepository.saveRefreshToken(it) }
            }

            // Reintentar la request original con el nuevo access token
            response.request.newBuilder()
                .header("Authorization", "Bearer ${data.token}")
                .build()

        } catch (e: Exception) {
            clearSession()
            null
        }
    }

    // Cuenta cuántas respuestas encadenadas hay — OkHttp enlaza previas via priorResponse
    private val Response.responseCount: Int
        get() = generateSequence(this) { it.priorResponse }.count()

    private fun clearSession() {
        tokenHolder.clear()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            userPreferencesRepository.clearSession()
        }
        // Señal a MainActivity para navegar a Login
        tokenHolder.sessionExpired.value = true
    }
}
