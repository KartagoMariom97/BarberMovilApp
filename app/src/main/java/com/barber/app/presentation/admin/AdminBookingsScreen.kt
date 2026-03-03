package com.barber.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barber.app.domain.model.AdminBarber
import com.barber.app.domain.model.AdminBooking
import com.barber.app.domain.model.AdminClient
import com.barber.app.domain.model.Service
import com.barber.app.presentation.components.ErrorOverlay
import com.barber.app.presentation.components.LoadingIndicator
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// [FIX-2] Imports para conversión correcta UTC → LocalDate en DatePicker del admin
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import androidx.compose.material3.SelectableDates

private val statusOptions = listOf(
    null to "Todos",
    "PENDING" to "Pendiente",
    "CONFIRMED" to "Confirmada",
    "IN_PROGRESS" to "En Progreso",
    "COMPLETED" to "Completada",
    "CANCELLED" to "Cancelada",
)

private val nextStatusOptions: Map<String, List<Pair<String, String>>> = mapOf(
    "PENDING"          to listOf("CONFIRMED" to "Confirmar", "CANCELLED" to "Cancelar"),
    // [FIX-3] MODIFIED_PENDING: el admin solo puede aprobar (→CONFIRMED) o rechazar (→CANCELLED)
    "MODIFIED_PENDING" to listOf("CONFIRMED" to "Aprobar", "CANCELLED" to "Rechazar"),
    "CONFIRMED"        to listOf("IN_PROGRESS" to "Iniciar", "CANCELLED" to "Cancelar"),
    "IN_PROGRESS"      to listOf("COMPLETED" to "Completar"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminBookingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminBookingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var changeStatusTarget by remember { mutableStateOf<Pair<AdminBooking, String>?>(null) }

    if (state.isLoading) LoadingIndicator()

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

            // [FIX-2] Filtrado local: cuando el filtro activo es PENDING, incluye también
            // MODIFIED_PENDING (el backend devuelve todos para ese caso; aquí los acotamos).
            val displayBookings = when (state.statusFilter) {
                "PENDING" -> state.bookings.filter {
                    val s = it.status.uppercase()
                    s == "PENDING" || s == "MODIFIED_PENDING"
                }
                else -> state.bookings
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (!state.isLoading && displayBookings.isEmpty()) {
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
                        // key estable para que Compose detecte cambios por id y recomponga correctamente
                        items(displayBookings, key = { it.id }) { booking ->
                            AdminBookingCard(
                                booking = booking,
                                onChangeStatus = { newStatus ->
                                    changeStatusTarget = booking to newStatus
                                },
                                // Edición solo permitida en PENDING y CONFIRMED; MODIFIED_PENDING no
                                onEdit = if (booking.status.uppercase() in listOf("PENDING", "CONFIRMED")) {
                                    { viewModel.showEditDialog(booking) }
                                } else null,
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

    /** Diálogo de edición de reserva: visible cuando showEditDialog == true */
    if (state.showEditDialog && state.editingBooking != null) {
        EditBookingDialog(
            booking   = state.editingBooking!!,
            barbers   = state.barbers,
            services  = state.services,
            onDismiss = { viewModel.dismissEditDialog() },
            onUpdate  = { barberId, fecha, hora, serviceIds ->
                val startTime = if (hora.length == 5) "$hora:00" else hora
                viewModel.updateBooking(state.editingBooking!!.id, barberId, fecha, startTime, serviceIds)
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

/** Diálogo de edición de una reserva existente (solo PENDING/CONFIRMED) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditBookingDialog(
    booking: AdminBooking,
    barbers: List<AdminBarber>,
    services: List<Service>,
    onDismiss: () -> Unit,
    onUpdate: (barberId: Long, fecha: String, hora: String, serviceIds: List<Long>) -> Unit,
) {
    var selectedBarber by remember {
        mutableStateOf(barbers.firstOrNull { it.codigoBarbero == booking.barberId })
    }
    var fecha by remember { mutableStateOf(booking.fechaReserva) }
    var hora  by remember { mutableStateOf(booking.startTime.take(5)) }  // HH:mm
    var selectedServiceIds by remember {
        mutableStateOf(booking.services.map { it.serviceId }.toSet())
    }

    var barberExpanded  by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }
    var showFechaPicker by remember { mutableStateOf(false) }
    var showTimePicker  by remember { mutableStateOf(false) }

    // [FIX-2] Medianoche UTC de hoy para bloquear selección de fechas pasadas
    val todayUtcMillis = remember {
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val fechaPickerState = rememberDatePickerState(
        // Pre-seleccionar la fecha actual de la reserva
        initialSelectedDateMillis = runCatching {
            LocalDate.parse(booking.fechaReserva).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrNull(),
        // [FIX-2] Solo permitir fechas >= hoy
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtcMillis
        },
    )

    val canUpdate = selectedBarber != null && fecha.isNotBlank() && hora.isNotBlank() && selectedServiceIds.isNotEmpty()

    if (showFechaPicker) {
        DatePickerDialog(
            onDismissRequest = { showFechaPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaPickerState.selectedDateMillis?.let { millis ->
                        // [FIX-2] Convertir con ZoneOffset.UTC (el DatePicker almacena millis en UTC);
                        // usar zona local causaba que se mostrara el día anterior en zonas UTC-N
                        fecha = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
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

    if (showTimePicker) {
        AdminTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour24, minute ->
                hora = "%02d:%02d".format(hour24, minute)
                showTimePicker = false
            },
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = Color.White,
        title = { Text("Editar Reserva #${booking.id}", color = Color.Black) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                // Cliente (solo lectura)
                item {
                    OutlinedTextField(
                        value = booking.clientName,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Cliente") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = Color.Black,
                            disabledBorderColor = Color.Gray,
                            disabledLabelColor = Color.Gray,
                        ),
                    )
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
                            singleLine = true,
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

                // Fecha
                item {
                    Box(modifier = Modifier.fillMaxWidth().clickable { showFechaPicker = true }) {
                        OutlinedTextField(
                            value = fecha,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Fecha*") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray,
                            ),
                        )
                    }
                }

                // Hora
                item {
                    Box(modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true }) {
                        OutlinedTextField(
                            value = hora,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Hora*") },
                            placeholder = { Text("Toca para elegir hora") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray,
                                disabledPlaceholderColor = Color.Gray,
                            ),
                            trailingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = "Elegir hora", tint = Color.Black)
                            },
                        )
                    }
                }

                // Servicios
                if (services.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = when (selectedServiceIds.size) {
                                        0    -> ""
                                        1    -> services.find { it.id == selectedServiceIds.first() }?.name ?: "1 servicio"
                                        else -> "${selectedServiceIds.size} servicios seleccionados"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Servicios*") },
                                    trailingIcon = {
                                        Icon(
                                            if (serviceExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                        )
                                    },
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                        ) { serviceExpanded = !serviceExpanded },
                                )
                            }
                            if (serviceExpanded) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 220.dp)
                                            .verticalScroll(rememberScrollState()),
                                    ) {
                                        services.forEach { svc ->
                                            val isChecked = selectedServiceIds.contains(svc.id)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedServiceIds = if (isChecked) selectedServiceIds - svc.id else selectedServiceIds + svc.id
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Checkbox(checked = isChecked, onCheckedChange = null)
                                                Text("${svc.name}  S/ ${"%.2f".format(svc.price)}", style = MaterialTheme.typography.bodyMedium)
                                            }
                                        }
                                    }
                                }
                            }
                            if (selectedServiceIds.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    services.filter { selectedServiceIds.contains(it.id) }.forEach { svc ->
                                        ServiceChipRemovable(name = svc.name, onRemove = { selectedServiceIds = selectedServiceIds - svc.id })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onUpdate(selectedBarber!!.codigoBarbero, fecha.trim(), hora.trim(), selectedServiceIds.toList())
                },
                enabled = canUpdate,
            ) { Text("Actualizar", color = if (canUpdate) Color.Black else Color.Gray) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Black) }
        },
    )
}

/**
 * Diálogo de creación de reserva.
 * Selectores de cliente y barbero con ExposedDropdownMenu.
 * Servicios con dropdown in-place (mismo patrón que EditBookingDialog) en lugar de checkboxes.
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
    var selectedServiceIds by remember { mutableStateOf(emptySet<Long>()) }

    var clientExpanded   by remember { mutableStateOf(false) }
    var barberExpanded   by remember { mutableStateOf(false) }
    var serviceExpanded  by remember { mutableStateOf(false) }
    var showFechaPicker  by remember { mutableStateOf(false) }
    var showTimePicker   by remember { mutableStateOf(false) }

    // [FIX-2] Medianoche UTC de hoy para bloquear selección de fechas pasadas
    val todayUtcMillis = remember {
        LocalDate.now().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
    }
    val fechaPickerState = rememberDatePickerState(
        // [FIX-2] Solo permitir fechas >= hoy
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis >= todayUtcMillis
        },
    )

    val canCreate = selectedClient != null && selectedBarber != null &&
        fecha.isNotBlank() && hora.isNotBlank() && selectedServiceIds.isNotEmpty()

    // DatePickerDialog para la fecha de la reserva — calendario blanco absoluto
    if (showFechaPicker) {
        DatePickerDialog(
            onDismissRequest = { showFechaPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaPickerState.selectedDateMillis?.let { millis ->
                        // [FIX-2] Convertir con ZoneOffset.UTC para evitar desfase de zona horaria
                        fecha = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().toString()
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

    if (showTimePicker) {
        AdminTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour24, minute ->
                hora = "%02d:%02d".format(hour24, minute)
                showTimePicker = false
            },
        )
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
                            singleLine = true,
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
                            singleLine = true,
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
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray,
                            ),
                        )
                    }
                }

                // Hora — selector visual con ruedas de scroll (NumberPicker)
                item {
                    Box(modifier = Modifier.fillMaxWidth().clickable { showTimePicker = true }) {
                        OutlinedTextField(
                            value = hora,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Hora*") },
                            placeholder = { Text("Toca para elegir hora") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = Color.Black,
                                disabledBorderColor = Color.Gray,
                                disabledLabelColor = Color.Gray,
                                disabledPlaceholderColor = Color.Gray,
                            ),
                            trailingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = "Elegir hora", tint = Color.Black)
                            },
                        )
                    }
                }

                // Servicios — dropdown in-place (mismo patrón que EditBookingDialog)
                if (services.isNotEmpty()) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            // TextField que actúa como disparador del dropdown
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = when (selectedServiceIds.size) {
                                        0    -> ""
                                        1    -> services.find { it.id == selectedServiceIds.first() }?.name ?: "1 servicio"
                                        else -> "${selectedServiceIds.size} servicios seleccionados"
                                    },
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Servicios*") },
                                    trailingIcon = {
                                        Icon(
                                            imageVector = if (serviceExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = null,
                                        )
                                    },
                                    enabled = false,
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                    ),
                                )
                                Box(
                                    modifier = Modifier
                                        .matchParentSize()
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                        ) { serviceExpanded = !serviceExpanded },
                                )
                            }

                            // Lista in-place de servicios con checkboxes
                            if (serviceExpanded) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    elevation = CardDefaults.cardElevation(2.dp),
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .heightIn(max = 220.dp)
                                            .verticalScroll(rememberScrollState()),
                                    ) {
                                        services.forEach { svc ->
                                            val isChecked = selectedServiceIds.contains(svc.id)
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        selectedServiceIds = if (isChecked) {
                                                            selectedServiceIds - svc.id
                                                        } else {
                                                            selectedServiceIds + svc.id
                                                        }
                                                    }
                                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            ) {
                                                Checkbox(checked = isChecked, onCheckedChange = null)
                                                Text(
                                                    "${svc.name}  S/ ${"%.2f".format(svc.price)}",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            // Chips de servicios seleccionados
                            if (selectedServiceIds.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    services
                                        .filter { selectedServiceIds.contains(it.id) }
                                        .forEach { svc ->
                                            ServiceChipRemovable(
                                                name = svc.name,
                                                onRemove = { selectedServiceIds = selectedServiceIds - svc.id },
                                            )
                                        }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        selectedClient!!.codigoCliente,
                        selectedBarber!!.codigoBarbero,
                        fecha.trim(),
                        hora.trim(),
                        selectedServiceIds.toList(),
                    )
                },
                enabled = canCreate,
            ) { Text("Crear", color = if (canCreate) Color.Black else Color.Gray) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Black) }
        },
    )
}

/** Card de reserva con chips de servicios (mismo patrón visual que BookingCard del cliente) */
@Composable
private fun AdminBookingCard(
    booking: AdminBooking,
    onChangeStatus: (String) -> Unit,
    onEdit: (() -> Unit)? = null,
) {
    val statusColor = when (booking.status.uppercase()) {
        "PENDING"          -> Color(0xFFFF9800)
        // [FIX-3] MODIFIED_PENDING: azul para distinguirlo de PENDING y CONFIRMED
        "MODIFIED_PENDING" -> Color(0xFF1976D2)
        "CONFIRMED"        -> Color(0xFF4CAF50)
        "IN_PROGRESS"      -> Color(0xFF7B1FA2)
        "CANCELLED"        -> Color(0xFFE53935)
        "COMPLETED"        -> Color(0xFF1565C0)
        else               -> Color(0xFF9E9E9E)
    }
    val statusLabel = when (booking.status.uppercase()) {
        "PENDING"          -> "Pendiente"
        // [FIX-3] Etiqueta visual para MODIFIED_PENDING
        "MODIFIED_PENDING" -> "Modif. Pendiente"
        "CONFIRMED"        -> "Confirmada"
        "IN_PROGRESS"      -> "En Progreso"
        "CANCELLED"        -> "Cancelada"
        "COMPLETED"        -> "Completada"
        else               -> booking.status
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Fila: fecha + chip de estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(booking.fechaReserva, style = MaterialTheme.typography.titleMedium)
                StatusChip(label = statusLabel, color = statusColor)
            }
            Spacer(modifier = Modifier.height(4.dp))

            // Cliente: texto plano + chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "Cliente:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BookingInfoChip(booking.clientName)
            }
            // Barbero: texto plano + chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    "Barbero:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                BookingInfoChip(booking.barberName)
            }
            // Hora
            if (booking.startTime.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        "Hora:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    BookingInfoChip(booking.startTime)
                }
            }

            // Servicios: etiqueta + chips horizontales (igual que BookingCard)
            if (booking.services.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Servicios:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                        BookingInfoChip(svc.name)
                    }
                }
                val total = booking.services.sumOf { it.price.toDouble() }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    "Total: S/ ${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            // Botones de acción (estado + editar)
            val actions = nextStatusOptions[booking.status.uppercase()]
            val canEdit = booking.status.uppercase() in listOf("PENDING", "CONFIRMED")
            if (!actions.isNullOrEmpty() || canEdit) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (canEdit && onEdit != null) {
                        Button(
                            onClick = onEdit,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                            modifier = Modifier.height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Editar", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                    actions?.forEach { (status, label) ->
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
                            contentPadding = PaddingValues(horizontal = 12.dp),
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

/** Chip de estado coloreado */
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

/** Chip genérico de información (mismo estilo que BookingServiceChip del cliente) */
@Composable
private fun BookingInfoChip(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(1.dp, Color.Black, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
        )
    }
}

/** Chip removible para servicios seleccionados en el dialog de creación */
@Composable
private fun ServiceChipRemovable(name: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)
            .border(1.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(50))
            .clickable { onRemove() }
            .padding(start = 10.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,
        )
        Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Quitar",
            modifier = Modifier.size(12.dp),
            tint = Color.Black.copy(alpha = 0.5f),
        )
    }
}

/** Dialog selector de hora con ruedas de scroll (hora 1-12, minuto 0-59, AM/PM) */
@Composable
private fun AdminTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour24: Int, minute: Int) -> Unit,
) {
    var selectedHour   by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAm           by remember { mutableStateOf(true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    "Selecciona la hora",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    // Rueda de horas (1-12)
                    AdminScrollPickerColumn(
                        items = (1..12).toList(),
                        selectedItem = selectedHour,
                        onItemSelected = { selectedHour = it },
                        label = { "%02d".format(it) },
                    )

                    Text(
                        ":",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 8.dp),
                    )

                    // Rueda de minutos (0-59)
                    AdminScrollPickerColumn(
                        items = (0..59).toList(),
                        selectedItem = selectedMinute,
                        onItemSelected = { selectedMinute = it },
                        label = { "%02d".format(it) },
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // Toggle AM / PM
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        listOf(true, false).forEach { am ->
                            val selected = isAm == am
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary
                                        else Color.LightGray.copy(alpha = 0.3f),
                                    )
                                    .clickable { isAm = am }
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = if (am) "AM" else "PM",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selected) Color.White else Color.DarkGray,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val hour24 = when {
                                isAm && selectedHour == 12  -> 0
                                !isAm && selectedHour == 12 -> 12
                                !isAm                       -> selectedHour + 12
                                else                        -> selectedHour
                            }
                            onConfirm(hour24, selectedMinute)
                        },
                    ) { Text("Aceptar") }
                }
            }
        }
    }
}

