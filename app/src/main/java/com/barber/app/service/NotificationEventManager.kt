package com.barber.app.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

// Singleton que transporta eventos de confirmación de reservas hacia MainActivity
// sin acoplamiento entre BarberFirebaseMessagingService/HomeViewModel y la UI raíz
@Singleton
class NotificationEventManager @Inject constructor() {

    // Cantidad de reservas confirmadas pendientes de notificar; 0 = sin evento activo
    private val _confirmedCount = MutableStateFlow(0)
    val confirmedCount: StateFlow<Int> = _confirmedCount.asStateFlow()

    // Emite evento con el número de reservas confirmadas (default 1 para eventos FCM individuales)
    fun onBookingConfirmed(count: Int = 1) {
        _confirmedCount.value = count
    }

    // Consume el evento tras ser aceptado por el usuario (dismiss del dialog global)
    fun clearConfirmedEvent() {
        _confirmedCount.value = 0
    }
}
