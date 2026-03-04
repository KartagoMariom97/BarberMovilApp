package com.barber.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.core.websocket.StompWebSocketManager
import com.barber.app.domain.model.Booking
import com.barber.app.domain.usecase.GetClientBookingsUseCase
import com.barber.app.service.NotificationEventManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class HomeState(
    val userName: String = "",
    val upcomingBookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Reservas nuevas no vistas (creadas por el admin) — activa el dialog de nueva reserva */
    val newAdminBookings: List<Booking> = emptyList(),
    val showNewBookingDialog: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getClientBookingsUseCase: GetClientBookingsUseCase,
    // [FIX-5] Inyectado para escuchar cambios en tiempo real vía WebSocket
    private val stompWebSocketManager: StompWebSocketManager,
    // Canal global para propagar eventos de confirmación al dialog raíz en MainActivity
    private val notificationEventManager: NotificationEventManager,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // IDs de reservas confirmadas ya notificadas en esta sesión (evita re-mostrar en recargas)
    private val notifiedConfirmedIds = mutableSetOf<Long>()

    init {
        loadData()
        // [FIX-5] Recarga silenciosa cuando llega una notificación WebSocket de cambio de estado
        viewModelScope.launch {
            stompWebSocketManager.notifications.collect { loadData() }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /** Cierra el dialog de nueva reserva del admin y marca esas reservas como vistas */
    fun dismissNewBookingDialog() {
        val ids = _state.value.newAdminBookings.map { it.id }.toSet()
        _state.value = _state.value.copy(showNewBookingDialog = false, newAdminBookings = emptyList())
        viewModelScope.launch {
            userPreferencesRepository.markBookingsAsSeen(ids)
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val prefs = userPreferencesRepository.userPreferences.first()
            _state.value = _state.value.copy(userName = prefs.nombres)

            if (prefs.clientId > 0) {
                when (val result = getClientBookingsUseCase(prefs.clientId)) {
                    is Resource.Success -> {
                        // [FIX-4] Incluye MODIFIED_PENDING y limita a reservas de las próximas 24h
                        val now   = LocalDateTime.now()
                        val limit = now.plusHours(24)
                        val dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                        val upcoming = result.data.filter { booking ->
                            val status = booking.status.uppercase()
                            if (status !in listOf("PENDING", "CONFIRMED", "MODIFIED_PENDING")) return@filter false
                            runCatching {
                                val dt = LocalDateTime.parse(
                                    "${booking.fechaReserva} ${booking.startTime.take(5)}", dtFmt
                                )
                                dt.isAfter(now) && dt.isBefore(limit)
                            }.getOrDefault(true) // si no puede parsear, incluye para no perder datos
                        }
                        val confirmed = result.data.filter { it.status.uppercase() == "CONFIRMED" }
                        // Detectar confirmaciones nuevas (no notificadas en esta sesión)
                        val newConfirmed = confirmed.filter { it.id !in notifiedConfirmedIds }
                        // Emitir al dialog global solo si hay nuevas y no hay evento FCM ya activo
                        // (evita doble dialog cuando FCM llega en background y luego loadData recarga)
                        if (newConfirmed.isNotEmpty() && notificationEventManager.confirmedCount.value == 0) {
                            notificationEventManager.onBookingConfirmed(count = newConfirmed.size)
                        }
                        // Registrar todos los IDs confirmados para no re-notificar en recargas
                        notifiedConfirmedIds.addAll(confirmed.map { it.id })

                        // Detectar reservas nuevas no vistas (creadas por el admin)
                        // 🔥🔥🔥 CAMBIO IMPORTANTE AQUÍ
                        // Ahora SOLO consideramos como "nueva reserva del admin"
                        // aquellas que están en estado PENDING.
                        // CONFIRMED ya no entra aquí para evitar doble diálogo.
                        val seenIds = userPreferencesRepository.getSeenBookingIds()
                        val newBookings = result.data.filter { booking ->
                            booking.id !in seenIds &&
                                    booking.status.uppercase() == "PENDING" &&

                                    /**
                                     * 🔥 CAMBIO CRÍTICO
                                     * Solo consideramos como "nueva reserva del admin"
                                     * aquellas creadas por ADMIN.
                                     *
                                     * Si el cliente creó la reserva,
                                     * NO debe mostrarse este dialog.
                                     */
                                    booking.createdBy.uppercase() == "ADMIN"
                        }

                        _state.value = _state.value.copy(
                            upcomingBookings = upcoming,
                            isLoading = false,
                            newAdminBookings = newBookings,
                            showNewBookingDialog = newBookings.isNotEmpty(),
                        )
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = result.message,
                        )
                    }
                    is Resource.Loading -> Unit
                }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
