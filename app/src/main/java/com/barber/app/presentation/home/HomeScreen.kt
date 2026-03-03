package com.barber.app.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Divider
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
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

import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@Composable
fun HomeScreen(
    onNavigateToBooking: () -> Unit,
    onNavigateToAppointments: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }

    // 🔥🔥🔥 CAMBIO PRINCIPAL
    // Este estado controla el indicador circular de SwipeRefresh
    val swipeRefreshState = rememberSwipeRefreshState(
        isRefreshing = state.isLoading
    )

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadData()
    }

    // [FIX-4] Solo un dialog a la vez: newBookingDialog tiene prioridad sobre confirmedDialog.
    // Los condicionales son mutuamente excluyentes (if / else if).
    // Dialog: notifica al cliente que el admin creó una reserva para él
    if (state.showNewBookingDialog && state.newAdminBookings.isNotEmpty()) {
        val booking = state.newAdminBookings.first()
        val statusLabel = when (booking.status.uppercase()) {
            "PENDING"    -> "Pendiente"
            "CONFIRMED"  -> "Confirmada"
            "IN_PROGRESS"-> "En progreso"
            "COMPLETED"  -> "Completada"
            "CANCELLED"  -> "Cancelada"
            else         -> booking.status
        }
        AlertDialog(
            onDismissRequest = { viewModel.dismissNewBookingDialog() },
            containerColor = Color.White,
            title = { Text("Nueva reserva del administrador", color = Color.Black) },
            text = {
                Text(
                    "El administrador ha creado una reserva para ti.\n\n" +
                    "Fecha: ${booking.fechaReserva}\n" +
                    "Hora: ${booking.startTime}\n" +
                    "Barbero: ${booking.barberName}\n" +
                    "Estado: $statusLabel",
                    color = Color.Black,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissNewBookingDialog() }) {
                    Text("Aceptar", color = Color.Black)
                }
            },
        )
    // [FIX-4] else if: el confirmedDialog solo se muestra si newBookingDialog no está activo
    } else if (state.showConfirmedDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissConfirmedDialog() },
            containerColor = Color.White,
            title = { Text("¡Reserva Confirmada!", color = Color.Black) },
            text = {
                Text(
                    "Se confirmó tu reserva. Tienes ${state.confirmedCount} reserva(s) confirmada(s).",
                    color = Color.Black,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissConfirmedDialog() }) {
                    Text("Aceptar", color = Color.Black)
                }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Encabezado + botón fijos (siempre visibles) ─────────────
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "Hola, ${state.userName}",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Bienvenido a tu barbería favorita",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
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

            Divider()

            // ── Lista de próximas citas (scrolleable) ────────────────────

            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                SwipeRefresh(
                    state = swipeRefreshState,
                    onRefresh = {
                        viewModel.loadData() // Se ejecuta al hacer swipe
                    }
                ){
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
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
                                showActions = false,
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
