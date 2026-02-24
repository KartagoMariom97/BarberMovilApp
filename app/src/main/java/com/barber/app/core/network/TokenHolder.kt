package com.barber.app.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenHolder @Inject constructor() {
    @Volatile
    var accessToken: String? = null

    @Volatile
    var refreshToken: String? = null

    /**
     * true cuando el JWT expira (401).
     * StateFlow garantiza que el evento no se pierde aunque la UI aún no esté suscrita.
     * Resetear a false en MainActivity tras mostrar el diálogo.
     */
    val sessionExpired = MutableStateFlow(false)

    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
