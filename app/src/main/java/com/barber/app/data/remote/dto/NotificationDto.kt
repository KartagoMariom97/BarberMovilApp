package com.barber.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UpdateFcmTokenRequest(
    @SerializedName("fcmToken") val fcmToken: String
)

data class BookingNotification(
    @SerializedName("bookingId") val bookingId: Long,
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("timestamp") val timestamp: String
)
