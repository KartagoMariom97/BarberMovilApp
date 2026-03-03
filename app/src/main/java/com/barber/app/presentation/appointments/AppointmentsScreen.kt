package com.barber.app.presentation.appointments

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

// 🔥 NUEVOS IMPORTS PARA PULL TO REFRESH
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

// [FIX-FILTERS] Necesario para scroll horizontal de chips cuando hay 5 opciones
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog

import androidx.compose.foundation.background
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults

// [FIX-FILTERS] Chips actualizados:
// TODAS → solo activas (PENDING/CONFIRMED/MODIFIED_PENDING/IN_PROGRESS)
// Canceladas y Completadas tienen chip propio
private val FILTER_OPTIONS = listOf(
    "TODAS"     to "Todas",
    "PENDING"   to "Pendientes",
    "CONFIRMED" to "Confirmadas",
    "CANCELLED" to "Canceladas",
    "COMPLETED" to "Completadas",
)

@Composable
fun AppointmentsScreen(
    onNavigateToBooking: (() -> Unit)? = null,
    viewModel: AppointmentsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedBooking by remember { mutableStateOf<Booking?>(null) }
    // Filtro activo — "TODAS" muestra todo excepto COMPLETED
    var activeFilter by remember { mutableStateOf("TODAS") }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadBookings(isRefresh = true)
    }

    // [FIX-FILTERS] Lógica de filtros centralizada en la capa de presentación
    // TODAS: solo muestra reservas activas (no CANCELLED, no COMPLETED)
    // PENDING: agrupa PENDING y MODIFIED_PENDING
    // CANCELLED / COMPLETED: exclusivos de su chip
    val filteredBookings = state.bookings.filter { booking ->
        val status = booking.status.uppercase()
        when (activeFilter) {
            "TODAS"     -> status in listOf("PENDING", "CONFIRMED", "MODIFIED_PENDING", "IN_PROGRESS")
            "PENDING"   -> status == "PENDING" || status == "MODIFIED_PENDING"
            "CONFIRMED" -> status == "CONFIRMED"
            "CANCELLED" -> status == "CANCELLED"
            "COMPLETED" -> status == "COMPLETED"
            else        -> true
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // [FIX-FILTERS] horizontalScroll para acomodar 5 chips sin overflow
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FILTER_OPTIONS.forEach { (status, label) ->
                    FilterChip(
                        selected = activeFilter == status,
                        onClick = { activeFilter = status },
                        label = { Text(label) },
                    )
                }
            }

            // Contenido: lista vacía o lista filtrada
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when {
                    !state.isLoading && filteredBookings.isEmpty() && state.error == null -> {
                        // Mensaje diferenciado: sin citas vs sin citas con ese filtro
                        ErrorMessage(
                            message = when (activeFilter) {
                                "TODAS"     -> "No tienes citas activas"
                                "CANCELLED" -> "No tienes citas canceladas"
                                "COMPLETED" -> "No tienes citas completadas"
                                else        -> "No hay citas con este filtro"
                            },
                        )
                    }
                    else -> {
                        SwipeRefresh(
                            state = swipeRefreshState,
                            onRefresh = { viewModel.loadBookings(isRefresh = true) },
                        ) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                            ) {
                                items(filteredBookings, key = { it.id }) { booking ->
                                    BookingCard(
                                        booking = booking,
                                        barbers = state.barbers,
                                        services = state.services,
                                        clientId = state.clientId,
                                        onUpdateBooking = { clientId, barberId, fecha, hora, serviceIds ->
                                            viewModel.updateBooking(
                                                booking.id,
                                                clientId,
                                                barberId,
                                                fecha,
                                                hora,
                                                serviceIds,
                                            )
                                        },
                                        onCancel = { viewModel.cancelBooking(booking.id) },
                                        onShowDetail = { selectedBooking = booking },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // [FIX] Solo bloquea pantalla en carga inicial; el refresh usa SwipeRefresh visual
        if (state.isLoading && !state.isRefreshing) {
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

        // [FIX-4] Solo un dialog visible a la vez; updateSuccess tiene prioridad.
        // Ninguno se cierra automáticamente: solo el usuario puede hacerlo con Aceptar.
        when {
            state.updateSuccess -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AlertDialog(
                        onDismissRequest = { viewModel.clearUpdateSuccess() },
                        containerColor = Color.White,
                        title = { Text("Reserva actualizada", color = Color.Black) },
                        text = { Text("La reserva fue actualizada correctamente.", color = Color.Black) },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.clearUpdateSuccess() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White,
                                ),
                            ) { Text("Aceptar") }
                        },
                    )
                }
            }
            // [FIX-3] Dialog cuando el admin editó la reserva del cliente
            state.showAdminUpdatedDialog -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center,
                ) {
                    AlertDialog(
                        onDismissRequest = { viewModel.clearAdminUpdatedDialog() },
                        containerColor = Color.White,
                        title = { Text("Reserva modificada", color = Color.Black) },
                        text = {
                            Text(
                                "Tu reserva ha sido actualizada por el administrador.",
                                color = Color.Black,
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = { viewModel.clearAdminUpdatedDialog() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White,
                                ),
                            ) { Text("Aceptar") }
                        },
                    )
                }
            }
        }
    }
}
