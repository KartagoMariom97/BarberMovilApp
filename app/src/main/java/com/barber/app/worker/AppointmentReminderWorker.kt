package com.barber.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@HiltWorker
class AppointmentReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val bookingId = inputData.getLong(KEY_BOOKING_ID, -1L)
        if (bookingId == -1L) return Result.failure()

        // Ruta recordatorio 5 min (disparado por FCM DATA SCHEDULE_REMINDER):
        // usa título y cuerpo pre-construidos en el backend
        val reminderTitle = inputData.getString(KEY_REMINDER_TITLE)
        val reminderBody  = inputData.getString(KEY_REMINDER_BODY)
        if (reminderTitle != null && reminderBody != null) {
            NotificationHelper.showBookingStatusUpdate(applicationContext, bookingId, reminderTitle, reminderBody)
            return Result.success()
        }

        // Ruta recordatorio 1 hora (scheduling local al crear/ver la reserva)
        val barberName = inputData.getString(KEY_BARBER_NAME) ?: return Result.failure()
        val time = inputData.getString(KEY_TIME) ?: return Result.failure()
        val date = inputData.getString(KEY_DATE) ?: return Result.failure()

        NotificationHelper.showAppointmentReminder(
            context = applicationContext,
            bookingId = bookingId,
            barberName = barberName,
            time = time,
            date = date,
        )

        return Result.success()
    }

    companion object {
        const val KEY_BOOKING_ID    = "booking_id"
        const val KEY_BARBER_NAME   = "barber_name"
        const val KEY_TIME          = "time"
        const val KEY_DATE          = "date"
        // Claves para el recordatorio de 5 min (disparado por FCM DATA)
        const val KEY_REMINDER_TITLE = "reminder_title"
        const val KEY_REMINDER_BODY  = "reminder_body"

        fun schedule(
            context: Context,
            bookingId: Long,
            barberName: String,
            date: String,
            time: String,
        ) {
            try {
                val appointmentDateTime = LocalDateTime.parse(
                    "${date}T${time}",
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                )
                // Remind 1 hour before
                val reminderTime = appointmentDateTime.minusHours(1)
                val now = LocalDateTime.now()
                val delay = Duration.between(now, reminderTime)

                if (delay.isNegative) return

                val data = Data.Builder()
                    .putLong(KEY_BOOKING_ID, bookingId)
                    .putString(KEY_BARBER_NAME, barberName)
                    .putString(KEY_TIME, time)
                    .putString(KEY_DATE, date)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
                    .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("reminder_$bookingId")
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)
            } catch (_: Exception) {
                // Invalid date/time format
            }
        }

        /**
         * Programa un recordatorio 5 minutos antes de la cita.
         * Disparado cuando el cliente/admin recibe un FCM DATA SCHEDULE_REMINDER.
         * Usa enqueueUniqueWork con REPLACE para actualizar si la hora cambió.
         * bookingTime: formato ISO "yyyy-MM-dd'T'HH:mm:ss" enviado por el backend.
         */
        fun scheduleFiveMin(
            context: Context,
            bookingId: Long,
            bookingTime: String,
            reminderTitle: String,
            reminderBody: String,
        ) {
            try {
                // Parseo tolerante: intenta ISO completo; si falla, intenta sin segundos
                val appointmentDateTime = try {
                    LocalDateTime.parse(bookingTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                } catch (_: Exception) {
                    LocalDateTime.parse(bookingTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
                }

                val reminderTime = appointmentDateTime.minusMinutes(5)
                val now  = LocalDateTime.now()
                val delay = Duration.between(now, reminderTime)

                if (delay.isNegative) return

                val data = Data.Builder()
                    .putLong(KEY_BOOKING_ID,    bookingId)
                    .putString(KEY_REMINDER_TITLE, reminderTitle)
                    .putString(KEY_REMINDER_BODY,  reminderBody)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<AppointmentReminderWorker>()
                    .setInitialDelay(delay.toMillis(), TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("reminder_5min_$bookingId")
                    .build()

                // REPLACE: si la hora de la cita cambió (MODIFIED_PENDING → CONFIRMED)
                // el nuevo job reemplaza al anterior con el tiempo correcto
                WorkManager.getInstance(context)
                    .enqueueUniqueWork("reminder_5min_$bookingId", ExistingWorkPolicy.REPLACE, workRequest)
            } catch (_: Exception) {
                // Formato de fecha inválido o error inesperado
            }
        }

        /** Cancela el recordatorio de 5 min para una reserva específica. */
        fun cancelFiveMin(context: Context, bookingId: Long) {
            WorkManager.getInstance(context).cancelUniqueWork("reminder_5min_$bookingId")
        }
    }
}
