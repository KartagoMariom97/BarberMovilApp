package com.barber.app.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import com.barber.app.presentation.components.ErrorOverlay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
    onRegisterSuccess: () -> Unit,
    viewModel: RegisterViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onRegisterSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registro") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = state.nombres,
                    onValueChange = viewModel::onNombresChange,
                    label = { Text("Nombres completos") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = state.fechaNacimiento,
                    onValueChange = {},
                    label = { Text("Fecha de nacimiento") },
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
                    value = state.dni,
                    onValueChange = viewModel::onDniChange,
                    label = { Text("DNI") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                Text("Género", style = MaterialTheme.typography.labelLarge)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("M" to "Masculino", "F" to "Femenino").forEach { (value, label) ->
                        FilterChip(
                            selected = state.genero == value,
                            onClick = { viewModel.onGeneroChange(value) },
                            label = { Text(label) },
                        )
                    }
                }

                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    label = { Text("E-mail") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                OutlinedTextField(
                    value = state.telefono,
                    onValueChange = viewModel::onTelefonoChange,
                    label = { Text("Teléfono") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = viewModel::register,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    enabled = !state.isLoading,
                ) {
                    Text(if (state.isLoading) "Registrando..." else "Registrarse")
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            ErrorOverlay(
                message = state.error ?: "",
                visible = state.error != null,
                onDismiss = viewModel::clearError,
            )

            if (showDatePicker) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
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
                                        viewModel.onFechaNacimientoChange(sdf.format(Date(millis)))
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
        }
    }
}
