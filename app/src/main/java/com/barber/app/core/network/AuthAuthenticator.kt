package com.barber.app.core.network

import com.barber.app.core.datastore.UserPreferencesRepository
import kotlinx.coroutines.runBlocking
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
            runBlocking {
                tokenHolder.clear()
                userPreferencesRepository.clearSession()
            }
            // MutableStateFlow.value es thread-safe; no necesita coroutine ni runBlocking
            tokenHolder.sessionExpired.value = true
            return null
        }
        return null
    }
}
