package com.barber.app.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * [F5] DTO genérico de paginación — espeja la estructura que retorna el backend PagedResponse<T>.
 * Campos:
 *   content       → lista de elementos de la página actual
 *   page          → índice de página actual (0-based)
 *   size          → elementos por página solicitados
 *   totalElements → total de registros en el servidor
 *   totalPages    → total de páginas disponibles
 *   last          → true si es la última página (nextKey = null en PagingSource)
 */
data class PagedResponse<T>(
    @SerializedName("content")       val content: List<T>,
    @SerializedName("page")          val page: Int,
    @SerializedName("size")          val size: Int,
    @SerializedName("totalElements") val totalElements: Long,
    @SerializedName("totalPages")    val totalPages: Int,
    @SerializedName("last")          val last: Boolean,
)
