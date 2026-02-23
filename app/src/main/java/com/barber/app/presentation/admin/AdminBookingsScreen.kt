package com.barber.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barber.app.domain.model.AdminBarber
import com.barber.app.domain.model.AdminBooking
import com.barber.app.domain.model.AdminClient
import com.barber.app.domain.model.Service
import com.barber.app.presentation.components.ErrorOverlay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val statusOptions = listOf(
    null to "Todos",
    "PENDING" to "Pendiente",
    "CONFIRMED" to "Confirmada",
    "IN_PROGRESS" to "En Progreso",
    "COMPLETED" to "Completada",
    "CANCELLED" to "Cancelada",
)

private val nextStatusOptions: Map<String, List<Pair<String, String>>> = mapOf(
    "PENDING"     to listOf("CONFIRMED" to "Confirmar", "CANCELLED" to "Cancelar"),
    "CONFIRMED"   to listOf("IN_PROGRESS" to "Iniciar", "CANCELLED" to "Cancelar"),
    "IN_PROGRESS" to listOf("COMPLETED" to "Completar"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminBookingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var changeStatusTarget by remember { mutableStateOf<Pair<AdminBooking, String>?>(null) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) viewModel.clearSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Reservas") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        /** FAB para abrir el diálogo de creación de reserva */
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Crear reserva")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filtros de estado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                statusOptions.forEach { (status, label) ->
                    FilterChip(
                        selected = state.statusFilter == status,
                        onClick = { viewModel.setFilter(status) },
                        label = { Text(label) },
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isLoading && state.bookings.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.bookings.isEmpty()) {
                    Text(
                        "No hay reservas",
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        item { Spacer(modifier = Modifier.height(4.dp)) }
                        items(state.bookings) { booking ->
                            AdminBookingCard(
                                booking = booking,
                                onChangeStatus = { newStatus ->
                                    changeStatusTarget = booking to newStatus
                                },
                            )
                        }
                        item { Spacer(modifier = Modifier.height(8.dp)) }
                    }
                }

                ErrorOverlay(
                    message = state.error ?: "",
                    visible = state.error != null,
                    onDismiss = viewModel::clearError,
                )
            }
        }
    }

    changeStatusTarget?.let { (booking, newStatus) ->
        val label = statusOptions.firstOrNull { it.first == newStatus }?.second ?: newStatus
        AlertDialog(
            onDismissRequest = { changeStatusTarget = null },
            containerColor = Color.White,
            title = { Text("Cambiar estado", color = Color.Black) },
            text = {
                Text(
                    "¿Cambiar la reserva del ${booking.fechaReserva} a estado \"$label\"?",
                    color = Color.Black,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.changeStatus(booking.id, newStatus)
                    changeStatusTarget = null
                }) { Text("Confirmar", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { changeStatusTarget = null }) { Text("Cancelar", color = Color.Black) }
            },
        )
    }

    /** Diálogo de creación de reserva: visible cuando showCreateDialog == true */
    if (state.showCreateDialog) {
        CreateBookingDialog(
            clients  = state.clients,
            barbers  = state.barbers,
            services = state.services,
            onDismiss = { viewModel.dismissCreateDialog() },
            onCreate  = { clientId, barberId, fecha, hora, serviceIds ->
                // Normaliza hora HH:mm → HH:mm:ss antes de enviar
                val startTime = if (hora.length == 5) "$hora:00" else hora
                viewModel.createBooking(clientId, barberId, fecha, startTime, serviceIds)
            },
        )
    }
}

