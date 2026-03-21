package com.barber.app.presentation.auth

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.repository.NotificationRepository
import com.barber.app.domain.usecase.LoginUseCase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class LoginState(
    val email: String = "",
    val password: String = "",      // Campo contraseña para login JWT
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    // true cuando el backend responde 403 ACCOUNT_DISABLED
    val accountDisabled: Boolean = false,
    // [F9] true cuando el dispositivo soporta biometría y el usuario tiene sesión previa guardada
    val biometricAvailable: Boolean = false,
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    // [FIX] inyectado para registrar FCM token post-login
    private val notificationRepository: NotificationRepository,
    // [F9] Para verificar si existe sesión previa antes de ofrecer biometría
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun onPasswordChange(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun clearAccountDisabled() {
        _state.value = _state.value.copy(accountDisabled = false)
    }

    /**
     * [F9] Verificar si biometría está disponible Y el usuario tiene token guardado.
     * Llamar desde LoginScreen con LaunchedEffect(Unit) pasando el contexto.
     */
    fun checkBiometricAvailability(context: Context) {
        viewModelScope.launch {
            val tokenExists = !userPreferencesRepository.getTokenOnce().isNullOrBlank()
            if (!tokenExists) return@launch

            val biometricManager = BiometricManager.from(context)
            val canAuthenticate = biometricManager.canAuthenticate(BIOMETRIC_STRONG)
            _state.value = _state.value.copy(
                biometricAvailable = canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
            )
        }
    }

    /**
     * [F9] Llamar desde LoginScreen cuando BiometricPrompt retorna onAuthenticationSucceeded.
     * El token ya existe en DataStore — AuthInterceptor lo cargará en la primera request.
     */
    fun onBiometricSuccess() {
        _state.value = _state.value.copy(isSuccess = true)
    }

    fun login() {
        val current = _state.value
        if (current.email.isBlank()) {
            _state.value = current.copy(error = "Ingresa tu correo electrónico.")
            return
        }
        if (current.password.isBlank()) {
            _state.value = current.copy(error = "Ingresa tu contraseña.")
            return
        }
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = loginUseCase(_state.value.email, _state.value.password)) {
                is Resource.Success -> {
                    // [FIX] registrar FCM token post-login; el token ya existe localmente
                    // así que await() es inmediato y no retrasa la navegación perceptiblemente.
                    // Si falla (red, backend), se loguea y la navegación continúa igual.
                    runCatching {
                        val fcmToken = FirebaseMessaging.getInstance().token.await()
                        withContext(Dispatchers.IO) { notificationRepository.updateFcmToken(fcmToken) }
                    }.onFailure { e ->
                        Log.e("FCM_TOKEN", "Error registrando FCM token post-login cliente: ${e.message}")
                    }
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }
                is Resource.Error -> {
                    // Detectar cuenta deshabilitada para mostrar AlertDialog específico
                    if (result.message?.contains("ACCOUNT_DISABLED", ignoreCase = true) == true ||
                        result.message?.contains("deshabilitada", ignoreCase = true) == true) {
                        _state.value = _state.value.copy(isLoading = false, accountDisabled = true)
                    } else {
                        _state.value = _state.value.copy(isLoading = false, error = result.message)
                    }
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
