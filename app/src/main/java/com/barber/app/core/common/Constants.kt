package com.barber.app.core.common

import com.barber.app.BuildConfig

object Constants {
    const val BASE_URL = BuildConfig.BASE_URL
    const val DATASTORE_NAME = "barber_preferences"
    const val NOTIFICATION_CHANNEL_ID = "appointment_reminders"
    const val NOTIFICATION_CHANNEL_NAME = "Recordatorios de Citas"

    val WS_URL: String = BASE_URL
        .replace("https://", "wss://")
        .replace("http://", "ws://")
        .substringBefore("/api/v1/") + "/ws"
}
