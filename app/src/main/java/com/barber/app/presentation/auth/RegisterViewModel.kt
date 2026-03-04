package com.barber.app.presentation.auth

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.domain.repository.NotificationRepository
import com.barber.app.domain.usecase.LoginUseCase
import com.barber.app.domain.usecase.RegisterUseCase
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

data class RegisterState(
    val nombres: String = "",
    val fechaNacimiento: String = "",
    val dni: String = "",
    val genero: String = "Masculino",
    val email: String = "",
    val telefono: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
    // [FIX] auto-login post-registro para obtener JWT y registrar FCM token
    private val loginUseCase: LoginUseCase,
    private val notificationRepository: NotificationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onNombresChange(value: String) { _state.value = _state.value.copy(nombres = value, error = null) }
    fun onFechaNacimientoChange(value: String) { _state.value = _state.value.copy(fechaNacimiento = value, error = null) }
    fun onDniChange(value: String) { _state.value = _state.value.copy(dni = value, error = null) }
    fun onGeneroChange(value: String) { _state.value = _state.value.copy(genero = value, error = null) }
    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value, error = null) }
    fun onTelefonoChange(value: String) { _state.value = _state.value.copy(telefono = value, error = null) }
    fun onPasswordChange(value: String) { _state.value = _state.value.copy(password = value, error = null) }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun register() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val s = _state.value
            when (val result = registerUseCase(s.nombres, s.fechaNacimiento, s.dni, s.genero, s.email, s.telefono,s.password)) {
                is Resource.Success -> {
                    // [FIX] El registro no devuelve JWT; se hace auto-login para obtenerlo,
                    // luego se registra el FCM token. Sin esto fcm_token queda NULL en BD.
                    runCatching {
                        loginUseCase(s.email, s.password)
                        val fcmToken = FirebaseMessaging.getInstance().token.await()
                        withContext(Dispatchers.IO) { notificationRepository.updateFcmToken(fcmToken) }
                    }.onFailure { e ->
                        Log.e("FCM_TOKEN", "Error registrando FCM token post-registro: ${e.message}")
                    }
                    _state.value = _state.value.copy(isLoading = false, isSuccess = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(isLoading = false, error = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
