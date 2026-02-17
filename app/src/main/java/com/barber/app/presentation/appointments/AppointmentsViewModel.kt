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

data class AppointmentsState(
    val bookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val getClientBookingsUseCase: GetClientBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AppointmentsState())
    val state: StateFlow<AppointmentsState> = _state.asStateFlow()

    init {
        loadBookings()
    }

    fun loadBookings() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val prefs = userPreferencesRepository.userPreferences.first()

            if (prefs.clientId <= 0) {
                _state.value = _state.value.copy(isLoading = false, error = "No se encontró sesión activa")
                return@launch
            }

            when (val result = getClientBookingsUseCase(prefs.clientId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        bookings = result.data
                            .filter { it.status.uppercase() != "CANCELLED" }
                            .sortedByDescending { it.fechaReserva },
                        isLoading = false,
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> Unit
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
                is Resource.Success -> loadBookings()
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
