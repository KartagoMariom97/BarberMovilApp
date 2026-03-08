package com.barber.app.data.remote.dto

/**
 * Wrapper estandarizado para todas las respuestas exitosas del backend.
 * Estructura: { status, message, data }
 * El campo [data] es nullable para cubrir respuestas sin cuerpo (ej: Void endpoints).
 */
data class ApiResponse<T>(
    val status: Int,
    val message: String,
    val data: T?,
)
