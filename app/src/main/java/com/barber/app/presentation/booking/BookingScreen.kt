package com.barber.app.presentation.booking

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.zIndex
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import androidx.compose.material3.AlertDialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barber.app.presentation.components.BarberCard
import com.barber.app.presentation.components.DetailOverlay
import com.barber.app.presentation.components.ErrorOverlay
import com.barber.app.presentation.components.LoadingIndicator
import com.barber.app.presentation.components.ServiceCard
import com.barber.app.presentation.components.ServiceDetailContent
import com.barber.app.domain.model.Service
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatTimeWithAmPm(time: String): String {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingScreen(
    onBookingSuccess: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: BookingViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showExitDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onBookingSuccess()
    }

    BackHandler {
        if (state.currentStep == BookingStep.BARBER) {
            showExitDialog = true
        } else {
            viewModel.previousStep()
        }
    }

    val stepTitle = when (state.currentStep) {
        BookingStep.BARBER -> "Elige tu barbero"
        BookingStep.SERVICES -> "Elige servicios"
        BookingStep.DATETIME -> "Fecha y hora"
        BookingStep.CONFIRMATION -> "Confirmar reserva"
    }

    val currentStepIndex = when (state.currentStep) {
        BookingStep.BARBER -> 0
        BookingStep.SERVICES -> 1
        BookingStep.DATETIME -> 2
        BookingStep.CONFIRMATION -> 3
    }

    if (showExitDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .zIndex(10f),
        )
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = Color.White,
            title = { Text("Cancelar reserva", color = Color.Black) },
            text = {
                Text(
                    "¿Estás seguro de que deseas salir? Se perderá el progreso de tu reserva.",
                    color = Color.Black,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    onNavigateBack()
                }) {
                    Text("Salir", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continuar reserva", color = Color.Black)
                }
            },
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stepTitle) },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (state.currentStep == BookingStep.BARBER) {
                                showExitDialog = true
                            } else {
                                viewModel.previousStep()
                            }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    },
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                StepIndicator(
                    totalSteps = 4,
                    currentStep = currentStepIndex,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                )

                if (state.isLoading) {
                    LoadingIndicator()
                    return@Column
                }

                when (state.currentStep) {
                    BookingStep.BARBER -> BarberSelectionStep(state, viewModel)
                    BookingStep.SERVICES -> ServiceSelectionStep(state, viewModel)
                    BookingStep.DATETIME -> DateTimeStep(state, viewModel)
                    BookingStep.CONFIRMATION -> ConfirmationStep(state, viewModel)
                }
            }
        }

        ErrorOverlay(
            message = state.error ?: "",
            visible = state.error != null,
            onDismiss = viewModel::clearError,
        )
    }
}

@Composable
private fun BarberSelectionStep(state: BookingState, viewModel: BookingViewModel) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(state.selectedBarber) {
        if (state.selectedBarber != null) {
            delay(2000)
            coroutineScope.launch {
                listState.animateScrollToItem(state.barbers.size)
            }
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(state.barbers) { barber ->
            BarberCard(
                barber = barber,
                isSelected = state.selectedBarber?.codigoBarbero == barber.codigoBarbero,
                onClick = { viewModel.selectBarber(barber) },
            )
        }
        item { NextButton(viewModel) }
    }
}

