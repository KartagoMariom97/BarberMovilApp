package com.barber.app.domain.usecase

import com.barber.app.data.remote.api.AppointmentApi
import javax.inject.Inject

/**
 * [F11] Verifica disponibilidad del barbero antes de avanzar al step de confirmación.
 * Llama directamente al API — operación de lectura liviana que no requiere caché Room.
 * Falla abierta (fail open): si la red falla, permite avanzar; el servidor valida en el create.
 */
class CheckAvailabilityUseCase @Inject constructor(
    private val appointmentApi: AppointmentApi,
) {
    suspend operator fun invoke(
        barberId: Long,
        date: String,
        startTime: String,
        totalMinutes: Int,
    ): Boolean {
        return try {
            appointmentApi.checkAvailability(barberId, date, startTime, totalMinutes).data ?: true
        } catch (e: Exception) {
            true // fail open — la validación definitiva ocurre en el servidor al crear la reserva
        }
    }
}
