package com.barber.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.model.ClientProfile
import com.barber.app.domain.usecase.GetProfileUseCase
import com.barber.app.domain.usecase.LogoutUseCase
import com.barber.app.domain.usecase.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val profile: ClientProfile? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
    val isUpdating: Boolean = false,
    val updateError: String? = null,
    val updateSuccess: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val prefs = userPreferencesRepository.userPreferences.first()

            if (prefs.clientId <= 0) {
                _state.value = _state.value.copy(isLoading = false, error = "No se encontró sesión activa")
                return@launch
            }

            when (val result = getProfileUseCase(prefs.clientId)) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(profile = result.data, isLoading = false)
                }
                is Resource.Error -> {
                    // Fallback to DataStore data
                    _state.value = _state.value.copy(
                        profile = ClientProfile(
                            id = prefs.clientId,
                            nombres = prefs.nombres,
                            email = prefs.email,
                            telefono = prefs.telefono,
                            dni = prefs.dni,
                        ),
                        isLoading = false,
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateProfile(nombres: String, genero: String, email: String, telefono: String, dni: String) {
        viewModelScope.launch {
            val prefs = userPreferencesRepository.userPreferences.first()
            _state.value = _state.value.copy(isUpdating = true, updateError = null, updateSuccess = false)

            when (val result = updateProfileUseCase(prefs.clientId, nombres, genero, email, telefono, dni)) {
                is Resource.Success -> {
                    val updatedProfile = result.data!!
                    userPreferencesRepository.saveSession(
                        clientId = prefs.clientId,
                        userId = prefs.userId,
                        nombres = updatedProfile.nombres,
                        email = updatedProfile.email,
                        telefono = updatedProfile.telefono,
                        dni = updatedProfile.dni,
                    )
                    _state.value = _state.value.copy(
                        profile = updatedProfile,
                        isUpdating = false,
                        updateSuccess = true,
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isUpdating = false,
                        updateError = result.message,
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun clearUpdateError() {
        _state.value = _state.value.copy(updateError = null)
    }

    fun clearUpdateSuccess() {
        _state.value = _state.value.copy(updateSuccess = false)
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }
}
