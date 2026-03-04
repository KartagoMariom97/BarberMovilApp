package com.barber.app.presentation.admin

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.repository.AuthRepository
import com.barber.app.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class AdminLoginState(
    val email: String = "",
    val password: String = "",
    val role: String = "ADMIN",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val nombres: String = "",
)

@HiltViewModel
class AdminLoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    // [FIX] inyectado para registrar FCM token post-login admin/barber
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AdminLoginState())
    val state: StateFlow<AdminLoginState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun onRoleChange(role: String) {
        _state.value = _state.value.copy(role = role, error = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun login() {
        val current = _state.value
        if (current.email.isBlank()) {
            _state.value = current.copy(error = "El email es obligatorio")
            return
        }
        if (current.password.isBlank()) {
            _state.value = current.copy(error = "La contraseña es obligatoria")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = authRepository.login(
                email = current.email,
                password = current.password
            )) {
                is Resource.Success -> {
                    val prefs = userPreferencesRepository.userPreferences.first()
                    // [FIX] registrar FCM token post-login admin/barber;
                    // sin esto el token queda NULL en BD y las notificaciones nunca llegan.
                    runCatching {
                        val fcmToken = FirebaseMessaging.getInstance().token.await()
                        withContext(Dispatchers.IO) { notificationRepository.updateFcmToken(fcmToken) }
                    }.onFailure { e ->
                        Log.e("FCM_TOKEN", "Error registrando FCM token post-login admin: ${e.message}")
                    }
                    _state.value = _state.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        nombres = prefs.nombres,
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message,
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