/**
 * Diálogo para crear una reserva nueva.
 * Selectores de cliente y barbero con ExposedDropdownMenu.
 * Fecha mediante DatePickerDialog (calendario blanco) — hora como TextField.
 * Servicios con checkboxes.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateBookingDialog(
    clients: List<AdminClient>,
    barbers: List<AdminBarber>,
    services: List<Service>,
    onDismiss: () -> Unit,
    onCreate: (clientId: Long, barberId: Long, fecha: String, hora: String, serviceIds: List<Long>) -> Unit,
) {
    var selectedClient   by remember { mutableStateOf<AdminClient?>(null) }
    var selectedBarber   by remember { mutableStateOf<AdminBarber?>(null) }
    var fecha            by remember { mutableStateOf("") }
    var hora             by remember { mutableStateOf("") }
    val selectedServices = remember { mutableStateListOf<Long>() }

    var clientExpanded   by remember { mutableStateOf(false) }
    var barberExpanded   by remember { mutableStateOf(false) }
    var showFechaPicker  by remember { mutableStateOf(false) }
    val fechaPickerState = rememberDatePickerState()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val canCreate = selectedClient != null && selectedBarber != null &&
        fecha.isNotBlank() && hora.isNotBlank() && selectedServices.isNotEmpty()

    // DatePickerDialog para la fecha de la reserva — calendario blanco absoluto
    if (showFechaPicker) {
        DatePickerDialog(
            onDismissRequest = { showFechaPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaPickerState.selectedDateMillis?.let { millis ->
                        fecha = sdf.format(Date(millis))
                    }
                    showFechaPicker = false
                }) { Text("Aceptar", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showFechaPicker = false }) { Text("Cancelar", color = Color.Black) }
            },
        ) {
            DatePicker(
                state = fechaPickerState,
                showModeToggle = false,
                title = null,
                headline = null,
                colors = DatePickerDefaults.colors(containerColor = Color.White),
            )
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Nueva Reserva", color = Color.Black) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Selector cliente
                item {
                    ExposedDropdownMenuBox(
                        expanded = clientExpanded,
                        onExpandedChange = { clientExpanded = !clientExpanded },
                    ) {
                        OutlinedTextField(
                            value = selectedClient?.nombres ?: "Seleccionar cliente",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Cliente") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(clientExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(expanded = clientExpanded, onDismissRequest = { clientExpanded = false }) {
                            clients.forEach { c ->
                                DropdownMenuItem(
                                    text = { Text(c.nombres) },
                                    onClick = { selectedClient = c; clientExpanded = false },
                                )
                            }
                        }
                    }
                }

                // Selector barbero
                item {
                    ExposedDropdownMenuBox(
                        expanded = barberExpanded,
                        onExpandedChange = { barberExpanded = !barberExpanded },
                    ) {
                        OutlinedTextField(
                            value = selectedBarber?.nombres ?: "Seleccionar barbero",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Barbero") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(barberExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(expanded = barberExpanded, onDismissRequest = { barberExpanded = false }) {
                            barbers.filter { it.active }.forEach { b ->
                                DropdownMenuItem(
                                    text = { Text(b.nombres) },
                                    onClick = { selectedBarber = b; barberExpanded = false },
                                )
                            }
                        }
                    }
                }

                // Fecha — selección mediante DatePickerDialog
                item {
                    Box(modifier = Modifier.fillMaxWidth().clickable { showFechaPicker = true }) {
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Fecha*") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray,
                            ),
                        )
                    }
                }
                item {
                    OutlinedTextField(value = hora, onValueChange = { hora = it }, label = { Text("Hora (HH:mm)*") }, modifier = Modifier.fillMaxWidth())
                }

                // Servicios: checkboxes
                if (services.isNotEmpty()) {
                    item { Text("Servicios*:", style = MaterialTheme.typography.labelMedium, color = Color.Black) }
                    items(services.size) { i ->
                        val svc = services[i]
                        val checked = svc.id in selectedServices
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .toggleable(value = checked, onValueChange = {
                                    if (it) selectedServices.add(svc.id) else selectedServices.remove(svc.id)
                                }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Checkbox(checked = checked, onCheckedChange = null)
                            Text("${svc.name}  S/ ${"%.2f".format(svc.price)}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(selectedClient!!.codigoCliente, selectedBarber!!.codigoBarbero, fecha.trim(), hora.trim(), selectedServices.toList()) },
                enabled = canCreate,
            ) { Text("Crear", color = if (canCreate) Color.Black else Color.Gray) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Black) }
        },
    )
}

@Composable
private fun AdminBookingCard(
    booking: AdminBooking,
    onChangeStatus: (String) -> Unit,
) {
    val statusColor = when (booking.status.uppercase()) {
        "PENDING"     -> Color(0xFFFF9800)
        "CONFIRMED"   -> Color(0xFF4CAF50)
        "IN_PROGRESS" -> Color(0xFF7B1FA2)
        "CANCELLED"   -> Color(0xFFE53935)
        "COMPLETED"   -> Color(0xFF1565C0)
        else          -> Color(0xFF9E9E9E)
    }
    val statusLabel = when (booking.status.uppercase()) {
        "PENDING"     -> "Pendiente"
        "CONFIRMED"   -> "Confirmada"
        "IN_PROGRESS" -> "En Progreso"
        "CANCELLED"   -> "Cancelada"
        "COMPLETED"   -> "Completada"
        else          -> booking.status
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(booking.fechaReserva, style = MaterialTheme.typography.titleMedium)
                StatusChip(label = statusLabel, color = statusColor)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Cliente: ${booking.clientName}", style = MaterialTheme.typography.bodySmall)
            Text("Barbero: ${booking.barberName}", style = MaterialTheme.typography.bodySmall)
            if (booking.startTime.isNotBlank()) {
                Text("Hora: ${booking.startTime}", style = MaterialTheme.typography.bodySmall)
            }
            if (booking.services.isNotEmpty()) {
                val total = booking.services.sumOf { it.price.toDouble() }
                Text("Total: S/ ${"%.2f".format(total)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }

            val actions = nextStatusOptions[booking.status.uppercase()]
            if (!actions.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    actions.forEach { (status, label) ->
                        val btnColor = when (status) {
                            "CONFIRMED"   -> Color(0xFF4CAF50)
                            "IN_PROGRESS" -> Color(0xFF7B1FA2)
                            "COMPLETED"   -> Color(0xFF1565C0)
                            "CANCELLED"   -> Color(0xFFE53935)
                            else          -> MaterialTheme.colorScheme.primary
                        }
                        Button(
                            onClick = { onChangeStatus(status) },
                            colors = ButtonDefaults.buttonColors(containerColor = btnColor),
                            modifier = Modifier.height(32.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp),
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color)
            .padding(horizontal = 10.dp, vertical = 3.dp),
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
    }
}
