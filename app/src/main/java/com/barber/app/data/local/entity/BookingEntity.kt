package com.barber.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.barber.app.domain.model.Booking

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: Long,
    val clientName: String,
    val barberName: String,
    val fechaReserva: String,
    val status: String,
    val startTime: String,
    val endTime: String?,
    val createdAt: String?,
)

fun BookingEntity.toDomain(services: List<BookingServiceDetailEntity> = emptyList()) = Booking(
    id = id,
    clientName = clientName,
    barberName = barberName,
    fechaReserva = fechaReserva,
    status = status,
    startTime = startTime,
    endTime = endTime,
    createdAt = createdAt,
    services = services.map { it.toDomain() },
)
