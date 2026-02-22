package com.barber.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.barber.app.domain.model.Barber
import com.barber.app.domain.model.Booking
import com.barber.app.domain.model.Service
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBookingDialog(
    booking: Booking,
    barbers: List<Barber>,
    services: List<Service>,
    clientId: Long,
    onDismiss: () -> Unit,
    onSave: (
        clientId: Long,
        barberId: Long,
        fechaReserva: String,
        startTime: String,
        serviceIds: List<Long>
    ) -> Unit
) {
    // Pre-select barber from current booking
    var selectedBarber by remember {
        mutableStateOf(barbers.find { it.nombres == booking.barberName })
    }
    // Pre-select all current booking services (multi-select)
    var selectedServiceIds by remember {
        mutableStateOf(booking.services.map { svc -> svc.serviceId }.toSet())
    }
    var selectedDate by remember { mutableStateOf(booking.fechaReserva) }
    var selectedTime by remember { mutableStateOf(booking.startTime) }

    var barberExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }
    var attemptedSave by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val todayMillis = remember {
        LocalDate.now()
            .atStartOfDay(ZoneOffset.UTC) // UTC para que coincida con los millis del DatePicker
            .toInstant()
            .toEpochMilli()
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            LocalDate.parse(booking.fechaReserva)
                .atStartOfDay(ZoneOffset.UTC) // UTC para evitar desfase de zona horaria
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) { null },
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                utcTimeMillis >= todayMillis
        }
    )

    val barberError = attemptedSave && selectedBarber == null
    val serviceError = attemptedSave && selectedServiceIds.isEmpty()

    Dialog(
        onDismissRequest = { onDismiss() },
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .navigationBarsPadding()
                .imePadding()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    // ── Título ──────────────────────────────────────────
                    Text(
                        text = "Editar reserva",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    HorizontalDivider(color = Color.Black.copy(alpha = 0.08f))

                    // ── Barbero ─────────────────────────────────────────
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = barberExpanded,
                            onExpandedChange = { barberExpanded = !barberExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedBarber?.nombres ?: booking.barberName,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Barbero") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = barberExpanded)
                                },
                                isError = barberError,
                                modifier = Modifier.menuAnchor().fillMaxWidth()
                            )
                            ExposedDropdownMenu(
                                expanded = barberExpanded,
                                onDismissRequest = { barberExpanded = false },
                                modifier = Modifier.heightIn(max = 172.dp) // ~3 items visibles
                            ) {
                                barbers.filter { it.active }.forEach { barber ->
                                    DropdownMenuItem(
                                        text = { Text(barber.nombres) },
                                        onClick = {
                                            selectedBarber = barber
                                            barberExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                        if (barberError) {
                            Text(
                                "Selecciona un barbero",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }

                    // ── Servicios (multi-select con chips) ──────────────
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
                                label = { Text("Servicios") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = if (serviceExpanded) Icons.Default.ArrowDropUp
                                                      else Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledBorderColor = if (serviceError) Color.Red
                                                          else MaterialTheme.colorScheme.outline,
                                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                    disabledLabelColor = if (serviceError) Color.Red
                                                         else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                enabled = false,
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Área transparente para abrir/cerrar la lista
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { serviceExpanded = !serviceExpanded }
                            )
                            // ⚠️ NO usamos DropdownMenu (popup) porque su ventana flotante
                            //    captura todos los toques exteriores, impidiendo que el tap
                            //    en la opacidad llegue al Box contenedor del dialog.
                            //    La lista se renderiza in-place (debajo del TextField) para
                            //    que un solo tap en la opacidad cierre el dialog directamente.
                        }

                        // Lista de servicios in-place (sin popup) ─────────────
                        if (serviceExpanded) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(max = 220.dp)
                                        .verticalScroll(rememberScrollState())
                                ) {
                                    services.forEach { service ->
                                        val isChecked = selectedServiceIds.contains(service.id)
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectedServiceIds = if (isChecked) {
                                                        selectedServiceIds - service.id
                                                    } else {
                                                        selectedServiceIds + service.id
                                                    }
                                                }
                                                .padding(horizontal = 16.dp, vertical = 12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Checkbox(checked = isChecked, onCheckedChange = null)
                                            Text(service.name, style = MaterialTheme.typography.bodyMedium)
                                        }
                                    }
                                }
                            }
                        }

                        if (serviceError) {
                            Text(
                                "Selecciona al menos un servicio",
                                color = Color.Red,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        // Chips de servicios seleccionados
                        if (selectedServiceIds.isNotEmpty()) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                elevation = CardDefaults.cardElevation(0.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    services
                                        .filter { selectedServiceIds.contains(it.id) }
                                        .forEach { svc ->
                                            EditServiceChip(
                                                name = svc.name,
                                                onRemove = {
                                                    selectedServiceIds = selectedServiceIds - svc.id
                                                }
                                            )
                                        }
                                }
                            }
                        }
                    }

                    // ── Fecha ───────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                    ) {
                        OutlinedTextField(
                            value = selectedDate,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Fecha de reserva") },
                            trailingIcon = {
                                Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── Hora ────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTimePicker = true }
                    ) {
                        OutlinedTextField(
                            value = if (selectedTime.isNotBlank()) formatEditTimeWithAmPm(selectedTime) else "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Hora de reserva") },
                            trailingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = "Seleccionar hora")
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledBorderColor = MaterialTheme.colorScheme.outline,
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            enabled = false,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // ── Botón guardar ────────────────────────────────────
                    Button(
                        onClick = {
                            attemptedSave = true
                            if (selectedBarber == null || selectedServiceIds.isEmpty()) return@Button
                            onSave(
                                clientId,
                                selectedBarber!!.codigoBarbero,
                                selectedDate,
                                selectedTime,
                                selectedServiceIds.toList()
                            )
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Guardar cambios", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            // ── Date picker overlay ─────────────────────────────────────
            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        Button(
                            onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    // Convertimos con ZoneOffset.UTC porque el DatePicker guarda millis en UTC
                                    val selectedLocalDate =
                                        java.time.Instant.ofEpochMilli(millis)
                                            .atZone(ZoneOffset.UTC)
                                            .toLocalDate()
                                    selectedDate = selectedLocalDate.toString()
                                }
                                showDatePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Black,
                                contentColor = Color.White
                            )
                        ) { Text("Aceptar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            // ── Time picker overlay ─────────────────────────────────────
            if (showTimePicker) {
                EditTimePickerDialog(
                    initialTimeStr = selectedTime,
                    onDismiss = { showTimePicker = false },
                    onConfirm = { hour24, minute ->
                        val h = "%02d".format(hour24)
                        val m = "%02d".format(minute)
                        selectedTime = "$h:$m:00"
                        showTimePicker = false
                    }
                )
            }
        }
    }
}

