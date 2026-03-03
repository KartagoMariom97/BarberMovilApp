package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBookingsState(
    val bookings: List<AdminBooking> = emptyList(),
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

    init { loadBookings() }

    fun loadBookings(status: String? = _state.value.statusFilter) {
        // [FIX-2] Cuando el filtro UI es PENDING, cargamos todo del backend (status=null)
        // para que MODIFIED_PENDING también llegue; la UI filtra localmente ambos estados.
        val networkStatus = if (status == "PENDING") null else status
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, statusFilter = status) }
            when (val result = repository.getAllBookings(status = networkStatus)) {
                is Resource.Success -> _state.update { it.copy(bookings = result.data, isLoading = false) }
                is Resource.Error   -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun changeStatus(id: Long, newStatus: String) {
        viewModelScope.launch {
            when (val result = repository.changeStatus(id, newStatus)) {
                is Resource.Success -> {
                    _state.update { state ->
                        state.copy(
                            bookings = state.bookings.map { if (it.id == id) result.data else it },
                            successMessage = "Estado actualizado a $newStatus",
                        )
                    }
                }
                is Resource.Error -> _state.update { it.copy(error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun setFilter(status: String?) { loadBookings(status) }

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

    /** Actualiza la reserva y recarga la lista del servidor para reflejar los cambios correctamente */
    fun updateBooking(id: Long, barberId: Long, fechaReserva: String, startTime: String, serviceIds: List<Long>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, showEditDialog = false, editingBooking = null) }
            when (val result = repository.updateBooking(id, barberId, fechaReserva, startTime, serviceIds)) {
                is Resource.Success -> {
                    // Fix: recarga del servidor para garantizar datos completos (status, servicios)
                    // y respetar el filtro activo (ej: si hay filtro PENDING, la reserva sigue apareciendo)
                    _state.update { it.copy(successMessage = "Reserva actualizada exitosamente") }
                    loadBookings()
                }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    /** Crea la reserva y recarga la lista tras éxito */
    fun createBooking(clientId: Long, barberId: Long, fechaReserva: String, startTime: String, serviceIds: List<Long>) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, showCreateDialog = false) }
            when (val result = repository.createBooking(clientId, barberId, fechaReserva, startTime, serviceIds)) {
                is Resource.Success -> {
                    _state.update { it.copy(successMessage = "Reserva creada exitosamente") }
                    loadBookings() // recarga la lista para mostrar la nueva reserva
                }
                is Resource.Error   -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError()   { _state.update { it.copy(error = null) } }
    fun clearSuccess() { _state.update { it.copy(successMessage = null) } }
}
