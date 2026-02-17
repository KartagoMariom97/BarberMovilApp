package com.barber.app.core.datastore

data class UserPreferences(
    val clientId: Long = -1L,
    val userId: Long = -1L,
    val nombres: String = "",
    val email: String = "",
    val telefono: String = "",
    val dni: String = "",
    val isLoggedIn: Boolean = false,
)
