package com.barber.app.data.remote.dto

import com.barber.app.domain.model.Client
import com.barber.app.domain.model.ClientProfile
import com.google.gson.annotations.SerializedName

data class ClientResponse(
    @SerializedName("codigoCliente") val codigoCliente: Long,
    @SerializedName("userId") val userId: Long?,
    @SerializedName("nombres") val nombres: String,
    @SerializedName("email") val email: String?,
    @SerializedName("createdAt") val createdAt: String?,
) {
    fun toDomain() = Client(
        codigoCliente = codigoCliente,
        userId = userId ?: -1L,
        nombres = nombres,
        email = email ?: "",
        createdAt = createdAt ?: "",
    )
}

data class ClientProfileResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("nombres") val nombres: String,
    @SerializedName("genero") val genero: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("dni") val dni: String?,
) {
    fun toDomain() = ClientProfile(
        id = id,
        nombres = nombres,
        genero = genero ?: "",
        email = email ?: "",
        telefono = telefono ?: "",
        dni = dni ?: "",
    )
}

data class CreateClientUserRequest(
    @SerializedName("nombres") val nombres: String,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String,
    @SerializedName("dni") val dni: String,
    @SerializedName("genero") val genero: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String,
)

data class UpdateClientProfileRequest(
    @SerializedName("nombres") val nombres: String,
    @SerializedName("genero") val genero: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("dni") val dni: String,
)
