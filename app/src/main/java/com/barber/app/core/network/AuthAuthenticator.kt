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
        // If we already tried and failed, don't retry
        if (response.request.header("Authorization") != null) {
            // Token refresh would go here when backend implements JWT
            // For now, clear session on auth failure
            runBlocking {
                tokenHolder.clear()
                userPreferencesRepository.clearSession()
            }
            return null
        }
        return null
    }
}