// ── Chip removible para servicios seleccionados ─────────────────────────────
@Composable
private fun EditServiceChip(name: String, onRemove: () -> Unit) {
    // Para cambiar el color: modifica containerColor (fondo) y los colores de Text/Icon
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White)           // ← FONDO del chip
            .border(1.dp, Color.Black.copy(alpha = 0.25f), RoundedCornerShape(50))
            .clickable { onRemove() }
            .padding(start = 10.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Black,              // ← COLOR DE LETRA del chip
            fontWeight = FontWeight.Medium
        )
        Icon(
            Icons.Default.Close,
            contentDescription = "Quitar",
            modifier = Modifier.size(13.dp),
            tint = Color.Black.copy(alpha = 0.5f)
        )
    }
}

// ── Helpers ─────────────────────────────────────────────────────────────────

private fun formatEditTimeWithAmPm(time: String): String {
    if (time.isBlank()) return ""
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

// ── Time picker dialog ───────────────────────────────────────────────────────

@Composable
private fun EditTimePickerDialog(
    initialTimeStr: String,
    onDismiss: () -> Unit,
    onConfirm: (hour24: Int, minute: Int) -> Unit,
) {
    val parts = initialTimeStr.split(":")
    val initHour24 = parts.getOrNull(0)?.toIntOrNull() ?: 12
    val initMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val initIsAm = initHour24 < 12
    val initHour12 = when {
        initHour24 == 0 -> 12
        initHour24 > 12 -> initHour24 - 12
        else -> initHour24
    }

    var selectedHour by remember { mutableStateOf(initHour12) }
    var selectedMinute by remember { mutableStateOf(initMinute) }
    var isAm by remember { mutableStateOf(initIsAm) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
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
                    TimeScrollPickerColumn(
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

                    TimeScrollPickerColumn(
                        items = (0..59).toList(),
                        selectedItem = selectedMinute,
                        onItemSelected = { selectedMinute = it },
                        label = { "%02d".format(it) },
                    )

                    Spacer(modifier = Modifier.width(16.dp))

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
                                        else Color.LightGray.copy(alpha = 0.3f)
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
                                isAm && selectedHour == 12 -> 0
                                !isAm && selectedHour == 12 -> 12
                                !isAm -> selectedHour + 12
                                else -> selectedHour
                            }
                            onConfirm(hour24, selectedMinute)
                        },
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        }
    }
}

// ── Scroll picker column (rueda de horas/minutos) ────────────────────────────

@Composable
private fun TimeScrollPickerColumn(
    items: List<Int>,
    selectedItem: Int,
    onItemSelected: (Int) -> Unit,
    label: (Int) -> String,
) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val itemHeight = 44.dp
    val visibleItems = 3

    LaunchedEffect(Unit) {
        val index = items.indexOf(selectedItem).coerceAtLeast(0)
        listState.animateScrollToItem(index)
    }

    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val firstVisible = listState.firstVisibleItemIndex
            val offset = listState.firstVisibleItemScrollOffset
            val targetIndex = if (offset > itemHeight.value * 0.5f) firstVisible + 1 else firstVisible
            val clampedIndex = targetIndex.coerceIn(0, items.size - 1)
            onItemSelected(items[clampedIndex])
            coroutineScope.launch {
                listState.animateScrollToItem(clampedIndex)
            }
        }
    }

    Box(
        modifier = Modifier
            .width(64.dp)
            .height(itemHeight * visibleItems),
        contentAlignment = Alignment.Center,
    ) {
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
                val item = items[index]
                val isSelected = item == selectedItem
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight)
                        .clickable {
                            onItemSelected(item)
                            coroutineScope.launch {
                                listState.animateScrollToItem(index)
                            }
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
