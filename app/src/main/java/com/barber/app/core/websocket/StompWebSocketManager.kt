package com.barber.app.core.websocket

import com.barber.app.data.remote.dto.BookingNotification
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StompWebSocketManager @Inject constructor(
    private val okHttpClient: OkHttpClient
) {
    private var webSocket: WebSocket? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val gson = Gson()

    private val _notifications = MutableSharedFlow<BookingNotification>(
        extraBufferCapacity = 10,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val notifications: SharedFlow<BookingNotification> = _notifications.asSharedFlow()

    fun connect(wsUrl: String, token: String) {
        disconnect()
        val request = Request.Builder().url(wsUrl).build()
        webSocket = okHttpClient.newWebSocket(request, StompListener(token))
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnecting")
        webSocket = null
    }

    private inner class StompListener(private val token: String) : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            webSocket.send(frame("CONNECT", mapOf(
                "accept-version" to "1.1,1.2",
                "Authorization" to "Bearer $token"
            )))
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            val command = text.substringBefore("\n").trim()
            when (command) {
                "CONNECTED" -> {
                    webSocket.send(frame("SUBSCRIBE", mapOf(
                        "id" to "sub-0",
                        "destination" to "/user/queue/booking-notifications"
                    )))
                }
                "MESSAGE" -> {
                    val body = text.substringAfterLast("\n\n").trimEnd('\u0000')
                    runCatching {
                        val notification = gson.fromJson(body, BookingNotification::class.java)
                        scope.launch { _notifications.emit(notification) }
                    }
                }
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            // FCM maneja las notificaciones cuando WebSocket no está disponible
        }
    }

    private fun frame(command: String, headers: Map<String, String>, body: String = ""): String {
        return buildString {
            append(command).append('\n')
            headers.forEach { (k, v) -> append("$k:$v\n") }
            append('\n').append(body).append('\u0000')
        }
    }
}
