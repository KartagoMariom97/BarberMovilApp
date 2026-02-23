package com.barber.app.domain.model

import java.math.BigDecimal

data class AdminBarber(
    val codigoBarbero: Long,
    val userId: Long,
    val nombres: String,
    val dni: String,
    val fechaNacimiento: String,
    val genero: String,
    val email: String,
    val telefono: String,
    val active: Boolean,
    val createdAt: String,
    val updatedAt: String,
)

data class AdminClient(
    val codigoCliente: Long,
    val userId: Long,
    val nombres: String,
    val dni: String,
    val fechaNacimiento: String,
    val genero: String,
    val email: String,
    val telefono: String,
    val createdAt: String,
)

data class AdminBooking(
    val id: Long,
    val clientId: Long,
    val clientName: String,
    val barberId: Long,
    val barberName: String,
    val fechaReserva: String,
    val status: String,
    val startTime: String,
    val endTime: String?,
    val totalMinutes: Int,
    val createdAt: String,
    val services: List<BookingServiceDetail> = emptyList(),
)
