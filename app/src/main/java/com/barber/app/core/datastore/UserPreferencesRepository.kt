package com.barber.app.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val CLIENT_ID = longPreferencesKey("client_id")
        val USER_ID = longPreferencesKey("user_id")
        val NOMBRES = stringPreferencesKey("nombres")
        val EMAIL = stringPreferencesKey("email")
        val TELEFONO = stringPreferencesKey("telefono")
        val DNI = stringPreferencesKey("dni")
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val TOKEN = stringPreferencesKey("token")
        val ROLE = stringPreferencesKey("role")
        val ENTITY_ID = longPreferencesKey("entity_id")
    }

    val userPreferences: Flow<UserPreferences> = dataStore.data.map { prefs ->
        UserPreferences(
            clientId = prefs[CLIENT_ID] ?: -1L,
            userId = prefs[USER_ID] ?: -1L,
            nombres = prefs[NOMBRES] ?: "",
            email = prefs[EMAIL] ?: "",
            telefono = prefs[TELEFONO] ?: "",
            dni = prefs[DNI] ?: "",
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
            token = prefs[TOKEN] ?: "",
            role = prefs[ROLE] ?: "",
            entityId = prefs[ENTITY_ID] ?: -1L,
        )
    }

    suspend fun saveSession(
        clientId: Long,
        userId: Long,
        nombres: String,
        email: String,
        telefono: String,
        dni: String = "",
    ) {
        dataStore.edit { prefs ->
            prefs[CLIENT_ID] = clientId
            prefs[USER_ID] = userId
            prefs[NOMBRES] = nombres
            prefs[EMAIL] = email
            prefs[TELEFONO] = telefono
            prefs[DNI] = dni
            prefs[IS_LOGGED_IN] = true
        }
    }

    suspend fun saveAdminSession(
        token: String,
        role: String,
        userId: Long,
        entityId: Long,
        nombres: String,
        email: String,
    ) {
        dataStore.edit { prefs ->
            prefs[TOKEN] = token
            prefs[ROLE] = role
            prefs[USER_ID] = userId
            prefs[ENTITY_ID] = entityId
            prefs[NOMBRES] = nombres
            prefs[EMAIL] = email
            prefs[IS_LOGGED_IN] = true
        }
    }

    suspend fun getTokenOnce(): String? {
        return dataStore.data.map { it[TOKEN] }.firstOrNull()?.takeIf { it.isNotEmpty() }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}