@Composable
private fun ServiceSelectionStep(state: BookingState, viewModel: BookingViewModel) {
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var selectedService by remember { mutableStateOf<Service?>(null) }

    LaunchedEffect(state.selectedServices.size) {
        if (state.selectedServices.isNotEmpty()) {
            delay(2000)
            coroutineScope.launch {
                listState.animateScrollToItem(state.services.size)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(state.services) { service ->
                ServiceCard(
                    service = service,
                    isSelected = state.selectedServices.contains(service.id),
                    onToggle = { viewModel.toggleService(service.id) },
                    onShowInfo = { selectedService = service },
                )
            }
            item { NextButton(viewModel) }
        }

        selectedService?.let { service ->
            DetailOverlay(
                title = "Detalle del Servicio",
                visible = true,
                onDismiss = { selectedService = null },
            ) {
                ServiceDetailContent(service)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimeStep(state: BookingState, viewModel: BookingViewModel) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = false)

    if (showDatePicker) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showDatePicker = false },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                    modifier = Modifier
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        "Selecciona la fecha",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Black,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.fillMaxWidth(),
                        showModeToggle = false,
                        title = null,
                        headline = null,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 8.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancelar", color = Color.Black)
                        }
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                viewModel.onDateChange(sdf.format(Date(millis)))
                            }
                            showDatePicker = false
                        }) {
                            Text("Aceptar", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    if (showTimePicker) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { showTimePicker = false },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
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
                    Spacer(modifier = Modifier.height(16.dp))
                    TimeInput(
                        state = timePickerState,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        TextButton(onClick = { showTimePicker = false }) {
                            Text("Cancelar", color = Color.Black)
                        }
                        TextButton(onClick = {
                            val h = "%02d".format(timePickerState.hour)
                            val m = "%02d".format(timePickerState.minute)
                            viewModel.onTimeChange("$h:$m")
                            showTimePicker = false
                        }) {
                            Text("Aceptar", color = Color.Black)
                        }
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = state.selectedDate,
            onValueChange = {},
            label = { Text("Fecha") },
            placeholder = { Text("Toca para elegir fecha") },
            singleLine = true,
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showDatePicker = true }) {
                    Icon(Icons.Default.CalendarMonth, contentDescription = "Elegir fecha")
                }
            },
        )
        OutlinedTextField(
            value = if (state.selectedTime.isNotBlank()) formatTimeWithAmPm(state.selectedTime) else "",
            onValueChange = {},
            label = { Text("Hora") },
            placeholder = { Text("Toca para elegir hora") },
            singleLine = true,
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                IconButton(onClick = { showTimePicker = true }) {
                    Icon(Icons.Default.Schedule, contentDescription = "Elegir hora")
                }
            },
        )
        Spacer(modifier = Modifier.weight(1f))
        NextButton(viewModel)
    }
}

@Composable
private fun ConfirmationStep(state: BookingState, viewModel: BookingViewModel) {
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Resumen de tu reserva", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                Text("Barbero: ${state.selectedBarber?.nombres ?: ""}")
                Text("Fecha: ${state.selectedDate}")
                Text("Hora: ${formatTimeWithAmPm(state.selectedTime)}")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Servicios:", style = MaterialTheme.typography.titleMedium)
                state.services.filter { state.selectedServices.contains(it.id) }.forEach { service ->
                    Text("  - ${service.name} (S/ ${service.price})")
                }
                Spacer(modifier = Modifier.height(8.dp))
                val total = state.services
                    .filter { state.selectedServices.contains(it.id) }
                    .sumOf { it.price.toDouble() }
                Text(
                    "Total: S/ ${"%.2f".format(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = viewModel::nextStep,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !state.isLoading,
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        ) {
            Text("Confirmar Reserva")
        }
    }
}

@Composable
private fun NextButton(viewModel: BookingViewModel) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
        Button(
            onClick = viewModel::nextStep,
            modifier = Modifier.fillMaxWidth().height(50.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
        ) {
            Text("Siguiente")
        }
    }
}

@Composable
private fun StepIndicator(
    totalSteps: Int,
    currentStep: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        for (i in 0 until totalSteps) {
            val isCompleted = i < currentStep
            val isCurrent = i == currentStep

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isCompleted -> MaterialTheme.colorScheme.primary
                            isCurrent -> MaterialTheme.colorScheme.primary
                            else -> Color.LightGray
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                } else {
                    Text(
                        text = "${i + 1}",
                        color = if (isCurrent) Color.White else Color.DarkGray,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            if (i < totalSteps - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(
                            if (i < currentStep) MaterialTheme.colorScheme.primary
                            else Color.LightGray
                        ),
                )
            }
        }
    }
}
