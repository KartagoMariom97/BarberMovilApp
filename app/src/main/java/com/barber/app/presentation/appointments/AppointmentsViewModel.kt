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
    val error: String? = null,
    val updateSuccess: Boolean = false   // 👈 NUEVO
)

@HiltViewModel
class AppointmentsViewModel @Inject constructor(
    private val getClientBookingsUseCase: GetClientBookingsUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val updateBookingUseCase: UpdateBookingUseCase,
    private val getBarbersUseCase: GetBarbersUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AppointmentsState())
    val state: StateFlow<AppointmentsState> = _state.asStateFlow()

    init {
        loadBookings()
        loadBarbers()
        loadServices()
    }

    fun loadBookings() {
    viewModelScope.launch {

        _state.update { it.copy(isLoading = true) }

        val prefs = userPreferencesRepository.userPreferences.first()
        val userId = prefs.clientId

        when (val result = getClientBookingsUseCase(userId)) {

            is Resource.Success -> {
                _state.update {
                    it.copy(
                        bookings = result.data
                            .filter { booking -> booking.status != "CANCELLED" }
                            .sortedByDescending { booking -> booking.fechaReserva },
                        clientId = userId,
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

            is Resource.Loading -> {
                _state.update {
                    it.copy(isLoading = true)
                }
            }
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
                is Resource.Success -> loadBookings()
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
}
