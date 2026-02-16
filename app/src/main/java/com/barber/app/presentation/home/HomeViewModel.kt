package com.barber.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.barber.app.core.common.Resource
import com.barber.app.core.datastore.UserPreferences
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.domain.model.Booking
import com.barber.app.domain.usecase.GetClientBookingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeState(
    val userName: String = "",
    val upcomingBookings: List<Booking> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val getClientBookingsUseCase: GetClientBookingsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    init {
        loadData()
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val prefs = userPreferencesRepository.userPreferences.first()
            _state.value = _state.value.copy(userName = prefs.nombres)

            if (prefs.clientId > 0) {
                when (val result = getClientBookingsUseCase(prefs.clientId)) {
                    is Resource.Success -> {
                        val upcoming = result.data.filter {
                            it.status.uppercase() in listOf("PENDING", "CONFIRMED")
                        }
                        _state.value = _state.value.copy(
                            upcomingBookings = upcoming,
                            isLoading = false,
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
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }
}
