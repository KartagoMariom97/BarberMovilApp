package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.domain.model.Service
import com.barber.app.domain.repository.AdminServiceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

data class AdminServicesState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    // Indica que se está refrescando via pull-to-refresh
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

@HiltViewModel
class AdminServicesViewModel @Inject constructor(
    private val repository: AdminServiceRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminServicesState())
    val state: StateFlow<AdminServicesState> = _state.asStateFlow()

    init { loadServices() }

    fun loadServices() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getAllServices()) {
                is Resource.Success -> _state.update { it.copy(services = result.data, isLoading = false) }
                is Resource.Error   -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun createService(name: String, description: String?, estimatedMinutes: Int, price: BigDecimal) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.createService(name, description, estimatedMinutes, price)) {
                is Resource.Success -> {
                    _state.update { state ->
                        state.copy(
                            services = state.services + result.data,
                            isLoading = false,
                            successMessage = "Servicio creado",
                        )
                    }
                }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateService(id: Long, name: String?, description: String?, estimatedMinutes: Int?, price: BigDecimal?) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.updateService(id, name, description, estimatedMinutes, price)) {
                is Resource.Success -> {
                    _state.update { state ->
                        state.copy(
                            services = state.services.map { if (it.id == id) result.data else it },
                            isLoading = false,
                            successMessage = "Servicio actualizado",
                        )
                    }
                }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    // Reemplaza delete físico: desactiva el servicio (soft delete)
    fun deactivateService(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.deactivateService(id)) {
                is Resource.Success -> {
                    _state.update { state ->
                        // Actualiza el servicio en la lista con active = false
                        state.copy(
                            services = state.services.map { if (it.id == id) result.data else it },
                            isLoading = false,
                            successMessage = "Servicio desactivado",
                        )
                    }
                }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun activateService(id: Long) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.activateService(id)) {
                is Resource.Success -> {
                    _state.update { state ->
                        // Actualiza el servicio en la lista con active = true
                        state.copy(
                            services = state.services.map { if (it.id == id) result.data else it },
                            isLoading = false,
                            successMessage = "Servicio activado",
                        )
                    }
                }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    /** Pull-to-refresh: recarga sin loading indicator de pantalla completa */
    fun refresh() {
        viewModelScope.launch {
            _state.update { it.copy(isRefreshing = true) }
            when (val result = repository.getAllServices()) {
                is Resource.Success -> _state.update { it.copy(services = result.data, isRefreshing = false) }
                is Resource.Error   -> _state.update { it.copy(error = result.message, isRefreshing = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError()   { _state.update { it.copy(error = null) } }
    fun clearSuccess() { _state.update { it.copy(successMessage = null) } }
}
