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
    /** Cantidad de reservas pendientes — muestra AlertDialog al abrir el dashboard */
    val pendingCount: Int = 0,
    val showPendingAlert: Boolean = false,
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
    private val bookingRepository: AdminBookingRepository,
) : ViewModel() {

    // Estado separado para controlar la navegación de logout (corrige bug: nunca se activaba)
    private val _loggedOut       = MutableStateFlow(false)
    private val _pendingCount    = MutableStateFlow(0)
    // Muestra el AlertDialog de reservas pendientes una sola vez al abrir
    private val _showPendingAlert = MutableStateFlow(false)

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
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AdminDashboardUiState(),
    )

    init {
        loadPendingCount()
    }

    /** Carga las reservas PENDING y muestra AlertDialog si hay al menos una */
    private fun loadPendingCount() {
        viewModelScope.launch {
            when (val result = bookingRepository.getAllBookings(status = "PENDING")) {
                is Resource.Success -> {
                    val count = result.data.size
                    _pendingCount.value = count
                    _showPendingAlert.value = count > 0
                }
                else -> Unit
            }
        }
    }

    fun dismissPendingAlert() { _showPendingAlert.value = false }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _loggedOut.value = true
        }
    }
}
