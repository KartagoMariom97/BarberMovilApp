package com.barber.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterState(
    val nombres: String = "",
    val fechaNacimiento: String = "",
    val dni: String = "",
    val genero: String = "Masculino",
    val email: String = "",
    val telefono: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterState())
    val state: StateFlow<RegisterState> = _state.asStateFlow()

    fun onNombresChange(value: String) { _state.value = _state.value.copy(nombres = value, error = null) }
    fun onFechaNacimientoChange(value: String) { _state.value = _state.value.copy(fechaNacimiento = value, error = null) }
    fun onDniChange(value: String) { _state.value = _state.value.copy(dni = value, error = null) }
    fun onGeneroChange(value: String) { _state.value = _state.value.copy(genero = value, error = null) }
    fun onEmailChange(value: String) { _state.value = _state.value.copy(email = value, error = null) }
    fun onTelefonoChange(value: String) { _state.value = _state.value.copy(telefono = value, error = null) }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun register() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val s = _state.value
            when (val result = registerUseCase(s.nombres, s.fechaNacimiento, s.dni, s.genero, s.email, s.telefono)) {
                is Resource.Success -> {
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
