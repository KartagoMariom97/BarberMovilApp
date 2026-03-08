package com.barber.app.core.network

import com.barber.app.core.datastore.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import javax.inject.Inject

class AuthAuthenticator @Inject constructor(
    private val tokenHolder: TokenHolder,
    private val userPreferencesRepository: UserPreferencesRepository,
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        if (response.request.header("Authorization") != null) {
            // [MEJORA] Eliminado runBlocking: bloqueaba el hilo de OkHttp y podía causar ANR.
            // tokenHolder.clear() es in-memory (@Volatile), no necesita coroutine.
            // clearSession() se lanza en un scope IO independiente sin bloquear OkHttp.
            tokenHolder.clear()
            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                userPreferencesRepository.clearSession()
            }
            // MutableStateFlow.value es thread-safe; señaliza a MainActivity para navegar al Login
            tokenHolder.sessionExpired.value = true
            return null
        }
        return null
    }
}
