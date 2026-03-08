package com.barber.app.service

import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.repository.NotificationRepository
import com.barber.app.worker.AppointmentReminderWorker
import com.barber.app.worker.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BarberFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    // Singleton para propagar eventos de confirmación hacia MainActivity sin acoplamiento
    @Inject
    lateinit var notificationEventManager: NotificationEventManager

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val type      = remoteMessage.data["type"]    ?: ""
        val bookingId = remoteMessage.data["bookingId"]?.toLongOrNull() ?: 0L

        // ── Mensajes DATA puros (sin notification payload) ───────────────────────
        // Siempre entregados aquí sin importar foreground/background/killed.

        // Recordatorio 5 min: programa WorkManager para disparar la notificación local
        if (type == "SCHEDULE_REMINDER") {
            val bookingTime   = remoteMessage.data["bookingTime"]   ?: return
            val reminderTitle = remoteMessage.data["reminderTitle"] ?: return
            val reminderBody  = remoteMessage.data["reminderBody"]  ?: return
            AppointmentReminderWorker.scheduleFiveMin(
                applicationContext, bookingId, bookingTime, reminderTitle, reminderBody,
            )
            return // no mostrar notificación inmediata; el Worker la disparará en 5 min
        }

        // Cancelar recordatorio: reserva cancelada antes de que dispare el Worker
        if (type == "CANCEL_REMINDER") {
            AppointmentReminderWorker.cancelFiveMin(applicationContext, bookingId)
            return
        }

        // ── Notificaciones visibles (existentes) ─────────────────────────────────
        // [FIX] Fallback a campos data[] cuando notification payload no está disponible.
        // Con notification+data, onMessageReceived solo se llama en foreground.
        // En background/killed, FCM muestra la notificación automáticamente usando
        // el canal 'appointment_reminders' (configurado en AndroidManifest y AndroidConfig del backend).
        val title  = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "Actualización de Reserva"
        val body   = remoteMessage.notification?.body  ?: remoteMessage.data["body"]  ?: ""
        val status = remoteMessage.data["status"] ?: ""

        // Siempre mostrar la notificación del sistema (foreground únicamente vía este path;
        // background/killed es manejado automáticamente por FCM SDK)
        NotificationHelper.showBookingStatusUpdate(applicationContext, bookingId, title, body)

        // Si la reserva fue confirmada, emitir evento al dialog global (count=1: una confirmación)
        if (status == "CONFIRMED" && type == "STATUS_CHANGED") {
            notificationEventManager.onBookingConfirmed(count = 1)
        }

        // Al recibir cancelación del admin, limpiar el recordatorio local
        // (seguro ante posibles fallas de entrega del CANCEL_REMINDER)
        if (type == "ADMIN_CANCELLED") {
            AppointmentReminderWorker.cancelFiveMin(applicationContext, bookingId)
        }
    }

    override fun onNewToken(token: String) {
        // [MEJORA] SupervisorJob: excepciones en este scope no colapsan el servicio FCM.
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            val prefs = userPreferencesRepository.userPreferences.firstOrNull()
            if (prefs?.isLoggedIn == true && prefs.token.isNotEmpty()) {
                runCatching { notificationRepository.updateFcmToken(token) }
            }
        }
    }
}
