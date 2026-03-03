package com.barber.app.domain.repository

interface NotificationRepository {
    suspend fun updateFcmToken(token: String)
}
