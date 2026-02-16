package com.barber.app.domain.model

data class Barber(
    val codigoBarbero: Long,
    val userId: Long,
    val nombres: String,
    val email: String,
    val telefono: String,
    val active: Boolean,
    val createdAt: String,
)
