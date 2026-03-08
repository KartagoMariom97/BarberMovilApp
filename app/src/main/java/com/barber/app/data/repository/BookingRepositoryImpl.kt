package com.barber.app.data.repository

import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.data.local.dao.BookingDao
import com.barber.app.data.local.entity.BookingEntity
import com.barber.app.data.remote.api.AppointmentApi
import com.barber.app.data.remote.api.ClientApi
import com.barber.app.data.remote.dto.CreateBookingRequest
import com.barber.app.domain.model.Booking
import com.barber.app.domain.repository.BookingRepository
import kotlinx.coroutines.flow.first
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
    private val bookingDao: BookingDao,
    private val userPreferencesRepository: UserPreferencesRepository // 👈 agregar
) : BookingRepository {

    override suspend fun createBooking(
        clientId: Long,
        barberId: Long,
        fechaReserva: String,
        startTime: String,
        serviceIds: List<Long>,
    ): Resource<Booking> {
        return try {
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            val response = appointmentApi.createBooking(
                CreateBookingRequest(
                    clientId = clientId,
                    barberId = barberId,
                    fechaReserva = fechaReserva,
                    startTime = startTime,
                    serviceIds = serviceIds,
                )
            ).data ?: throw Exception("Error al crear reserva")
            // Write-through: persistir en Room tras éxito en red
            val entity = BookingEntity(
                id = response.id,
                clientId = clientId, // 🔥 IMPORTANTE
                clientName = response.clientName ?: "",
                barberName = response.barberName ?: "",
                fechaReserva = response.fechaReserva ?: "",
                status = response.status ?: "",
                startTime = response.startTime ?: "",
                endTime = response.endTime,
                createdAt = response.createdAt,
            )
            bookingDao.upsertBookings(listOf(entity))
            Resource.Success(response.toDomain())
        } catch (e: HttpException) {
            val backendMsg = try {
                val errorBody = e.response()?.errorBody()?.string()
                errorBody?.let {
                    val json = com.google.gson.JsonParser.parseString(it).asJsonObject
                    json.get("message")?.asString
                }
            } catch (_: Exception) { null }

            val msg = backendMsg ?: when (e.code()) {
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
        // [FIX] Siempre va a la red: garantiza modificationUsed actualizado y todos los estados
        // Room sigue actualizándose como write-through dentro de syncBookings
        return syncBookings(clientId)
    }

    private suspend fun syncBookings(clientId: Long): Resource<List<Booking>> {
        return try {
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            val summaries = clientApi.getClientBookings(clientId).data ?: emptyList()
            val bookings = summaries.map { summary ->
                try {
                    // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
                    val detail = appointmentApi.getBookingById(summary.bookingId).data ?: throw Exception("Detalle no encontrado")
                    // Persistir detalle completo en Room
                    bookingDao.upsertBookings(listOf(detail.toEntity(clientId)))
                    bookingDao.deleteServiceDetails(detail.id)
                    bookingDao.upsertServiceDetails(detail.serviceEntities())
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
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            val response = appointmentApi.getBookingById(id).data ?: throw Exception("Reserva no encontrada")
            val clientId = userPreferencesRepository.userPreferences.first().clientId
            bookingDao.upsertBookings(listOf(response.toEntity(clientId)))
            bookingDao.deleteServiceDetails(response.id)
            bookingDao.upsertServiceDetails(response.serviceEntities())
            Resource.Success(response.toDomain())
        } catch (e: Exception) {
            Resource.Error(mapNetworkException(e, "Error al obtener la reserva"))
        }
    }

    override suspend fun cancelBooking(id: Long): Resource<Booking> {
        return try {
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            val response = appointmentApi.cancelBooking(id).data ?: throw Exception("Error al cancelar")
            // Write-through: marcar como cancelado en Room
            bookingDao.markCancelled(id)
            Resource.Success(response.toDomain())
        } catch (e: HttpException) {
            val backendMsg = try {
                val errorBody = e.response()?.errorBody()?.string()
                errorBody?.let {
                    val json = com.google.gson.JsonParser.parseString(it).asJsonObject
                    json.get("message")?.asString
                }
            } catch (_: Exception) { null }

            val msg = backendMsg ?: when (e.code()) {
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
            // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
            val response = appointmentApi.getAllBookings().data ?: emptyList()
            Resource.Success(response.map { it.toDomain() })
        } catch (e: Exception) {
            Resource.Error(mapNetworkException(e, "Error al obtener las reservas"))
        }
    }

    override suspend fun updateBooking(
        bookingId: Long,
        clientId: Long,
        barberId: Long,
        fecha: String,
        hora: String,
        serviceIds: List<Long>,
    ): Resource<Unit> {
        return try {
            appointmentApi.updateBooking(
                bookingId = bookingId,
                request = CreateBookingRequest(
                    clientId = clientId,
                    barberId = barberId,
                    fechaReserva = fecha,
                    startTime = hora,
                    serviceIds = serviceIds,
                )
            )
            // Write-through: refrescar datos actualizados en Room
            try {
                // [MEJORA] ApiResponse: extrae .data del wrapper estandarizado
                val updated = appointmentApi.getBookingById(bookingId).data ?: throw Exception("Detalle no encontrado")
                bookingDao.upsertBookings(listOf(updated.toEntity(clientId)))
                bookingDao.deleteServiceDetails(bookingId)
                bookingDao.upsertServiceDetails(updated.serviceEntities())
            } catch (_: Exception) { /* si falla el refresh, la caché se actualizará en el próximo sync */ }
            Resource.Success(Unit)
        } catch (e: HttpException) {
            val backendMsg = try {
                val errorBody = e.response()?.errorBody()?.string()
                errorBody?.let {
                    val json = com.google.gson.JsonParser.parseString(it).asJsonObject
                    json.get("message")?.asString
                }
            } catch (_: Exception) { null }

            val msg = backendMsg ?: when (e.code()) {
                400 -> "Datos inválidos para actualizar la reserva."
                404 -> "Reserva no encontrada."
                409 -> "Ya existe una reserva en ese horario."
                else -> "Error del servidor (${e.code()})"
            }
            Resource.Error(msg)
        } catch (e: Exception) {
            Resource.Error(mapNetworkException(e, "Error al actualizar la reserva"))
        }
    }
}
