package com.barber.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barber.app.domain.model.Booking
import com.barber.app.presentation.components.BookingCard
import com.barber.app.presentation.components.BookingDetailContent
import com.barber.app.presentation.components.DetailOverlay
import com.barber.app.presentation.components.ErrorOverlay
import com.barber.app.presentation.components.LoadingIndicator

import com.barber.app.domain.model.Barber
import com.barber.app.domain.model.Service

@Composable
fun HomeScreen(
    onNavigateToBooking: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Hola, ${state.userName}",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bienvenido a tu barbería favorita",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            item {
                Button(
                    onClick = onNavigateToBooking,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                ) {
                    Icon(Icons.Default.ContentCut, contentDescription = null)
                    Text(
                        text = "  Reservar Cita",
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }

            if (state.upcomingBookings.isNotEmpty()) {
                item {
                    Text(
                        text = "Próximas citas",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                items(state.upcomingBookings) { booking ->
                    BookingCard(
                        booking = booking,
                        barbers = emptyList(),
                        services = emptyList(),
                        clientId = 0L,
                        showActions = false, // 👈 ESTO ES CLAVE
                        onUpdateBooking = { _, _, _, _, _ -> },
                        onCancel = null,
                        onShowDetail = { selectedBooking = booking }
                    )
                }
            } else {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = "No tienes citas próximas",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
