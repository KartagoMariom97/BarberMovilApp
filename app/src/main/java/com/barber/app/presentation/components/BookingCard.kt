package com.barber.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.ui.window.Dialog

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
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Barbero:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BookingServiceChip(name = booking.barberName)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Hora:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BookingServiceChip(name = formatTimeDisplay(booking.startTime))
            }
            if (booking.services.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Servicios:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    booking.services.forEach { svc ->
                        BookingServiceChip(name = svc.name)
                    }
                }
                val total = booking.services.sumOf { it.price.toDouble() }
                Text(
                    "Total: S/ ${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
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

/**
 * Chip de servicio en las tarjetas de reserva.
 * Para cambiar el COLOR: edita `containerColor` (fondo) y `textColor` (texto) aquí abajo.
 */
@Composable
private fun BookingServiceChip(name: String) {
    val containerColor = Color.White          // ← FONDO del chip (prueba con Color(0xFF...) para otro color)
    val textColor      = Color.Black          // ← COLOR DE LETRA del chip
    val borderColor    = Color.Black.copy(alpha = 0.22f)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
        )
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
        Text("Servicios:", style = MaterialTheme.typography.labelMedium, color = darkText)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            booking.services.forEach { svc ->
                BookingServiceChip(name = svc.name)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        val total = booking.services.sumOf { it.price.toDouble() }
        Text("Total: S/ ${"%.2f".format(total)}", style = MaterialTheme.typography.titleSmall, color = Color.Black)
    }
}
