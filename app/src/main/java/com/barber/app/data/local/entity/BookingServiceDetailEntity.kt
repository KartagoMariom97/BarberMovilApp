package com.barber.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import com.barber.app.domain.model.BookingServiceDetail
import java.math.BigDecimal

@Entity(
    tableName = "booking_service_details",
    primaryKeys = ["bookingId", "serviceId"],
    foreignKeys = [
        ForeignKey(
            entity = BookingEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookingId"],
            onDelete = CASCADE,
        )
    ],
)
data class BookingServiceDetailEntity(
    val bookingId: Long,
    val serviceId: Long,
    val name: String,
    val minutes: Int,
    val price: String,  // BigDecimal almacenado como String
)

fun BookingServiceDetailEntity.toDomain() = BookingServiceDetail(
    serviceId = serviceId,
    name = name,
    minutes = minutes,
    price = try { BigDecimal(price) } catch (_: Exception) { BigDecimal.ZERO },
)
