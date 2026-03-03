package com.barber.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Contenedor singleton que transporta eventos de notificación FCM hacia MainActivity
// sin acoplamiento directo entre BarberFirebaseMessagingService y la UI
@Singleton
class NotificationEventManager @Inject constructor() {

    // bookingId de la reserva confirmada; null cuando no hay evento pendiente
    private val _confirmedBookingId = MutableStateFlow<Long?>(null)
    val confirmedBookingId: StateFlow<Long?> = _confirmedBookingId.asStateFlow()

    fun onBookingConfirmed(bookingId: Long) {
        _confirmedBookingId.value = bookingId
    }

    fun clearConfirmedEvent() {
        _confirmedBookingId.value = null
    }
}
