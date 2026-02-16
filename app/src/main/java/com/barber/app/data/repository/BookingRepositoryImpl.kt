package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.remote.api.AppointmentApi
import com.barber.app.data.remote.api.ClientApi
import com.barber.app.data.remote.dto.CreateBookingRequest
import com.barber.app.domain.model.Booking
import com.barber.app.domain.repository.BookingRepository
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

private fun mapNetworkException(e: Exception, fallback: String): String {
    return when (e) {
        is SocketTimeoutException -> "Tiempo de espera agotado. Verifica tu conexión e intenta de nuevo."
        is UnknownHostException -> "Sin conexión a internet. Verifica tu red e intenta de nuevo."
        else -> e.message ?: fallback
    }
}

class BookingRepositoryImpl @Inject constructor(
    private val appointmentApi: AppointmentApi,
    private val clientApi: ClientApi,
) : BookingRepository {

    override suspend fun createBooking(
        clientId: Long,
        barberId: Long,
        fechaReserva: String,
        startTime: String,
        serviceIds: List<Long>,
    ): Resource<Booking> {
        return try {
            val response = appointmentApi.createBooking(
                CreateBookingRequest(
                    clientId = clientId,
                    barberId = barberId,
                    fechaReserva = fechaReserva,
                    startTime = startTime,
                    serviceIds = serviceIds,
                )
            )
            Resource.Success(response.toDomain())
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                400 -> "Datos de reserva inválidos. Verifica fecha, hora y servicios."
                404 -> "Cliente o barbero no encontrado."
                409 -> "Ya existe una reserva en ese horario."
                else -> "Error del servidor (${e.code()})"
            }
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error(mapNetworkException(e, "Error al crear la reserva"))
        }
    }

    override suspend fun getClientBookings(clientId: Long): Resource<List<Booking>> {
        return try {
            val summaries = clientApi.getClientBookings(clientId)
            val bookings = summaries.map { summary ->
                try {
                    val detail = appointmentApi.getBookingById(summary.bookingId)
                    detail.toDomain()
                } catch (_: Exception) {
                    summary.toDomain()
                }
            }
            Resource.Success(bookings)
        } catch (e: Exception) {
            Resource.Error(mapNetworkException(e, "Error al obtener las reservas"))
        }
    }

    override suspend fun getBookingById(id: Long): Resource<Booking> {
        return try {
            val response = appointmentApi.getBookingById(id)
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(mapNetworkException(e, "Error al obtener la reserva"))
        }
    }

    override suspend fun cancelBooking(id: Long): Resource<Booking> {
        return try {
            val response = appointmentApi.cancelBooking(id)
            Resource.Success(response.toDomain())
        } catch (e: HttpException) {
            val msg = when (e.code()) {
                400 -> "No se puede cancelar esta reserva."
                404 -> "Reserva no encontrada."
                else -> "Error del servidor (${e.code()})"
            }
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error(mapNetworkException(e, "Error al cancelar la reserva"))
        }
    }

    override suspend fun getAllBookings(): Resource<List<Booking>> {
        return try {
            val response = appointmentApi.getAllBookings()
            Resource.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(mapNetworkException(e, "Error al obtener las reservas"))
        }
    }
}
