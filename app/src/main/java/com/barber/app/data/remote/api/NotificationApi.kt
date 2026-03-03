package com.barber.app.data.remote.api

import com.barber.app.data.remote.dto.UpdateFcmTokenRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT

interface NotificationApi {
    @PUT("notifications/fcm-token")
    suspend fun updateFcmToken(@Body request: UpdateFcmTokenRequest): Response<Unit>
}
