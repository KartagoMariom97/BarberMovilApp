package com.barber.app.domain.model

data class Client(
    val codigoCliente: Long,
    val userId: Long,
    val nombres: String,
    val email: String,
    val createdAt: String,
)

data class ClientProfile(
    val id: Long,
    val nombres: String,
    val email: String,
    val telefono: String,
)
