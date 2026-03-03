package com.barber.app.service

import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.repository.NotificationRepository
import com.barber.app.worker.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
        val title     = remoteMessage.notification?.title ?: "Actualización de Reserva"
        val body      = remoteMessage.notification?.body ?: ""
        val bookingId = remoteMessage.data["bookingId"]?.toLongOrNull() ?: 0L
        val status    = remoteMessage.data["status"] ?: ""
        val type      = remoteMessage.data["type"] ?: ""

        // Siempre mostrar la notificación del sistema (funciona en background y foreground)
        NotificationHelper.showBookingStatusUpdate(applicationContext, bookingId, title, body)

        // Si la reserva fue confirmada, señalizar para mostrar AlertDialog en la app
        if (status == "CONFIRMED" && type == "STATUS_CHANGED") {
            notificationEventManager.onBookingConfirmed(bookingId)
        }
    }

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = userPreferencesRepository.userPreferences.firstOrNull()
            if (prefs?.isLoggedIn == true && prefs.token.isNotEmpty()) {
                runCatching { notificationRepository.updateFcmToken(token) }
            }
        }
    }
}
