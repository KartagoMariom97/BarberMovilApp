package com.barber.app.data.remote.dto

import com.barber.app.data.local.entity.ServiceEntity
import com.barber.app.domain.model.Service
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ServiceResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int,
    @SerializedName("price") val price: BigDecimal,
    @SerializedName("active") val active: Boolean = true, // soft delete field
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
) {
    fun toDomain() = Service(
        id = id,
        name = name,
        description = description ?: "",
        estimatedMinutes = estimatedMinutes,
        price = price,
        active = active,
    )

    fun toEntity() = ServiceEntity(
        id = id,
        name = name,
        description = description,
        estimatedMinutes = estimatedMinutes,
        price = price.toPlainString(),
        active = active,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}
