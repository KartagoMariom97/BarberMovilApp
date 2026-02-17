package com.barber.app.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.model.Barber
import com.barber.app.domain.model.Booking
import com.barber.app.domain.model.Service
import com.barber.app.domain.usecase.CreateBookingUseCase
import com.barber.app.domain.usecase.GetBarbersUseCase
import com.barber.app.domain.usecase.GetServicesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class BookingStep { BARBER, SERVICES, DATETIME, CONFIRMATION }

data class BookingState(
    val currentStep: BookingStep = BookingStep.BARBER,
    val barbers: List<Barber> = emptyList(),
    val services: List<Service> = emptyList(),
    val selectedBarber: Barber? = null,
    val selectedServices: Set<Long> = emptySet(),
    val selectedDate: String = "",
    val selectedTime: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val bookingResult: Booking? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class BookingViewModel @Inject constructor(
    private val getBarbersUseCase: GetBarbersUseCase,
    private val getServicesUseCase: GetServicesUseCase,
    private val createBookingUseCase: CreateBookingUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookingState())
    val state: StateFlow<BookingState> = _state.asStateFlow()

    init {
        loadBarbers()
    }

    private fun loadBarbers() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = getBarbersUseCase()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(barbers = result.data, isLoading = false)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun loadServices() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            when (val result = getServicesUseCase()) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(services = result.data, isLoading = false)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(error = result.message, isLoading = false)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun selectBarber(barber: Barber) {
        _state.value = _state.value.copy(selectedBarber = barber, error = null)
    }

    fun toggleService(serviceId: Long) {
        val current = _state.value.selectedServices.toMutableSet()
        if (current.contains(serviceId)) current.remove(serviceId) else current.add(serviceId)
        _state.value = _state.value.copy(selectedServices = current, error = null)
    }

    fun onDateChange(date: String) {
        _state.value = _state.value.copy(selectedDate = date, error = null)
    }

    fun onTimeChange(time: String) {
        _state.value = _state.value.copy(selectedTime = time, error = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetState() {
        _state.value = BookingState(barbers = _state.value.barbers)
    }

    fun nextStep() {
        val s = _state.value
        when (s.currentStep) {
            BookingStep.BARBER -> {
                if (s.selectedBarber == null) {
                    _state.value = s.copy(error = "Selecciona un barbero")
                    return
                }
                _state.value = s.copy(currentStep = BookingStep.SERVICES, error = null)
                loadServices()
            }
            BookingStep.SERVICES -> {
                if (s.selectedServices.isEmpty()) {
                    _state.value = s.copy(error = "Selecciona al menos un servicio")
                    return
                }
                _state.value = s.copy(currentStep = BookingStep.DATETIME, error = null)
            }
            BookingStep.DATETIME -> {
                if (s.selectedDate.isBlank() || s.selectedTime.isBlank()) {
                    _state.value = s.copy(error = "Selecciona fecha y hora")
                    return
                }
                _state.value = s.copy(currentStep = BookingStep.CONFIRMATION, error = null)
            }
            BookingStep.CONFIRMATION -> confirmBooking()
        }
    }

    fun previousStep() {
        val s = _state.value
        when (s.currentStep) {
            BookingStep.SERVICES -> _state.value = s.copy(currentStep = BookingStep.BARBER, error = null)
            BookingStep.DATETIME -> _state.value = s.copy(currentStep = BookingStep.SERVICES, error = null)
            BookingStep.CONFIRMATION -> _state.value = s.copy(currentStep = BookingStep.DATETIME, error = null)
            else -> Unit
        }
    }

    fun goToStep(stepIndex: Int) {
        val currentIndex = BookingStep.entries.indexOf(_state.value.currentStep)
        if (stepIndex < currentIndex) {
            val targetStep = BookingStep.entries[stepIndex]
            _state.value = _state.value.copy(currentStep = targetStep, error = null)
        }
    }

    private fun confirmBooking() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val s = _state.value
            val prefs = userPreferencesRepository.userPreferences.first()

            // Normalize time to HH:mm:ss for backend
            val normalizedTime = if (s.selectedTime.matches(Regex("\\d{2}:\\d{2}"))) {
                "${s.selectedTime}:00"
            } else {
                s.selectedTime
            }

            when (val result = createBookingUseCase(
                clientId = prefs.clientId,
                barberId = s.selectedBarber!!.codigoBarbero,
                fechaReserva = s.selectedDate,
                startTime = normalizedTime,
                serviceIds = s.selectedServices.toList(),
            )) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        bookingResult = result.data,
                        isSuccess = true,
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
        }
    }
}
