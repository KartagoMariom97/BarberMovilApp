package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminDashboardUiState(
    val nombres: String = "",
    val role: String = "",
    val isLoggedOut: Boolean = false,
)

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val uiState: StateFlow<AdminDashboardUiState> =
        userPreferencesRepository.userPreferences.map { prefs ->
            AdminDashboardUiState(
                nombres = prefs.nombres,
                role = prefs.role,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AdminDashboardUiState(),
        )

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
