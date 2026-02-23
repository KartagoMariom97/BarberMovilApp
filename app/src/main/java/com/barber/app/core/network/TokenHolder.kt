package com.barber.app.core.network

import kotlinx.coroutines.flow.MutableSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenHolder @Inject constructor() {
    @Volatile
    var accessToken: String? = null

    @Volatile
    var refreshToken: String? = null

    /** Emite un evento cuando el token expira (401) — la UI observa esto para redirigir al login */
    val sessionExpiredFlow = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)

    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
