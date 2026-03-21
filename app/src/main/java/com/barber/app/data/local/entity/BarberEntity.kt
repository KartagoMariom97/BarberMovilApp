package com.barber.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.barber.app.domain.model.Barber

@Entity(tableName = "barbers")
data class BarberEntity(
    @PrimaryKey val codigoBarbero: Long,
    val userId: Long?,
    val nombres: String,
    val email: String?,
    val telefono: String?,
    val active: Boolean,
    val createdAt: String?,
    // [F6] Timestamp (epoch ms) de la última sincronización con el servidor; null = pendiente de sync
    val syncedAt: Long? = null,
)

fun BarberEntity.toDomain() = Barber(
    codigoBarbero = codigoBarbero,
    userId = userId ?: -1L,
    nombres = nombres,
    email = email ?: "",
    telefono = telefono ?: "",
    active = active,
    createdAt = createdAt ?: "",
)