/** Columna scrolleable con snap para seleccionar un número (horas o minutos) */
@Composable
private fun AdminScrollPickerColumn(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    label: (Int) -> String,
) {
    val listState      = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val itemHeight     = 44.dp
    val visibleItems   = 3

    LaunchedEffect(Unit) {
        val index = items.indexOf(selectedItem).coerceAtLeast(0)
        listState.scrollToItem(index)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisible = listState.firstVisibleItemIndex
            val offset       = listState.firstVisibleItemScrollOffset
            val targetIndex  = if (offset > itemHeight.value * 0.5f) firstVisible + 1 else firstVisible
            val clampedIndex = targetIndex.coerceIn(0, items.size - 1)
            onItemSelected(items[clampedIndex])
            coroutineScope.launch { listState.animateScrollToItem(clampedIndex) }
        }
    }

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(itemHeight * visibleItems),
        contentAlignment = Alignment.Center,
    ) {
        // Resaltado del elemento central
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        )

        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight * visibleItems),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(vertical = itemHeight),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = listState),
        ) {
            items(items.size) { index ->
                val item       = items[index]
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable {
                            onItemSelected(item)
                            coroutineScope.launch { listState.animateScrollToItem(index) }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label(item),
                        fontSize = if (isSelected) 24.sp else 18.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) Color.Black else Color.Gray,
                    )
                }
            }
        }
    }
}
