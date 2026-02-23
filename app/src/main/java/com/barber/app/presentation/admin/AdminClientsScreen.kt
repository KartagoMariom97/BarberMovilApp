package com.barber.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.clickable
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barber.app.domain.model.AdminClient
import com.barber.app.presentation.components.ErrorOverlay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClientsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminClientsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editingClient by remember { mutableStateOf<AdminClient?>(null) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) viewModel.clearSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Clientes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        /** FAB para abrir el diálogo de creación de cliente */
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.showCreateDialog() }) {
                Icon(Icons.Default.Add, contentDescription = "Crear cliente")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading && state.clients.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(state.clients) { client ->
                        ClientAdminCard(
                            client = client,
                            onEdit = { editingClient = client },
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

    editingClient?.let { client ->
        EditClientDialog(
            client = client,
            onDismiss = { editingClient = null },
            onSave = { nombres, email, telefono ->
                viewModel.updateClient(client.codigoCliente, nombres, email, telefono)
                editingClient = null
            },
        )
    }

    /** Diálogo de creación: visible cuando showCreateDialog == true */
    if (state.showCreateDialog) {
        CreateClientDialog(
            onDismiss = { viewModel.dismissCreateDialog() },
            onCreate  = { nombres, fechaNacimiento, dni, genero, email, telefono, password ->
                viewModel.createClient(nombres, fechaNacimiento, dni, genero, email, telefono, password)
            },
        )
    }
}

/** Diálogo para crear un cliente nuevo con DatePickerDialog para fecha de nacimiento */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateClientDialog(
    onDismiss: () -> Unit,
    onCreate: (nombres: String, fechaNacimiento: String, dni: String, genero: String, email: String?, telefono: String, password: String?) -> Unit,
) {
    var nombres         by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }
    var dni             by remember { mutableStateOf("") }
    var telefono        by remember { mutableStateOf("") }
    var email           by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var generoExpanded  by remember { mutableStateOf(false) }
    var genero          by remember { mutableStateOf("") }
    var showFechaPicker by remember { mutableStateOf(false) }
    val fechaPickerState = rememberDatePickerState()
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    val generoOptions = listOf("M", "F", "Otro")
    val canCreate = nombres.isNotBlank() && fechaNacimiento.isNotBlank() &&
        dni.isNotBlank() && genero.isNotBlank() && telefono.isNotBlank()

    // DatePickerDialog para fecha de nacimiento — calendario blanco absoluto
    if (showFechaPicker) {
        DatePickerDialog(
            onDismissRequest = { showFechaPicker = false },
            confirmButton = {
                TextButton(onClick = {
                    fechaPickerState.selectedDateMillis?.let { millis ->
                        fechaNacimiento = sdf.format(Date(millis))
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
        title = { Text("Nuevo Cliente", color = Color.Black) },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    // Nombres — Máx. 100 caracteres
                    OutlinedTextField(
                        value = nombres,
                        onValueChange = { if (it.length <= 100) nombres = it },
                        label = { Text("Nombres*") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Máx. 100 caracteres") },
                    )
                }
                item {
                    // Fecha de nacimiento — selección mediante DatePickerDialog
                    Box(modifier = Modifier.fillMaxWidth().clickable { showFechaPicker = true }) {
                        OutlinedTextField(
                            value = fechaNacimiento,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Fecha Nacimiento*") },
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
                    // DNI — Máx. 10 dígitos
                    OutlinedTextField(
                        value = dni,
                        onValueChange = { if (it.length <= 10) dni = it },
                        label = { Text("DNI*") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Máx. 10 dígitos") },
                    )
                }
                item {
                    // Selector de género
                    ExposedDropdownMenuBox(
                        expanded = generoExpanded,
                        onExpandedChange = { generoExpanded = !generoExpanded },
                    ) {
                        OutlinedTextField(
                            value = genero.ifBlank { "Seleccionar" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Género*") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(generoExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                        )
                        ExposedDropdownMenu(expanded = generoExpanded, onDismissRequest = { generoExpanded = false }) {
                            generoOptions.forEach { g ->
                                DropdownMenuItem(text = { Text(g) }, onClick = { genero = g; generoExpanded = false })
                            }
                        }
                    }
                }
                item {
                    // Teléfono — Máx. 15 dígitos
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { if (it.length <= 15) telefono = it },
                        label = { Text("Teléfono*") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Máx. 15 dígitos") },
                    )
                }
                item {
                    // Email — Máx. 100 caracteres (campo opcional)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (it.length <= 100) email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        supportingText = { Text("Máx. 100 caracteres") },
                    )
                }
                item {
                    // Contraseña con toggle de visibilidad — Máx. 50 caracteres (campo opcional)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { if (it.length <= 50) password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        supportingText = { Text("Máx. 50 caracteres") },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                            }
                        },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        nombres.trim(), fechaNacimiento, dni.trim(), genero,
                        email.takeIf { it.isNotBlank() }, telefono.trim(),
                        password.takeIf { it.isNotBlank() },
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

@Composable
private fun ClientAdminCard(
    client: AdminClient,
    onEdit: () -> Unit,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(client.nombres, style = MaterialTheme.typography.titleMedium)
                if (client.email.isNotBlank()) {
                    Text(client.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (client.telefono.isNotBlank()) {
                    Text(client.telefono, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFFFFC107))
            }
        }
    }
}

@Composable
private fun EditClientDialog(
    client: AdminClient,
    onDismiss: () -> Unit,
    onSave: (nombres: String?, email: String?, telefono: String?) -> Unit,
) {
    var nombres  by remember { mutableStateOf(client.nombres) }
    var email    by remember { mutableStateOf(client.email) }
    var telefono by remember { mutableStateOf(client.telefono) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text("Editar Cliente", color = Color.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = nombres, onValueChange = { nombres = it }, label = { Text("Nombres") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    nombres.takeIf { it.isNotBlank() },
                    email.takeIf { it.isNotBlank() },
                    telefono.takeIf { it.isNotBlank() },
                )
            }) { Text("Guardar", color = Color.Black) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Black) }
        },
    )
}
