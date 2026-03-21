package com.barber.app.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.barber.app.data.remote.api.AdminBookingApi
import com.barber.app.domain.model.AdminBooking

/**
 * [F5] PagingSource que carga páginas de reservas admin desde el backend.
 *
 * Decisión de diseño:
 *   - Cuando statusFilter == "PENDING", se envía status=null al backend para que devuelva
 *     también MODIFIED_PENDING. El filtro local dentro de load() acota ambos estados.
 *   - Esto evita un cambio en el contrato API del backend para soportar múltiples estados.
 *
 * Thread safety: PagingSource es creado una vez por filtro; Pager lo invalida y recrea
 * automáticamente al llamar a PagingDataAdapter.refresh() o lazyPagingItems.refresh().
 */
class AdminBookingsPagingSource(
    private val api: AdminBookingApi,
    private val statusFilter: String?,
) : PagingSource<Int, AdminBooking>() {

    // Cuando el filtro UI es PENDING, mandamos null al backend para incluir MODIFIED_PENDING
    private val networkStatus = if (statusFilter == "PENDING") null else statusFilter

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, AdminBooking> {
        val page = params.key ?: 0
        return try {
            val response = api.getAllBookings(
                status = networkStatus,
                page   = page,
                size   = params.loadSize,
            )
            val pagedData = response.data
                ?: return LoadResult.Error(Exception("Respuesta vacía del servidor"))

            // Filtrado local para el caso PENDING: incluye MODIFIED_PENDING
            val items = pagedData.content
                .map { it.toDomain() }
                .let { list ->
                    if (statusFilter == "PENDING") {
                        list.filter { b ->
                            val s = b.status.uppercase()
                            s == "PENDING" || s == "MODIFIED_PENDING"
                        }
                    } else list
                }

            LoadResult.Page(
                data    = items,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (pagedData.last) null else page + 1,
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, AdminBooking>): Int? {
        return state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }
    }
}
