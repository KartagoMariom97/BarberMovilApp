package com.barber.app.data.repository

import com.barber.app.data.remote.api.NotificationApi
import com.barber.app.data.remote.dto.UpdateFcmTokenRequest
import com.barber.app.domain.repository.NotificationRepository
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val api: NotificationApi
) : NotificationRepository {

    override suspend fun updateFcmToken(token: String) {
        api.updateFcmToken(UpdateFcmTokenRequest(token))
    }
}
