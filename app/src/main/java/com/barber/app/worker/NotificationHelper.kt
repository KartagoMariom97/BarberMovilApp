package com.barber.app.worker

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.barber.app.MainActivity
import com.barber.app.R
import com.barber.app.core.common.Constants

object NotificationHelper {

    fun showAppointmentReminder(
        context: Context,
        bookingId: Long,
        barberName: String,
        time: String,
        date: String,
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, bookingId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Recordatorio de cita")
            .setContentText("Tienes una cita con $barberName el $date a las $time")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("Tienes una cita programada con $barberName el $date a las $time. No olvides llegar a tiempo.")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(bookingId.toInt(), notification)
        } catch (_: SecurityException) {
            // Permission not granted
        }
    }
}
