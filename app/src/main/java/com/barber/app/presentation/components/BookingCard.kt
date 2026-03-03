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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.text.font.FontWeight
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

// [FIX-VISUAL] Estado derivado: CONFIRMED + modificationUsed = aprobado por admin, no editable
// Centralizado aquí para que BookingCard y BookingDetailContent lo usen sin duplicar lógica
private val Booking.isModifiedConfirmed: Boolean
    get() = status.uppercase() == "CONFIRMED" && modificationUsed

private val YellowButton = Color(0xFFFFC107)
private val RedButton    = Color(0xFFE53935)
private val PendingOrange = Color(0xFFFF9800)

@Composable
fun BookingCard(
    booking: Booking,
    barbers: List<Barber>,
    services: List<Service>,
    clientId: Long,
    showActions: Boolean = true,
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
    var showEditDialog    by remember { mutableStateOf(false) }

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
                }) { Text("Sí, cancelar", color = Color.Black) }
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
            onSave = { cId, barberId, fecha, hora, serviceIds ->
                onUpdateBooking(cId, barberId, fecha, hora, serviceIds)
            },
        )
    }

    // [FIX-VISUAL] Evaluar estado derivado primero para override de color
    val isModifiedConfirmed = booking.isModifiedConfirmed
    val statusColor = when {
        isModifiedConfirmed                                  -> Color(0xFF1565C0) // azul oscuro: mod. aprobada
        booking.status.uppercase() == "PENDING"             -> PendingOrange
        booking.status.uppercase() == "CONFIRMED"           -> Color(0xFF4CAF50)
        booking.status.uppercase() == "MODIFIED_PENDING"    -> Color(0xFF1976D2)
        booking.status.uppercase() == "IN_PROGRESS"         -> Color(0xFF7B1FA2)
        booking.status.uppercase() == "CANCELLED"           -> MaterialTheme.colorScheme.error
        booking.status.uppercase() == "COMPLETED"           -> MaterialTheme.colorScheme.primary
        else                                                -> MaterialTheme.colorScheme.onSurface
    }

    // MODIFIED_PENDING excluido: sin iconos mientras el admin revisa la modificación del cliente
    val isActive = booking.status.uppercase() in listOf("PENDING", "CONFIRMED")
    // [FIX-1] Regla correcta: PENDING o CONFIRMED + sin modificación previa
    val canEdit = booking.status.uppercase() in listOf("PENDING", "CONFIRMED") && !booking.modificationUsed

    Card(
        // ← CAMBIAR COLOR: fondo de la card (containerColor)
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), // ← CAMBIAR: sombra de la card
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(booking.fechaReserva, style = MaterialTheme.typography.titleLarge)
                // Ícono semántico por cada estado (antes: solo PENDING tenía ícono)
                val statusIcon = when (booking.status.uppercase()) {
                    "PENDING"          -> Icons.Default.Schedule
                    "CONFIRMED"        -> Icons.Default.CheckCircle
                    "MODIFIED_PENDING" -> Icons.Default.Info   // esperando revisión del admin
                    "IN_PROGRESS"      -> Icons.Default.PlayArrow
                    "CANCELLED"        -> Icons.Default.Cancel
                    "COMPLETED"        -> Icons.Default.Done
                    else               -> Icons.Default.Schedule
                }
                Icon(
                    statusIcon,
                    contentDescription = booking.status,
                    tint = statusColor,
                    modifier = Modifier.size(22.dp),
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text("Barbero:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BookingServiceChip(name = booking.barberName)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    booking.services.forEach { svc ->
                        BookingServiceChip(name = svc.name)
                    }
                }
                val total = booking.services.sumOf { it.price.toDouble() }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Total: S/ ${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Indicador visual: solo en estados activos donde el usuario podría intentar editar
                // Oculto en MODIFIED_PENDING, MODIFIED_CONFIRMED, CANCELLED y COMPLETED
                val showModifiedIndicator = booking.modificationUsed
                    && booking.status.uppercase() !in listOf("MODIFIED_PENDING", "CANCELLED", "COMPLETED")
                    && !isModifiedConfirmed
                if (showModifiedIndicator) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Reserva ya modificada",
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp),
                    )
                }
                // [CAMBIO] Icono de detalles cambiado a negro absoluto (antes era gris 0xFF9E9E9E)
                IconButton(onClick = { onShowDetail?.invoke() }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Info, "Detalles", tint = Color.Black, modifier = Modifier.size(20.dp))
                }
                // Editar: solo si canEdit (no modificó antes y no fue rechazada la modificación)
                if (showActions && canEdit) {
                    IconButton(onClick = { showEditDialog = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, "Editar", tint = YellowButton, modifier = Modifier.size(20.dp))
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
 * Chip genérico para servicios y datos de reserva.
 * [CAMBIO] borde ahora es negro absoluto (antes tenía alpha 0.22f).
 */
@Composable
private fun BookingServiceChip(name: String) {
    val containerColor = Color.White  // ← CAMBIAR COLOR: fondo del chip
    val textColor      = Color.Black  // ← CAMBIAR COLOR: texto del chip
    // [CAMBIO] borderColor cambiado a negro absoluto (sin alpha) para mayor visibilidad
    val borderColor    = Color.Black

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
        )
    }
}

