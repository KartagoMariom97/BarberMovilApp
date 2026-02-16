package com.barber.app.domain.model

import java.math.BigDecimal

data class Booking(
    val id: Long,
    val clientName: String,
    val barberName: String,
    val fechaReserva: String,
    val status: String,
    val startTime: String,
    val endTime: String? = null,
    val createdAt: String? = null,
    val services: List<BookingServiceDetail> = emptyList(),
)

data class BookingServiceDetail(
    val serviceId: Long,
    val name: String,
    val minutes: Int,
    val price: BigDecimal,
)
