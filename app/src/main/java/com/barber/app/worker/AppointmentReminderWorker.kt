package com.barber.app.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
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
        val barberName = inputData.getString(KEY_BARBER_NAME) ?: return Result.failure()
        val time = inputData.getString(KEY_TIME) ?: return Result.failure()
        val date = inputData.getString(KEY_DATE) ?: return Result.failure()

        if (bookingId == -1L) return Result.failure()

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
        const val KEY_BOOKING_ID = "booking_id"
        const val KEY_BARBER_NAME = "barber_name"
        const val KEY_TIME = "time"
        const val KEY_DATE = "date"

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
    }
}
