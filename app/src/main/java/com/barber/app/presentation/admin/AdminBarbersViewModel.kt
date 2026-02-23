package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminBarber
import com.barber.app.domain.repository.AdminBarberRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminBarbersState(
    val barbers: List<AdminBarber> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AdminBarbersViewModel @Inject constructor(
    private val repository: AdminBarberRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminBarbersState())
    val state: StateFlow<AdminBarbersState> = _state.asStateFlow()

    init { loadBarbers() }

    fun loadBarbers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getAllBarbers()) {
                is Resource.Success -> _state.update { it.copy(barbers = result.data, isLoading = false) }
                is Resource.Error   -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateBarber(id: Long, nombres: String?, email: String?, telefono: String?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.updateBarber(id, nombres, email, telefono)) {
                is Resource.Success -> {
                    _state.update { state ->
                        state.copy(
                            barbers = state.barbers.map { if (it.codigoBarbero == id) result.data else it },
                            isLoading = false,
                            successMessage = "Barbero actualizado",
                        )
                    }
                }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun toggleActive(id: Long) {
        viewModelScope.launch {
            when (val result = repository.toggleActive(id)) {
                is Resource.Success -> {
                    _state.update { state ->
                        state.copy(barbers = state.barbers.map { if (it.codigoBarbero == id) result.data else it })
                    }
                }
                is Resource.Error -> _state.update { it.copy(error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError()   { _state.update { it.copy(error = null) } }
    fun clearSuccess() { _state.update { it.copy(successMessage = null) } }
}
