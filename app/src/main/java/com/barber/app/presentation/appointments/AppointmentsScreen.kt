package com.barber.app.presentation.appointments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barber.app.domain.model.Booking
import com.barber.app.presentation.components.BookingCard
import com.barber.app.presentation.components.BookingDetailContent
import com.barber.app.presentation.components.DetailOverlay
import com.barber.app.presentation.components.ErrorMessage
import com.barber.app.presentation.components.ErrorOverlay
import com.barber.app.presentation.components.LoadingIndicator

@Composable
fun AppointmentsScreen(
    onNavigateToBooking: (() -> Unit)? = null,
    viewModel: AppointmentsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadBookings()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            !state.isLoading && state.bookings.isEmpty() && state.error == null -> {
                ErrorMessage(message = "No tienes citas registradas")
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.bookings, key = { it.id }) { booking ->
                        BookingCard(
                            booking = booking,
                            onCancel = { viewModel.cancelBooking(booking.id) },
                            onEdit = onNavigateToBooking,
                            onShowDetail = { selectedBooking = booking },
                        )
                    }
                }
            }
        }

        if (state.isLoading) {
            LoadingIndicator()
        }

        ErrorOverlay(
            message = state.error ?: "",
            visible = state.error != null,
            onDismiss = viewModel::clearError,
        )

        selectedBooking?.let { booking ->
            DetailOverlay(
                title = "Detalles de la Reserva",
                visible = true,
                onDismiss = { selectedBooking = null },
            ) {
                BookingDetailContent(booking)
            }
        }
    }
}
