package com.barber.app.presentation.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminProfileUiState(
    val nombres: String = "",
    val email: String = "",
    val role: String = "",
    val entityId: Long = -1L,
    val userId: Long = -1L,
    val isLoggedOut: Boolean = false,
    // edit dialog
    val showEditDialog: Boolean = false,
    val isSaving: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    // change password
    val showChangePasswordDialog: Boolean = false,
    val isChangingPassword: Boolean = false,
    val changePasswordError: String? = null,
    val changePasswordSuccess: Boolean = false,
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
            _state.update {
                it.copy(
                    nombres = prefs.nombres,
                    email = prefs.email,
                    role = prefs.role,
                    entityId = prefs.entityId,
                    userId = prefs.userId,
                )
            }
        }
    }

    fun showEditDialog()    { _state.update { it.copy(showEditDialog = true, saveError = null, saveSuccess = false) } }
    fun dismissEditDialog() { _state.update { it.copy(showEditDialog = false, saveError = null) } }
    fun clearSaveError()    { _state.update { it.copy(saveError = null) } }
    fun clearSaveSuccess()  { _state.update { it.copy(saveSuccess = false) } }

    fun showChangePasswordDialog()    { _state.update { it.copy(showChangePasswordDialog = true, changePasswordError = null) } }
    fun dismissChangePasswordDialog() { _state.update { it.copy(showChangePasswordDialog = false, changePasswordError = null) } }
    fun clearChangePasswordSuccess()  { _state.update { it.copy(changePasswordSuccess = false) } }

    fun changePassword(newPassword: String) {
        val userId = _state.value.userId
        if (userId <= 0L) {
            _state.update { it.copy(changePasswordError = "No se pudo obtener el ID de usuario.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isChangingPassword = true, changePasswordError = null) }
            when (val result = authRepository.changePassword(userId, newPassword)) {
                is Resource.Success -> _state.update {
                    it.copy(
                        isChangingPassword = false,
                        showChangePasswordDialog = false,
                        changePasswordSuccess = true,
                    )
                }
                is Resource.Error -> _state.update {
                    it.copy(isChangingPassword = false, changePasswordError = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun updateProfile(nombres: String, email: String, password: String?) {
        val userId = _state.value.userId
        if (userId <= 0L) {
            _state.update { it.copy(saveError = "No se pudo obtener el ID de usuario.") }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSaving = true, saveError = null) }
            when (val result = authRepository.updateAdminProfile(userId, nombres, email, password)) {
                is Resource.Success -> {
                    _state.update {
                        it.copy(
                            nombres = nombres,
                            email = email,
                            isSaving = false,
                            showEditDialog = false,
                            saveSuccess = true,
                        )
                    }
                }
                is Resource.Error -> _state.update { it.copy(isSaving = false, saveError = result.message) }
                is Resource.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _state.update { it.copy(isLoggedOut = true) }
        }
    }
}
