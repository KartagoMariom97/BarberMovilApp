package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.domain.model.AdminClient
import com.barber.app.domain.repository.AdminClientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminClientsState(
    val clients: List<AdminClient> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    /** Controla visibilidad del diálogo de creación de cliente */
    val showCreateDialog: Boolean = false,
)

@HiltViewModel
class AdminClientsViewModel @Inject constructor(
    private val repository: AdminClientRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminClientsState())
    val state: StateFlow<AdminClientsState> = _state.asStateFlow()

    init { loadClients() }

    fun loadClients() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.getAllClients()) {
                is Resource.Success -> _state.update { it.copy(clients = result.data, isLoading = false) }
                is Resource.Error   -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateClient(
        id: Long,
        nombres: String?,
        email: String?,
        telefono: String?,
        password: String? = null,
        dni: String? = null,
        genero: String? = null,
        fechaNacimiento: String? = null,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            when (val result = repository.updateClient(id, nombres, email, telefono, password, dni, genero, fechaNacimiento)) {
                is Resource.Success -> {
                    _state.update { state ->
                        state.copy(
                            clients = state.clients.map { if (it.codigoCliente == id) result.data else it },
                            isLoading = false,
                            successMessage = "Cliente actualizado",
                        )
                    }
                }
                is Resource.Error -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    /** Abre el diálogo de creación */
    fun showCreateDialog()    { _state.update { it.copy(showCreateDialog = true) } }
    /** Cierra el diálogo de creación */
    fun dismissCreateDialog() { _state.update { it.copy(showCreateDialog = false) } }

    /** Crea un cliente nuevo y recarga la lista tras éxito */
    fun createClient(
        nombres: String, fechaNacimiento: String, dni: String, genero: String,
        email: String?, telefono: String, password: String?,
    ) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, showCreateDialog = false) }
            when (val result = repository.createClient(nombres, fechaNacimiento, dni, genero, email, telefono, password)) {
                is Resource.Success -> {
                    _state.update { it.copy(successMessage = "Cliente creado exitosamente") }
                    loadClients() // recarga lista para obtener datos completos
                }
                is Resource.Error   -> _state.update { it.copy(error = result.message, isLoading = false) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun toggleClientStatus(id: Long, active: Boolean) {
        viewModelScope.launch {
            when (val result = repository.updateClientStatus(id, active)) {
                is Resource.Success -> _state.update { state ->
                    state.copy(
                        clients = state.clients.map { if (it.codigoCliente == id) result.data else it },
                        successMessage = if (active) "Cliente activado" else "Cliente desactivado",
                    )
                }
                is Resource.Error   -> _state.update { it.copy(error = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearError()   { _state.update { it.copy(error = null) } }
    fun clearSuccess() { _state.update { it.copy(successMessage = null) } }
}