/**
 * Chip de estado con color semántico por estado de la reserva.
 * [CAMBIO] nuevo composable — convierte el estado de texto plano a chip coloreado.
 *   PENDING   → fondo amarillo absoluto  (#FFC107), texto blanco
 *   CONFIRMED → fondo verde  (#4CAF50), texto blanco
 *   CANCELLED → fondo rojo   (#E53935), texto blanco
 *   COMPLETED → fondo azul   (#1565C0), texto blanco
 */
@Composable
private fun BookingStatusChip(status: String) {
    // [CAMBIO] cada estado tiene su color semántico propio, letras blancas para contraste
    val (bgColor, label) = when (status.uppercase()) {
        "PENDING"            -> Color(0xFFFFC107) to "Pendiente"
        "CONFIRMED"          -> Color(0xFF4CAF50) to "Confirmada"
        "MODIFIED_PENDING"   -> Color(0xFF1976D2) to "Modificación Pendiente"
        "IN_PROGRESS"        -> Color(0xFF7B1FA2) to "En Progreso"
        "CANCELLED"          -> Color(0xFFE53935) to "Cancelada"
        "COMPLETED"          -> Color(0xFF1565C0) to "Completada"
        // [FIX-VISUAL] Estado visual derivado: modificación aprobada por admin
        "MODIFIED_CONFIRMED" -> Color(0xFF1565C0) to "Modificado y Confirmado"
        else                 -> Color(0xFF9E9E9E) to status
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            // [CAMBIO] borde del chip de estado usa el mismo color semántico (sin borde visible)
            .border(1.dp, bgColor, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            // [CAMBIO] texto blanco para contraste sobre fondo de color
            color = Color.White,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
fun BookingDetailContent(booking: Booking) {
    // [CAMBIO] darkText ahora es negro absoluto (antes era Color.Black.copy(alpha = 0.8f))
    val darkText = Color.Black

    // Fecha
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // [CAMBIO] etiqueta "Fecha:" ahora usa bodyMedium (era bodySmall) y negro absoluto
        Text("Fecha:", style = MaterialTheme.typography.bodyMedium, color = darkText, fontWeight = FontWeight.Bold)
        BookingServiceChip(name = booking.fechaReserva)
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Hora inicio
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // [CAMBIO] etiqueta "Hora:" bodyMedium + negro absoluto
        Text("Hora:", style = MaterialTheme.typography.bodyMedium, color = darkText, fontWeight = FontWeight.Bold)
        BookingServiceChip(name = formatTimeDisplay(booking.startTime))
    }

    // Hora fin (opcional)
    if (!booking.endTime.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            // [CAMBIO] etiqueta "Hora fin:" bodyMedium + negro absoluto
            Text("Hora fin:", style = MaterialTheme.typography.bodyMedium, color = darkText, fontWeight = FontWeight.Bold)
            BookingServiceChip(name = formatTimeDisplay(booking.endTime))
        }
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Barbero
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // [CAMBIO] etiqueta "Barbero:" bodyMedium + negro absoluto
        Text("Barbero:", style = MaterialTheme.typography.bodyMedium, color = darkText, fontWeight = FontWeight.Bold)
        BookingServiceChip(name = booking.barberName)
    }

    Spacer(modifier = Modifier.height(6.dp))

    // Estado — [CAMBIO] reemplazado de texto plano a BookingStatusChip con color semántico
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Estado:", style = MaterialTheme.typography.bodyMedium, color = darkText, fontWeight = FontWeight.Bold)
        // [CAMBIO] chip de estado con color propio por tipo (amarillo/verde/rojo/azul)
        // [FIX-VISUAL] Usar estado derivado centralizado para chip de detalles
        BookingStatusChip(status = if (booking.isModifiedConfirmed) "MODIFIED_CONFIRMED" else booking.status)
    }

    // Servicios
    if (booking.services.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        // [CAMBIO] etiqueta "Servicios:" bodyMedium + negro absoluto
        Text("Servicios:", style = MaterialTheme.typography.bodyMedium, color = darkText, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            booking.services.forEach { svc ->
                BookingServiceChip(name = svc.name)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val total = booking.services.sumOf { it.price.toDouble() }
        // [CAMBIO] texto del total negro absoluto y bodyLarge para mayor legibilidad
        Text(
            "Total: S/ ${"%.2f".format(total)}",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Black,
            fontWeight = FontWeight.Bold,
        )
    }
}
