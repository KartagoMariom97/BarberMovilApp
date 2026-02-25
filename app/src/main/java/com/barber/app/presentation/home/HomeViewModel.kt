package com.barber.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferences
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.model.Booking
import com.barber.app.domain.usecase.GetClientBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val userName: String = "",
    val upcomingBookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    /** Cantidad de reservas confirmadas detectadas al cargar — activa el dialog de notificación */
    val confirmedCount: Int = 0,
    val showConfirmedDialog: Boolean = false,
    /** Reservas nuevas no vistas (creadas por el admin) — activa el dialog de nueva reserva */
    val newAdminBookings: List<Booking> = emptyList(),
    val showNewBookingDialog: Boolean = false,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getClientBookingsUseCase: GetClientBookingsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    // Flag para mostrar el dialog de confirmación solo una vez por sesión
    private var confirmedDialogShown = false

    init {
        loadData()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    /** Cierra el dialog de reserva confirmada */
    fun dismissConfirmedDialog() {
        _state.value = _state.value.copy(showConfirmedDialog = false)
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
                        val upcoming = result.data.filter {
                            it.status.uppercase() in listOf("PENDING", "CONFIRMED")
                        }
                        val confirmed = result.data.filter { it.status.uppercase() == "CONFIRMED" }
                        // Muestra el dialog de confirmación solo la primera vez que se detectan reservas confirmadas
                        val showDialog = confirmed.isNotEmpty() && !confirmedDialogShown
                        if (showDialog) confirmedDialogShown = true

                        // Detectar reservas nuevas no vistas (creadas por el admin)
                        val seenIds = userPreferencesRepository.getSeenBookingIds()
                        val newBookings = result.data.filter {
                            it.id !in seenIds &&
                            it.status.uppercase() in listOf("PENDING", "CONFIRMED")
                        }

                        _state.value = _state.value.copy(
                            upcomingBookings = upcoming,
                            isLoading = false,
                            confirmedCount = confirmed.size,
                            showConfirmedDialog = showDialog,
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
