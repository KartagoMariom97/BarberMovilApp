package com.barber.app.data.remote.dto

import com.barber.app.domain.model.Barber
import com.google.gson.annotations.SerializedName

data class BarberResponse(
    @SerializedName("codigoBarbero") val codigoBarbero: Long,
    @SerializedName("userId") val userId: Long?,
    @SerializedName("nombres") val nombres: String,
    @SerializedName("email") val email: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("active") val active: Boolean,
    @SerializedName("createdAt") val createdAt: String?,
) {
    fun toDomain() = Barber(
        codigoBarbero = codigoBarbero,
        userId = userId ?: -1L,
        nombres = nombres,
        email = email ?: "",
        telefono = telefono ?: "",
        active = active,
        createdAt = createdAt ?: "",
    )
}
