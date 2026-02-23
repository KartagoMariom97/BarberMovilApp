package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminBooking
import com.barber.app.domain.repository.AdminBookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBookingsState(
    val bookings: List<AdminBooking> = emptyList(),
    val statusFilter: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AdminBookingsViewModel @Inject constructor(
    private val repository: AdminBookingRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminBookingsState())
    val state: StateFlow<AdminBookingsState> = _state.asStateFlow()

    init { loadBookings() }

    fun loadBookings(status: String? = _state.value.statusFilter) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, statusFilter = status) }
            when (val result = repository.getAllBookings(status = status)) {
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

    fun clearError()   { _state.update { it.copy(error = null) } }
    fun clearSuccess() { _state.update { it.copy(successMessage = null) } }
}
