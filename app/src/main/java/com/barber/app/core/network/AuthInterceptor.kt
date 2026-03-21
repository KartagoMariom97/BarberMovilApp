package com.barber.app.core.network

import com.barber.app.core.datastore.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor(
    private val tokenHolder: TokenHolder,
    // [F4] Repositorio DataStore para recuperar el token tras reinicio de app
    private val userPreferencesRepository: UserPreferencesRepository,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        // [F4] Si el token en memoria es null (app reiniciada), cargarlo desde DataStore
        if (tokenHolder.accessToken.isNullOrBlank()) {
            tokenHolder.accessToken = runBlocking { userPreferencesRepository.getTokenOnce() }
        }

        val token = tokenHolder.accessToken
        val newRequest = if (!token.isNullOrBlank()) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(newRequest)
    }
}
