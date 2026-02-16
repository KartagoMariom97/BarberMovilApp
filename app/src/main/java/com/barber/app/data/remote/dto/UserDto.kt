package com.barber.app.data.remote.dto

import com.barber.app.domain.model.User
import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("nombres") val nombres: String,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String?,
    @SerializedName("dni") val dni: String?,
    @SerializedName("sexo") val sexo: String?,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String?,
    @SerializedName("role") val role: String?,
    @SerializedName("createdAt") val createdAt: String?,
) {
    fun toDomain() = User(
        id = id,
        nombres = nombres,
        fechaNacimiento = fechaNacimiento ?: "",
        dni = dni ?: "",
        genero = sexo ?: "",
        email = email,
        telefono = telefono ?: "",
        role = role ?: "",
        createdAt = createdAt ?: "",
    )
}

data class CreateUserRequest(
    @SerializedName("nombres") val nombres: String,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String,
    @SerializedName("dni") val dni: String,
    @SerializedName("genero") val genero: String,
    @SerializedName("email") val email: String,
    @SerializedName("telefono") val telefono: String,
    @SerializedName("roleId") val roleId: Long,
)
