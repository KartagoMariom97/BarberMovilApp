package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.repository.AdminBookingRepository
import com.barber.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val nombres: String = "",
    val role: String = "",
    val isLoggedOut: Boolean = false,
    /** Reservas PENDING nuevas pendientes de aprobación */
    val pendingCount: Int = 0,
    val showPendingAlert: Boolean = false,
    /** Reservas MODIFIED_PENDING: cliente modificó y espera aprobación del admin */
    val modifiedPendingCount: Int = 0,
    val showModifiedPendingAlert: Boolean = false,
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val bookingRepository: AdminBookingRepository,
) : ViewModel() {

    // Estado separado para controlar la navegación de logout (corrige bug: nunca se activaba)
    private val _loggedOut              = MutableStateFlow(false)
    private val _pendingCount           = MutableStateFlow(0)
    private val _modifiedPendingCount   = MutableStateFlow(0)
    // Muestra el AlertDialog de reservas pendientes una sola vez al abrir; en orden de prioridad
    private val _showPendingAlert         = MutableStateFlow(false)
    private val _showModifiedPendingAlert = MutableStateFlow(false)

    val uiState: StateFlow<AdminDashboardUiState> = combine(
        userPreferencesRepository.userPreferences,
        _loggedOut,
        _pendingCount,
        _showPendingAlert,
    ) { prefs, loggedOut, pendingCount, showAlert ->
        AdminDashboardUiState(
            nombres = prefs.nombres,
            role = prefs.role,
            isLoggedOut = loggedOut,
            pendingCount = pendingCount,
            showPendingAlert = showAlert,
        )
    }.combine(_modifiedPendingCount) { state, modifiedCount ->
        // Agrega conteo de MODIFIED_PENDING en cadena para evitar combine de 5+ flujos
        state.copy(modifiedPendingCount = modifiedCount)
    }.combine(_showModifiedPendingAlert) { state, showModified ->
        state.copy(showModifiedPendingAlert = showModified)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AdminDashboardUiState(),
    )

    init {
        loadPendingCounts()
    }

    /** Carga reservas PENDING y MODIFIED_PENDING en una sola llamada; muestra alertas en orden de prioridad */
    private fun loadPendingCounts() {
        viewModelScope.launch {
            // Carga todas las reservas sin filtro para contar por estado en el cliente
            when (val result = bookingRepository.getAllBookings(status = null)) {
                is Resource.Success -> {
                    val pending  = result.data.count { it.status.uppercase() == "PENDING" }
                    val modified = result.data.count { it.status.uppercase() == "MODIFIED_PENDING" }
                    _pendingCount.value = pending
                    _modifiedPendingCount.value = modified
                    // Prioridad: primero PENDING; si no hay, mostrar MODIFIED_PENDING
                    when {
                        pending  > 0 -> _showPendingAlert.value = true
                        modified > 0 -> _showModifiedPendingAlert.value = true
                    }
                }
                else -> Unit
            }
        }
    }

    /** Al cerrar el alert de PENDING, mostrar el de MODIFIED_PENDING si corresponde */
    fun dismissPendingAlert() {
        _showPendingAlert.value = false
        if (_modifiedPendingCount.value > 0) _showModifiedPendingAlert.value = true
    }

    fun dismissModifiedPendingAlert() { _showModifiedPendingAlert.value = false }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loggedOut.value = true
        }
    }
}
