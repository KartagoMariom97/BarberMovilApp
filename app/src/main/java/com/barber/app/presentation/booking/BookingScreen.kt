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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
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
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.graphics.graphicsLayer
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

private fun formatDateForDisplay(date: String): String {
    if (date.isBlank()) return ""
    return try {
        val parts = date.split("-")
        if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else date
    } catch (_: Exception) { date }
}

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
    var showSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) showSuccessDialog = true
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
                .background(Color.Black.copy(alpha = 0.7f))
                .zIndex(10f),
        )
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            containerColor = Color.White,
            title = {
                Text(
                    "Cancelar reserva",
                    color = Color.Black,
                    fontSize = 18.sp,
                )
            },
            text = {
                Text(
                    "Se perderá el progreso de tu reserva.",
                    color = Color.Black,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showExitDialog = false
                    viewModel.resetState()
                    onNavigateBack()
                }) {
                    Text("Salir", color = Color(0xFFE53935))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Continuar", color = Color.Black)
                }
            },
        )
    }

    if (showSuccessDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                .zIndex(10f),
        )
        AlertDialog(
            onDismissRequest = {
                showSuccessDialog = false
                onBookingSuccess()
            },
            containerColor = Color.White,
            icon = {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(40.dp),
                )
            },
            title = {
                Text(
                    "Reserva creada",
                    color = Color.Black,
                    fontSize = 18.sp,
                )
            },
            text = {
                Text(
                    "Tu reserva se ha registrado exitosamente.",
                    color = Color.Black,
                    fontSize = 14.sp,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showSuccessDialog = false
                    onBookingSuccess()
                }) {
                    Text("Aceptar", color = Color(0xFF4CAF50))
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
                    onStepClick = { step -> viewModel.goToStep(step) },
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
            delay(1000)
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

    // Auto-confirm when date is tapped
    LaunchedEffect(datePickerState.selectedDateMillis) {
        if (showDatePicker && datePickerState.selectedDateMillis != null) {
            delay(300)
            datePickerState.selectedDateMillis?.let { millis ->
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                viewModel.onDateChange(sdf.format(Date(millis)))
            }
            showDatePicker = false
        }
    }

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
                    .padding(horizontal = 8.dp)
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
                    modifier = Modifier.verticalScroll(rememberScrollState()),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer {
                                scaleX = 0.92f
                                scaleY = 0.92f
                            },
                        showModeToggle = false,
                        title = null,
                        headline = null,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 8.dp, bottom = 4.dp),
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
        NumberTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour24, minute ->
                val h = "%02d".format(hour24)
                val m = "%02d".format(minute)
                viewModel.onTimeChange("$h:$m")
                showTimePicker = false
            },
        )
    }

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        OutlinedTextField(
            value = formatDateForDisplay(state.selectedDate),
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
private fun NumberTimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour24: Int, minute: Int) -> Unit,
) {
    var selectedHour by remember { mutableStateOf(12) }
    var selectedMinute by remember { mutableStateOf(0) }
    var isAm by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
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
                    // Hour picker (1-12)
                    ScrollPickerColumn(
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

                    // Minute picker (0-59)
                    ScrollPickerColumn(
                        items = (0..59).toList(),
                        selectedItem = selectedMinute,
                        onItemSelected = { selectedMinute = it },
                        label = { "%02d".format(it) },
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // AM/PM toggle
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

@Composable
private fun ScrollPickerColumn(
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
        listState.scrollToItem(index)
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
        // Selection highlight
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
                Text("Fecha: ${formatDateForDisplay(state.selectedDate)}")
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
    onStepClick: (Int) -> Unit = {},
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
                    )
                    .then(
                        if (isCompleted) Modifier.clickable { onStepClick(i) }
                        else Modifier
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
