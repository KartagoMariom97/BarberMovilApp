package com.barber.app.data.remote.dto

import com.barber.app.domain.model.Booking
import com.barber.app.domain.model.BookingServiceDetail
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class BookingResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("clientName") val clientName: String?,
    @SerializedName("barberName") val barberName: String?,
    @SerializedName("fechaReserva") val fechaReserva: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("createdAt") val createdAt: String?,
) {
    fun toDomain() = Booking(
        id = id,
        clientName = clientName ?: "",
        barberName = barberName ?: "",
        fechaReserva = fechaReserva ?: "",
        status = status ?: "",
        startTime = startTime ?: "",
        endTime = endTime,
        createdAt = createdAt,
    )
}

data class BookingWithServicesResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("clientName") val clientName: String?,
    @SerializedName("barberName") val barberName: String?,
    @SerializedName("fechaReserva") val fechaReserva: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("createdAt") val createdAt: String?,
    @SerializedName("services") val services: List<BookingServiceDetailResponse>?,
) {
    fun toDomain() = Booking(
        id = id,
        clientName = clientName ?: "",
        barberName = barberName ?: "",
        fechaReserva = fechaReserva ?: "",
        status = status ?: "",
        startTime = startTime ?: "",
        createdAt = createdAt,
        services = services?.map { it.toDomain() } ?: emptyList(),
    )
}

data class BookingDetailResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("clientName") val clientName: String?,
    @SerializedName("barberName") val barberName: String?,
    @SerializedName("fechaReserva") val fechaReserva: String?,
    @SerializedName("status") val status: String?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("endTime") val endTime: String?,
    @SerializedName("services") val services: List<BookingServiceDetailResponse>?,
) {
    fun toDomain() = Booking(
        id = id,
        clientName = clientName ?: "",
        barberName = barberName ?: "",
        fechaReserva = fechaReserva ?: "",
        status = status ?: "",
        startTime = startTime ?: "",
        endTime = endTime,
        services = services?.map { it.toDomain() } ?: emptyList(),
    )
}

data class BookingServiceDetailResponse(
    @SerializedName("serviceId") val serviceId: Long,
    @SerializedName("name") val name: String,
    @SerializedName("minutes") val minutes: Int,
    @SerializedName("price") val price: BigDecimal,
) {
    fun toDomain() = BookingServiceDetail(
        serviceId = serviceId,
        name = name,
        minutes = minutes,
        price = price,
    )
}

data class CreateBookingRequest(
    @SerializedName("clientId") val clientId: Long,
    @SerializedName("barberId") val barberId: Long,
    @SerializedName("fechaReserva") val fechaReserva: String,
    @SerializedName("startTime") val startTime: String,
    @SerializedName("serviceIds") val serviceIds: List<Long>,
)

data class ClientBookingSummaryResponse(
    @SerializedName("bookingId") val bookingId: Long,
    @SerializedName("fechaReserva") val fechaReserva: String?,
    @SerializedName("startTime") val startTime: String?,
    @SerializedName("totalMinutes") val totalMinutes: Int?,
    @SerializedName("status") val status: String?,
    @SerializedName("barberName") val barberName: String?,
) {
    fun toDomain() = Booking(
        id = bookingId,
        clientName = "",
        barberName = barberName ?: "",
        fechaReserva = fechaReserva ?: "",
        status = status ?: "",
        startTime = startTime ?: "",
    )
}
