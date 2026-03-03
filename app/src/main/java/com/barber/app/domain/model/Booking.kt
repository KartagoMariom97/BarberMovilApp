package com.barber.app.domain.model

import java.math.BigDecimal

data class Booking(
    val id: Long,
    val clientName: String,
    val barberName: String,
    val fechaReserva: String,
    val status: String,
    val createdBy: String = "",
    val startTime: String,
    val endTime: String? = null,
    val createdAt: String? = null,
    val services: List<BookingServiceDetail> = emptyList(),
    // true cuando el cliente ya usó su única modificación permitida
    val modificationUsed: Boolean = false,
)

data class BookingServiceDetail(
    val serviceId: Long,
    val name: String,
    val minutes: Int,
    val price: BigDecimal,
)
