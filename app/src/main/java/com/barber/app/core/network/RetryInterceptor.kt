package com.barber.app.core.network

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * [F2] Interceptor de reintentos con exponential backoff.
 * - Reintenta hasta 3 veces en IOException (sin red) o 503 (servidor no disponible).
 * - No reintenta en 4xx (errores del cliente) ni en 2xx/3xx.
 * - Backoff: 500ms → 1000ms → 2000ms entre intentos.
 */
class RetryInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        var attempt = 0
        var lastException: IOException? = null

        while (attempt < MAX_RETRIES) {
            try {
                val response = chain.proceed(request)
                // Solo reintenta en 503 (servicio no disponible); el resto se devuelve directo
                if (response.code != 503) return response
                response.close()
            } catch (e: IOException) {
                lastException = e
            }

            attempt++
            if (attempt < MAX_RETRIES) {
                // Exponential backoff: 500ms, 1000ms, 2000ms
                Thread.sleep(INITIAL_DELAY_MS * (1L shl attempt))
            }
        }

        throw lastException ?: IOException("Máximo de reintentos alcanzado ($MAX_RETRIES intentos)")
    }

    companion object {
        private const val MAX_RETRIES = 3
        private const val INITIAL_DELAY_MS = 500L
    }
}
