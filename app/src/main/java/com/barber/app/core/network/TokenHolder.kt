package com.barber.app.core.network

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenHolder @Inject constructor() {
    @Volatile
    var accessToken: String? = null

    @Volatile
    var refreshToken: String? = null

    fun clear() {
        accessToken = null
        refreshToken = null
    }
}
