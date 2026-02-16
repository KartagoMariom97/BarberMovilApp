package com.barber.app.domain.model

data class User(
    val id: Long,
    val nombres: String,
    val fechaNacimiento: String,
    val dni: String,
    val genero: String,
    val email: String,
    val telefono: String,
    val role: String,
    val createdAt: String,
)
