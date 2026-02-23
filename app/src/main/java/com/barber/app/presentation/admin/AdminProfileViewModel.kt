package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminProfileUiState(
    val nombres: String = "",
    val email: String = "",
    val role: String = "",
    val entityId: Long = -1L,
    val isLoggedOut: Boolean = false,
)

@HiltViewModel
class AdminProfileViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminProfileUiState())
    val state: StateFlow<AdminProfileUiState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.userPreferences.first()
            _state.value = AdminProfileUiState(
                nombres = prefs.nombres,
                email = prefs.email,
                role = prefs.role,
                entityId = prefs.entityId,
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }
}
