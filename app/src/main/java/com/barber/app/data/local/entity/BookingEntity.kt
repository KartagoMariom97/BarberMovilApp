package com.barber.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.barber.app.domain.model.Booking

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: Long,
    val clientId: Long,
    val clientName: String,
    val barberName: String,
    val fechaReserva: String,
    val status: String,
    val startTime: String,
    val endTime: String?,
    val createdAt: String?,
    // true cuando el cliente ya usó su única modificación permitida
    val modificationUsed: Boolean = false,
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
    modificationUsed = modificationUsed,
    services = services.map { it.toDomain() },
)
