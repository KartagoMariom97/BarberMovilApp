package com.barber.app.presentation.appointments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.model.Booking
import com.barber.app.domain.usecase.CancelBookingUseCase
import com.barber.app.domain.usecase.GetClientBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.barber.app.domain.usecase.UpdateBookingUseCase
import com.barber.app.domain.usecase.GetBarbersUseCase
import com.barber.app.domain.usecase.GetServicesUseCase

import com.barber.app.core.websocket.StompWebSocketManager
import com.barber.app.domain.model.Barber
import com.barber.app.domain.model.Service

import kotlinx.coroutines.flow.update
import kotlinx.coroutines.flow.first


data class AppointmentsState(
    val bookings: List<Booking> = emptyList(),
    val barbers: List<Barber> = emptyList(),
    val services: List<Service> = emptyList(),
    val clientId: Long = 0L,
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val updateSuccess: Boolean = false,
    // [FIX-3] true cuando el admin editó datos estructurales de una reserva del cliente
    val showAdminUpdatedDialog: Boolean = false,
)

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val getClientBookingsUseCase: GetClientBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val updateBookingUseCase: UpdateBookingUseCase,
    private val getBarbersUseCase: GetBarbersUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
    // [FIX-5] Inyectado para escuchar cambios en tiempo real vía WebSocket
    private val stompWebSocketManager: StompWebSocketManager,
) : ViewModel() {

    private val _state = MutableStateFlow(AppointmentsState())
    val state: StateFlow<AppointmentsState> = _state.asStateFlow()

    // [FIX-3] Flag para evitar falso positivo: cuando el cliente mismo edita,
    // el WebSocket notifica su propio cambio — no debe activar el dialog de "admin editó"
    private var clientJustEdited = false

    init {
        loadBookings()
        loadBarbers()
        loadServices()
        // Recarga silenciosa cuando llega notificación WebSocket de cambio de estado
        viewModelScope.launch {
            stompWebSocketManager.notifications.collect { loadBookings(isRefresh = true) }
        }
    }

    // ✅ FUNCIÓN PÚBLICA (NO suspend)
// Esta es la que puedes llamar desde cualquier parte
    fun loadBookings(isRefresh: Boolean = false) {
        viewModelScope.launch {
            loadBookingsInternal(isRefresh)
        }
    }

    private suspend fun loadBookingsInternal(isRefresh: Boolean) {
        // Guardar snapshot previo antes de recargar (para detección de edición del admin)
        val previousBookings = if (isRefresh) _state.value.bookings else emptyList()

        if (isRefresh) {
            _state.update { it.copy(isRefreshing = true) }
        } else {
            _state.update { it.copy(isLoading = true) }
        }

        val prefs = userPreferencesRepository.userPreferences.first()
        val userId = prefs.clientId

        when (val result = getClientBookingsUseCase(userId)) {

            is Resource.Success -> {
                // [FIX-FILTERS] Sin filtro en ViewModel — la UI filtra por chip activo
                // CANCELLED y COMPLETED llegan al estado para que los chips los muestren
                val newBookings = result.data
                    .sortedByDescending { booking -> booking.fechaReserva }

                // [FIX-3] Detectar si el admin editó datos estructurales de una reserva.
                // Solo aplica en refresh (WebSocket) y cuando el cliente NO acaba de editar.
                // Comparamos barbero, fecha u hora: si alguno cambió sin que el cliente lo iniciara
                // → el admin modificó la reserva.
                val adminEdited = isRefresh && !clientJustEdited && newBookings.any { newB ->
                    val old = previousBookings.find { it.id == newB.id } ?: return@any false
                    newB.barberName != old.barberName ||
                        newB.fechaReserva != old.fechaReserva ||
                        newB.startTime.take(5) != old.startTime.take(5)
                }
                // Resetear flag: el siguiente refresh ya no es del cliente
                if (isRefresh) clientJustEdited = false

                _state.update {
                    it.copy(
                        bookings = newBookings,
                        clientId = userId,
                        isLoading = false,
                        isRefreshing = false,
                        error = null,
                        // [FIX-4] OR: nunca baja a false automáticamente; solo clearAdminUpdatedDialog()
                        showAdminUpdatedDialog = it.showAdminUpdatedDialog || adminEdited,
                    )
                }
            }

            is Resource.Error -> {
                if (isRefresh) clientJustEdited = false
                _state.update {
                    it.copy(
                        error = result.message,
                        isLoading = false,
                        isRefreshing = false,
                    )
                }
            }

            is Resource.Loading -> {
                _state.update { it.copy(isLoading = true) }
            }
        }
    }

    private fun loadBarbers() {
        viewModelScope.launch {
            when (val result = getBarbersUseCase()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(barbers = result.data)
                }
            else -> Unit
            }
        }
    }

    private fun loadServices() {
        viewModelScope.launch {
            when (val result = getServicesUseCase()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(services = result.data)
            }
            else -> Unit
            }
        }
    }

    fun updateBooking(
        bookingId: Long,
        clientId: Long,
        barberId: Long,
        fecha: String,
        hora: String,
        serviceIds: List<Long>
    ) {
    viewModelScope.launch {

        _state.update { it.copy(isLoading = true) }
        when (
            val result = updateBookingUseCase(
                bookingId,
                clientId,
                barberId,
                fecha,
                hora,
                serviceIds
            )
        ) {
            is Resource.Success -> {
                // [FIX-3] Marcar que fue el cliente quien editó; el WebSocket que llega
                // a continuación corresponde a su propio cambio, no a una edición del admin
                clientJustEdited = true
                loadBookings()
                _state.update {
                    it.copy(
                        updateSuccess = true,
                        isLoading = false,
                        error = null
                    )
                }
            }
            is Resource.Error -> {
                _state.update {
                    it.copy(
                        error = result.message,
                        isLoading = false
                    )
                }
            }
                else -> Unit
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun cancelBooking(bookingId: Long) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = cancelBookingUseCase(bookingId)) {
                is Resource.Success -> {

                    // 👇 ESPERAMOS que termine
                    loadBookings()

                    _state.update { it.copy(isLoading = false) }
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearUpdateSuccess() {
        _state.update { it.copy(updateSuccess = false) }
    }

    // [FIX-3] El usuario presionó Aceptar en el dialog de "admin editó tu reserva"
    fun clearAdminUpdatedDialog() {
        _state.update { it.copy(showAdminUpdatedDialog = false) }
    }
}
