package com.barber.app.presentation.booking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.model.Barber
import com.barber.app.domain.model.Booking
import com.barber.app.domain.model.Service
import com.barber.app.domain.usecase.CheckAvailabilityUseCase
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
    // [F11] Verifica disponibilidad del barbero antes de avanzar a confirmación
    private val checkAvailabilityUseCase: CheckAvailabilityUseCase,
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

    /**
     * [F8] Re-ejecuta la última operación fallida según el step actual.
     * Llamar desde la UI cuando se muestra el botón "Reintentar" en estado de error.
     */
    fun retry() {
        when (_state.value.currentStep) {
            BookingStep.BARBER        -> loadBarbers()
            BookingStep.SERVICES      -> loadServices()
            BookingStep.CONFIRMATION  -> confirmBooking()
            else                      -> Unit
        }
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
                // [F11] Validar disponibilidad del barbero antes de mostrar la confirmación
                validateAvailabilityAndAdvance(s)
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

    // [F11] Verifica disponibilidad async; avanza a CONFIRMATION si OK o muestra error
    private fun validateAvailabilityAndAdvance(s: BookingState) {
        val normalizedTime = if (s.selectedTime.matches(Regex("\\d{2}:\\d{2}")))
            "${s.selectedTime}:00" else s.selectedTime
        val totalMinutes = s.services
            .filter { it.id in s.selectedServices }
            .sumOf { it.estimatedMinutes }

        viewModelScope.launch {
            _state.value = s.copy(isLoading = true, error = null)
            val available = checkAvailabilityUseCase(
                barberId = s.selectedBarber!!.codigoBarbero,
                date = s.selectedDate,
                startTime = normalizedTime,
                totalMinutes = totalMinutes,
            )
            _state.value = if (available) {
                _state.value.copy(currentStep = BookingStep.CONFIRMATION, isLoading = false)
            } else {
                _state.value.copy(
                    isLoading = false,
                    error = "El barbero no está disponible en ese horario. Por favor elige otro.",
                )
            }
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
                    // Marcar como vista para que HomeScreen no la muestre como "nueva del admin"
                    userPreferencesRepository.markBookingsAsSeen(setOf(result.data.id))
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
