package com.barber.app.data.remote.dto

import com.barber.app.domain.model.AdminBarber
import com.barber.app.domain.model.AdminBooking
import com.barber.app.domain.model.AdminClient
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

// ─── Admin Barber ─────────────────────────────────────────────────────────────

data class AdminBarberResponse(
    @SerializedName("codigoBarbero") val codigoBarbero: Long,
    @SerializedName("userId")        val userId: Long?,
    @SerializedName("nombres")       val nombres: String,
    @SerializedName("dni")           val dni: String?,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String?,
    @SerializedName("genero")        val genero: String?,
    @SerializedName("email")         val email: String?,
    @SerializedName("telefono")      val telefono: String?,
    @SerializedName("active")        val active: Boolean,
    @SerializedName("createdAt")     val createdAt: String?,
    @SerializedName("updatedAt")     val updatedAt: String?,
) {
    fun toDomain() = AdminBarber(
        codigoBarbero  = codigoBarbero,
        userId         = userId ?: -1L,
        nombres        = nombres,
        dni            = dni ?: "",
        fechaNacimiento = fechaNacimiento ?: "",
        genero         = genero ?: "",
        email          = email ?: "",
        telefono       = telefono ?: "",
        active         = active,
        createdAt      = createdAt ?: "",
        updatedAt      = updatedAt ?: "",
    )
}

data class AdminUpdateBarberRequest(
    @SerializedName("nombres")  val nombres: String? = null,
    @SerializedName("email")    val email: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
    @SerializedName("active")   val active: Boolean? = null,
)

// ─── Admin Client Create ──────────────────────────────────────────────────────

/** Request para POST /api/v1/admin/clients — crea cliente con cuenta de usuario */
data class AdminCreateClientRequest(
    @SerializedName("nombres")          val nombres: String,
    @SerializedName("fechaNacimiento")  val fechaNacimiento: String,   // yyyy-MM-dd
    @SerializedName("dni")              val dni: String,
    @SerializedName("genero")           val genero: String,
    @SerializedName("email")            val email: String? = null,
    @SerializedName("telefono")         val telefono: String,
    @SerializedName("password")         val password: String? = null,
)

// ─── Admin Client ─────────────────────────────────────────────────────────────

data class AdminClientResponse(
    @SerializedName("codigoCliente")   val codigoCliente: Long,
    @SerializedName("userId")          val userId: Long?,
    @SerializedName("nombres")         val nombres: String,
    @SerializedName("dni")             val dni: String?,
    @SerializedName("fechaNacimiento") val fechaNacimiento: String?,
    @SerializedName("genero")          val genero: String?,
    @SerializedName("email")           val email: String?,
    @SerializedName("telefono")        val telefono: String?,
    @SerializedName("createdAt")       val createdAt: String?,
    @SerializedName("updatedAt")       val updatedAt: String?,
) {
    fun toDomain() = AdminClient(
        codigoCliente   = codigoCliente,
        userId          = userId ?: -1L,
        nombres         = nombres,
        dni             = dni ?: "",
        fechaNacimiento = fechaNacimiento ?: "",
        genero          = genero ?: "",
        email           = email ?: "",
        telefono        = telefono ?: "",
        createdAt       = createdAt ?: "",
    )
}

data class AdminUpdateClientRequest(
    @SerializedName("nombres")  val nombres: String? = null,
    @SerializedName("email")    val email: String? = null,
    @SerializedName("telefono") val telefono: String? = null,
)

// ─── Admin Service (reutiliza ServiceResponse existente) ─────────────────────

data class AdminCreateServiceRequest(
    @SerializedName("name")             val name: String,
    @SerializedName("description")      val description: String?,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int,
    @SerializedName("price")            val price: BigDecimal,
)

data class AdminUpdateServiceRequest(
    @SerializedName("name")             val name: String? = null,
    @SerializedName("description")      val description: String? = null,
    @SerializedName("estimatedMinutes") val estimatedMinutes: Int? = null,
    @SerializedName("price")            val price: BigDecimal? = null,
)

// ─── Admin Barber Create ──────────────────────────────────────────────────────

/** Request para POST /api/v1/barbers/user — crea barbero con cuenta de usuario */
data class AdminCreateBarberRequest(
    @SerializedName("nombres")          val nombres: String,
    @SerializedName("fechaNacimiento")  val fechaNacimiento: String,    // yyyy-MM-dd
    @SerializedName("dni")              val dni: String,
    @SerializedName("genero")           val genero: String,
    @SerializedName("email")            val email: String,
    @SerializedName("password")         val password: String,
    @SerializedName("telefono")         val telefono: String? = null,
    @SerializedName("active")           val active: Boolean = true,
)

// ─── Admin Booking ────────────────────────────────────────────────────────────

data class AdminBookingResponse(
    @SerializedName("id")           val id: Long,
    @SerializedName("clientId")     val clientId: Long,
    @SerializedName("clientName")   val clientName: String?,
    @SerializedName("barberId")     val barberId: Long,
    @SerializedName("barberName")   val barberName: String?,
    @SerializedName("fechaReserva") val fechaReserva: String?,
    @SerializedName("status")       val status: String?,
    @SerializedName("startTime")    val startTime: String?,
    @SerializedName("endTime")      val endTime: String?,
    @SerializedName("totalMinutes") val totalMinutes: Int?,
    @SerializedName("createdAt")    val createdAt: String?,
    @SerializedName("services")     val services: List<BookingServiceDetailResponse>?,
) {
    fun toDomain() = AdminBooking(
        id           = id,
        clientId     = clientId,
        clientName   = clientName ?: "",
        barberId     = barberId,
        barberName   = barberName ?: "",
        fechaReserva = fechaReserva ?: "",
        status       = status ?: "",
        startTime    = startTime ?: "",
        endTime      = endTime,
        totalMinutes = totalMinutes ?: 0,
        createdAt    = createdAt ?: "",
        services     = services?.map { it.toDomain() } ?: emptyList(),
    )
}

data class AdminChangeStatusRequest(
    @SerializedName("status") val status: String,
)
