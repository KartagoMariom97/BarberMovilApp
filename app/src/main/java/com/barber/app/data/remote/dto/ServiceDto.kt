package com.barber.app.data.remote.dto

import com.barber.app.domain.model.Service
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class ServiceResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String?,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int,
    @SerializedName("price") val price: BigDecimal,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("updatedAt") val updatedAt: String?,
) {
    fun toDomain() = Service(
        id = id,
        name = name,
        description = description ?: "",
        estimatedMinutes = estimatedMinutes,
        price = price,
    )
}
