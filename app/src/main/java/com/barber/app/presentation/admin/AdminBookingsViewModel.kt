package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminBarber
import com.barber.app.domain.model.AdminBooking
import com.barber.app.domain.model.AdminClient
import com.barber.app.domain.model.Service
import com.barber.app.domain.repository.AdminBarberRepository
import com.barber.app.domain.repository.AdminBookingRepository
import com.barber.app.domain.repository.AdminClientRepository
import com.barber.app.domain.repository.ServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBookingsState(
    // [F5] bookings e isRefreshing eliminados — ahora vienen de pagedBookings / lazyPagingItems.loadState
    val statusFilter: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    /** Controla visibilidad del diálogo de creación */
    val showCreateDialog: Boolean = false,
    /** Controla visibilidad del diálogo de edición */
    val showEditDialog: Boolean = false,
    val editingBooking: AdminBooking? = null,
    /** Listas para los selectores del diálogo de creación/edición */
    val clients: List<AdminClient> = emptyList(),
    val barbers: List<AdminBarber> = emptyList(),
    val services: List<Service> = emptyList(),
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class AdminBookingsViewModel @Inject constructor(
    private val repository: AdminBookingRepository,
    /** Repos adicionales para cargar selectores del diálogo de creación */
    private val clientRepo: AdminClientRepository,
    private val barberRepo: AdminBarberRepository,
    private val serviceRepo: ServiceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminBookingsState())
    val state: StateFlow<AdminBookingsState> = _state.asStateFlow()

    // [F5] Fuente de verdad del filtro activo — flatMapLatest recrea el PagingSource al cambiar
    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    // [F5] Flow paginado reactivo al filtro; cachedIn sobrevive recomposiciones y rotación de pantalla
    val pagedBookings: Flow<PagingData<AdminBooking>> = _statusFilter
        .flatMapLatest { status -> repository.getPagedBookings(status) }
        .cachedIn(viewModelScope)

    fun changeStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            when (val result = repository.changeStatus(id, newStatus)) {
                // [F5] Ya no actualizamos state.bookings — la pantalla llama lazyPagingItems.refresh()
                // al detectar successMessage para recargar desde el servidor
                is Resource.Success -> _state.update { it.copy(successMessage = "Estado actualizado a $newStatus") }
                is Resource.Error   -> _state.update { it.copy(error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    // [F5] Actualiza _statusFilter → flatMapLatest recrea el PagingSource automáticamente
    fun setFilter(status: String?) {
        _statusFilter.value = status
        _state.update { it.copy(statusFilter = status) }
    }

    /** Abre el diálogo y carga clientes, barberos y servicios (solo si la lista está vacía) */
    fun showCreateDialog() {
        _state.update { it.copy(showCreateDialog = true) }
        viewModelScope.launch {
            // Carga cada selector en paralelo; guarda resultado en una sola llamada a update
            val cDeferred = async { if (_state.value.clients.isEmpty()) clientRepo.getAllClients() else null }
            val bDeferred = async { if (_state.value.barbers.isEmpty()) barberRepo.getAllBarbers() else null }
            val sDeferred = async { if (_state.value.services.isEmpty()) serviceRepo.getAllServices() else null }
            val cRes = cDeferred.await()
            val bRes = bDeferred.await()
            val sRes = sDeferred.await()
            _state.update { s ->
                s.copy(
                    clients  = if (cRes is Resource.Success) cRes.data else s.clients,
                    barbers  = if (bRes is Resource.Success) bRes.data else s.barbers,
                    services = if (sRes is Resource.Success) sRes.data else s.services,
                )
            }
        }
    }

    /** Cierra el diálogo de creación */
    fun dismissCreateDialog() { _state.update { it.copy(showCreateDialog = false) } }

    /** Abre el diálogo de edición para una reserva (solo PENDING/CONFIRMED) */
    fun showEditDialog(booking: AdminBooking) {
        _state.update { it.copy(showEditDialog = true, editingBooking = booking) }
        // Carga barberos y servicios si no están cargados
        viewModelScope.launch {
            val bDeferred = async { if (_state.value.barbers.isEmpty()) barberRepo.getAllBarbers() else null }
            val sDeferred = async { if (_state.value.services.isEmpty()) serviceRepo.getAllServices() else null }
            val bRes = bDeferred.await()
            val sRes = sDeferred.await()
            _state.update { s ->
                s.copy(
                    barbers  = if (bRes is Resource.Success) bRes.data else s.barbers,
                    services = if (sRes is Resource.Success) sRes.data else s.services,
                )
            }
        }
    }

    /** Cierra el diálogo de edición */
    fun dismissEditDialog() { _state.update { it.copy(showEditDialog = false, editingBooking = null) } }

    /** [F5] Actualiza la reserva; la pantalla detecta successMessage y llama lazyPagingItems.refresh() */
    fun updateBooking(id: Long, barberId: Long, fechaReserva: String, startTime: String, serviceIds: List<Long>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, showEditDialog = false, editingBooking = null) }
            when (val result = repository.updateBooking(id, barberId, fechaReserva, startTime, serviceIds)) {
                is Resource.Success -> _state.update { it.copy(successMessage = "Reserva actualizada exitosamente", isLoading = false) }
                is Resource.Error   -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    /** [F5] Crea la reserva; la pantalla detecta successMessage y llama lazyPagingItems.refresh() */
    fun createBooking(clientId: Long, barberId: Long, fechaReserva: String, startTime: String, serviceIds: List<Long>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, showCreateDialog = false) }
            when (val result = repository.createBooking(clientId, barberId, fechaReserva, startTime, serviceIds)) {
                is Resource.Success -> _state.update { it.copy(successMessage = "Reserva creada exitosamente", isLoading = false) }
                is Resource.Error   -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError()   { _state.update { it.copy(error = null) } }
    fun clearSuccess() { _state.update { it.copy(successMessage = null) } }
}
