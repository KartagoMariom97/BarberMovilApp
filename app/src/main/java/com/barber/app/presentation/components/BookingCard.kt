package com.barber.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.barber.app.domain.model.Booking
import com.barber.app.domain.model.Barber
import com.barber.app.domain.model.Service

import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button

import com.barber.app.presentation.components.EditBookingDialog

private fun formatTimeDisplay(time: String): String {
    if (time.isBlank()) return time
    val parts = time.split(":")
    if (parts.size < 2) return time
    val hour = parts[0].toIntOrNull() ?: return time
    val minute = parts[1]
    val amPm = if (hour < 12) "AM" else "PM"
    val displayHour = when {
        hour == 0 -> 12
        hour > 12 -> hour - 12
        else -> hour
    }
    return "$displayHour:$minute $amPm"
}

private val GrayButton = Color(0xFF9E9E9E)
private val YellowButton = Color(0xFFFFC107)
private val RedButton = Color(0xFFE53935)
private val PendingOrange = Color(0xFFFF9800)

@Composable
fun BookingCard(
    booking: Booking,
    barbers: List<Barber>,
    services: List<Service>,
    clientId: Long,
    showActions: Boolean = true, // 👈 NUEVO
    onUpdateBooking: (
        clientId: Long,
        barberId: Long,
        fecha: String,
        hora: String,
        serviceIds: List<Long>
    ) -> Unit,
    onCancel: (() -> Unit)? = null,
    onShowDetail: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }

    if (showConfirmDialog && onCancel != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = Color.White,
            title = { Text("Cancelar reserva", color = Color.Black) },
            text = {
                Text(
                    "¿Estás seguro de que deseas cancelar esta reserva del ${booking.fechaReserva} a las ${formatTimeDisplay(booking.startTime)}?",
                    color = Color.Black,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmDialog = false
                    onCancel()
                }) {
                    Text("Sí, cancelar", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("No, mantener", color = Color.Black)
                }
            },
        )
    }

    if (showEditDialog) {
        EditBookingDialog(
            booking = booking,
            barbers = barbers,
            services = services,
            clientId = clientId,
            onDismiss = { showEditDialog = false },
            onSave = { clientId, barberId, fecha, hora, serviceIds ->
                onUpdateBooking(
                    clientId,
                    barberId,
                    fecha,
                    hora,
                    serviceIds
                )
            }
        )
    }

    val statusColor = when (booking.status.uppercase()) {
        "PENDING" -> PendingOrange
        "CONFIRMED" -> MaterialTheme.colorScheme.secondary
        "CANCELLED" -> MaterialTheme.colorScheme.error
        "COMPLETED" -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    val isActive = booking.status.uppercase() in listOf("PENDING", "CONFIRMED")

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(booking.fechaReserva, style = MaterialTheme.typography.titleLarge)
                when (booking.status.uppercase()) {
                    "PENDING" -> {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = "Pendiente",
                            tint = PendingOrange,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                    else -> {
                        val statusText = when (booking.status.uppercase()) {
                            "CONFIRMED" -> "Confirmada"
                            "CANCELLED" -> "Cancelada"
                            "COMPLETED" -> "Completada"
                            else -> booking.status
                        }
                        Text(statusText, style = MaterialTheme.typography.bodySmall, color = statusColor)
                    }
                }
            }
            Text("Barbero: ${booking.barberName}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 2.dp))
            Text("Hora: ${formatTimeDisplay(booking.startTime)}", style = MaterialTheme.typography.bodyMedium)
            if (booking.services.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Servicios:",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 2.dp),
                )
                booking.services.forEach { svc ->
                    Text(
                        "  - ${svc.name} (${svc.minutes} min) - S/ ${svc.price}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                val total = booking.services.sumOf { it.price.toDouble() }
                Text(
                    "Total: S/ ${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { onShowDetail?.invoke() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Info, "Detalles", tint = GrayButton, modifier = Modifier.size(20.dp))
                }
                // 👇 SOLO SI showActions ES TRUE
                if (showActions && isActive) {
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = YellowButton,
                        modifier = Modifier.size(20.dp)
                        )
                    }
                }
                if (showActions && isActive && onCancel != null) {
                    IconButton(onClick = { showConfirmDialog = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Cancel, "Cancelar", tint = RedButton, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BookingDetailContent(booking: Booking) {
    val darkText = Color.Black.copy(alpha = 0.8f)
    Text("Fecha: ${booking.fechaReserva}", style = MaterialTheme.typography.bodyMedium, color = darkText)
    Text("Hora: ${formatTimeDisplay(booking.startTime)}", style = MaterialTheme.typography.bodyMedium, color = darkText)
    if (!booking.endTime.isNullOrBlank()) {
        Text("Hora fin: ${formatTimeDisplay(booking.endTime)}", style = MaterialTheme.typography.bodyMedium, color = darkText)
    }
    Text("Barbero: ${booking.barberName}", style = MaterialTheme.typography.bodyMedium, color = darkText)
    Text("Estado: ${
        when (booking.status.uppercase()) {
            "PENDING" -> "Pendiente"; "CONFIRMED" -> "Confirmada"
            "CANCELLED" -> "Cancelada"; "COMPLETED" -> "Completada"
            else -> booking.status
        }
    }", style = MaterialTheme.typography.bodyMedium, color = darkText)
    if (booking.services.isNotEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text("Servicios:", style = MaterialTheme.typography.titleSmall, color = Color.Black)
        booking.services.forEach { svc ->
            Text("  - ${svc.name} (${svc.minutes} min) - S/ ${svc.price}", style = MaterialTheme.typography.bodySmall, color = darkText)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val total = booking.services.sumOf { it.price.toDouble() }
        Text("Total: S/ ${"%.2f".format(total)}", style = MaterialTheme.typography.titleSmall, color = Color.Black)
    }
}
