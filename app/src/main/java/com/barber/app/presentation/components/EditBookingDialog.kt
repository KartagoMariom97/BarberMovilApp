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
    var selectedServices by remember { mutableStateOf<List<Service>>(emptyList()) }

    var barberExpanded by remember { mutableStateOf(false) }
    var serviceExpanded by remember { mutableStateOf(false) }

    Dialog(
    onDismissRequest = onDismiss,
    properties = androidx.compose.ui.window.DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = false
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    onDismiss()   // 👈 Cierra al tocar afuera
                },
            contentAlignment = Alignment.Center
        ) { 
        Surface(
                modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                    ) { },
                shape = RoundedCornerShape(20.dp),
                color = Color.White, // 👈 BLANCO FORZADO
                tonalElevation = 6.dp
            ) 
        {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {

                Text(
                    text = "Editar reserva",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (selectedBarber == null || selectedServices.isEmpty())
                            return@Button

                        onSave(
                            clientId,
                            selectedBarber!!.codigoBarbero,
                            booking.fechaReserva,
                            booking.startTime,
                            selectedServices.map { it.id }
                        )

                        onDismiss()
                    },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Guardar cambios")
                    }
                }
            }
        }
    }
}