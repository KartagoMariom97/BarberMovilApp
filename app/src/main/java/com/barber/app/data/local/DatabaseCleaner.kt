package com.barber.app.data.local

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseCleaner @Inject constructor(
    private val database: BarberDatabase
) {

    /**
     * 🔥 Borra completamente la base de datos local.
     *
     * Se usa exclusivamente cuando el usuario cierra sesión.
     */
    suspend fun clearAll() {
        database.clearAllTablesSafe()
    }
}