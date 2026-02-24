package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.data.remote.api.AdminBookingApi
import com.barber.app.data.remote.dto.AdminChangeStatusRequest
import com.barber.app.data.remote.dto.AdminUpdateBookingRequest
import com.barber.app.data.remote.dto.CreateBookingRequest
import com.barber.app.domain.model.AdminBooking
import com.barber.app.domain.repository.AdminBookingRepository
import retrofit2.HttpException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

private fun mapError(e: Exception, fallback: String): String = when (e) {
    is SocketTimeoutException -> "Tiempo de espera agotado."
    is UnknownHostException   -> "Sin conexión a internet."
    is HttpException          -> {
        val body = try { e.response()?.errorBody()?.string() } catch (_: Exception) { null }
        val msg  = try { com.google.gson.JsonParser.parseString(body).asJsonObject.get("message")?.asString } catch (_: Exception) { null }
        msg ?: "Error del servidor (${e.code()})"
    }
    else -> e.message ?: fallback
}

class AdminBookingRepositoryImpl @Inject constructor(
    private val api: AdminBookingApi,
) : AdminBookingRepository {

    override suspend fun getAllBookings(
        status: String?,
        barberId: Long?,
        clientId: Long?,
    ): Resource<List<AdminBooking>> {
        return try {
            Resource.Success(api.getAllBookings(status, barberId, clientId).map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al obtener las reservas"))
        }
    }

    override suspend fun getBookingById(id: Long): Resource<AdminBooking> {
        return try {
            Resource.Success(api.getBookingById(id).toDomain())
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al obtener la reserva"))
        }
    }

    override suspend fun changeStatus(id: Long, status: String): Resource<AdminBooking> {
        return try {
            val response = api.changeStatus(id, AdminChangeStatusRequest(status))
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al cambiar el estado"))
        }
    }

    /** Crea reserva llamando POST /bookings; retorna Unit porque el ViewModel recarga la lista */
    override suspend fun createBooking(
        clientId: Long,
        barberId: Long,
        fechaReserva: String,
        startTime: String,
        serviceIds: List<Long>,
    ): Resource<Unit> {
        return try {
            api.createBooking(
                CreateBookingRequest(
                    clientId = clientId,
                    barberId = barberId,
                    fechaReserva = fechaReserva,
                    startTime = startTime,
                    serviceIds = serviceIds,
                ),
            )
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al crear la reserva"))
        }
    }

    override suspend fun updateBooking(
        id: Long,
        barberId: Long,
        fechaReserva: String,
        startTime: String,
        serviceIds: List<Long>,
    ): Resource<AdminBooking> {
        return try {
            val response = api.updateBooking(
                id,
                AdminUpdateBookingRequest(
                    barberId = barberId,
                    fechaReserva = fechaReserva,
                    startTime = startTime,
                    serviceIds = serviceIds,
                ),
            )
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(mapError(e, "Error al editar la reserva"))
        }
    }
}
