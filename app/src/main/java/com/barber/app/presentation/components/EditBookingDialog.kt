package com.barber.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.barber.app.domain.model.Barber
import com.barber.app.domain.model.Booking
import com.barber.app.domain.model.Service
import java.time.LocalDate
import java.time.LocalTime

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange



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

    var selectedBarber by remember { mutableStateOf<Barber?>(null) }
    var selectedService by remember { mutableStateOf<Service?>(null) }
    var selectedDate by remember { mutableStateOf(booking.fechaReserva) }

    var barberExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = try {
            LocalDate.parse(booking.fechaReserva)
                .atStartOfDay(java.time.ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {    
            null
        }
    )

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
                    ) {
                        onDismiss()
                    },
            contentAlignment = Alignment.Center
        ) { 
            
        Card(
                modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { 
                            // 🔥 Consumimos el click para que NO se propague
                        },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    ) // 👈 BLANCO FORZADO
            ) {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Text(
                    text = "Editar reserva",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 🔹 BARBER DROPDOWN
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
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = barberExpanded,
                        onDismissRequest = { barberExpanded = false }
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

                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 SERVICE DROPDOWN
                    ExposedDropdownMenuBox(
                        expanded = serviceExpanded,
                        onExpandedChange = { serviceExpanded = !serviceExpanded }
                    ) {

                        OutlinedTextField(
                            value = selectedService?.name
                            ?: booking.services.firstOrNull()?.name.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Servicio") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = serviceExpanded)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = serviceExpanded,
                            onDismissRequest = { serviceExpanded = false }
                        ) {
                            services.forEach { service ->
                                DropdownMenuItem(
                                    text = { Text(service.name) },
                                    onClick = {
                                        selectedService = service
                                        serviceExpanded = false
                                    }
                                )
                            }
                        }
                    }

                Spacer(modifier = Modifier.height(8.dp))

                // 🔹 DATE PICKER

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
                                    Icon(
                                        imageVector = Icons.Default.DateRange,
                                        contentDescription = "Seleccionar fecha"
                                    )
                                },
                            colors = OutlinedTextFieldDefaults.colors(
                                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                            
                            enabled = false, // 🔥 importante
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                Spacer(modifier = Modifier.height(12.dp))    

                Button(
                        onClick = {
                            if (selectedBarber == null || selectedService == null)
                                return@Button

                            onSave(
                                clientId,
                                selectedBarber!!.codigoBarbero,
                                selectedDate,              // 🔥 AHORA SÍ ENVÍA LA FECHA MODIFICADA
                                booking.startTime,
                                listOf(selectedService!!.id)
                            )

                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Guardar cambios")
                    }
                }
            }

            if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            Button(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let { millis ->
                                        val selectedLocalDate =
                                            java.time.Instant.ofEpochMilli(millis)
                                                .atZone(java.time.ZoneId.systemDefault())
                                                .toLocalDate()

                                        selectedDate = selectedLocalDate.toString()
                                    }
                                    showDatePicker = false
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Black,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Aceptar")
                            }
                        },
                        dismissButton = {
                            TextButton(
                                onClick = { showDatePicker = false }
                            ) {
                                Text("Cancelar")
                            }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

        }
    }
}