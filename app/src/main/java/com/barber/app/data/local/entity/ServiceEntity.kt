package com.barber.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.barber.app.domain.model.Service
import java.math.BigDecimal

@Entity(tableName = "services")
data class ServiceEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String?,
    val estimatedMinutes: Int,
    val price: String,      // BigDecimal almacenado como String
    val createdAt: String?,
    val updatedAt: String?,
)

fun ServiceEntity.toDomain() = Service(
    id = id,
    name = name,
    description = description ?: "",
    estimatedMinutes = estimatedMinutes,
    price = try { BigDecimal(price) } catch (_: Exception) { BigDecimal.ZERO },
)
