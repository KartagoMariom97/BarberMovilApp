package com.barber.app.presentation.admin

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.Switch
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.barber.app.domain.model.AdminClient
import com.barber.app.presentation.components.ErrorOverlay
import com.barber.app.presentation.components.LoadingIndicator
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClientsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminClientsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editingClient by remember { mutableStateOf<AdminClient?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var successDialogMessage by remember { mutableStateOf("") }
    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) {
            successDialogMessage = state.successMessage!!
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Clientes") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
        // [MEJORA] Migrado de accompanist-swiperefresh a Material3 PullToRefreshBox (nativo)
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = { viewModel.refresh() },
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                if (state.isLoading) {
                    LoadingIndicator()
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
                                onToggleStatus = { active -> viewModel.toggleClientStatus(client.codigoCliente, active) },
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

    editingClient?.let { client ->
        EditClientDialog(
            client = client,
            onDismiss = { editingClient = null },
            onSave = { nombres, email, telefono, password, dni, genero, fechaNacimiento ->
                viewModel.updateClient(
                    client.codigoCliente,
                    nombres,
                    email,
                    telefono,
                    password,
                    dni,
                    genero,
                    fechaNacimiento,
                )
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

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; viewModel.clearSuccess() },
            containerColor = Color.White,
            title = { Text("¡Operación exitosa!", color = Color.Black) },
            text = { Text(successDialogMessage, color = Color.Black) },
            confirmButton = {
                TextButton(onClick = { showSuccessDialog = false; viewModel.clearSuccess() }) {
                    Text("Aceptar", color = Color.Black)
                }
            },
        )
    }
}

@Composable
private fun ClientAdminCard(
    client: AdminClient,
    onEdit: () -> Unit,
    onToggleStatus: (Boolean) -> Unit,
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(client.nombres, style = MaterialTheme.typography.titleLarge)
                    if (client.email.isNotBlank()) {
                        Text(client.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (client.telefono.isNotBlank()) {
                        Text("Telefono: ${client.telefono}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (client.dni.isNotBlank()) {
                        Text("DNI: ${client.dni}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFFFFC107))
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = if (client.active) "Activo" else "Inactivo",
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 24.sp),
                    color = if (client.active) Color(0xFF388E3C) else Color(0xFFE53935),
                )
                Switch(
                    checked = client.active,
                    onCheckedChange = onToggleStatus,
                )
            }
        }
    }
}

/** Diálogo de EDICIÓN completo: todos los campos del cliente incluyendo DNI y contraseña */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditClientDialog(
    client: AdminClient,
    onDismiss: () -> Unit,
    onSave: (
        nombres: String?,
        email: String?,
        telefono: String?,
        password: String?,
        dni: String?,
        genero: String?,
        fechaNacimiento: String?,
    ) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    var nombres          by remember { mutableStateOf(client.nombres) }
    var email            by remember { mutableStateOf(client.email) }
    var telefono         by remember { mutableStateOf(client.telefono) }
    var dni              by remember { mutableStateOf(client.dni) }
    var fechaNacimiento  by remember { mutableStateOf(client.fechaNacimiento) }
    var genero           by remember { mutableStateOf(client.genero) }
    var password         by remember { mutableStateOf("") }
    var passwordVisible  by remember { mutableStateOf(false) }
    var generoExpanded   by remember { mutableStateOf(false) }
    var showFechaPicker  by remember { mutableStateOf(false) }

    val fechaPickerState = rememberDatePickerState()
    // Fix: TimeZone UTC evita retroceso de día al convertir millis (medianoche UTC) a fecha local
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }
    val generoOptions = listOf("Masculino", "Femenino", "Otro")

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
        modifier = Modifier.imePadding(),
        properties = DialogProperties(decorFitsSystemWindows = false),
        title = { Text("Editar Cliente", color = Color.Black) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    OutlinedTextField(
                        value = nombres,
                        onValueChange = { if (!it.contains('\n')) nombres = it },
                        label = { Text("Nombres") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    )
                }
                item {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (!it.contains('\n')) email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    )
                }
                item {
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { if (!it.contains('\n') && it.length <= 9) telefono = it },
                        label = { Text("Teléfono") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    )
                }
                item {
                    OutlinedTextField(
                        value = dni,
                        onValueChange = { if (!it.contains('\n') && it.length <= 8) dni = it },
                        label = { Text("DNI") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        supportingText = { Text("Máx. 8 dígitos") },
                    )
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth().clickable { showFechaPicker = true }) {
                        OutlinedTextField(
                            value = fechaNacimiento,
                            onValueChange = {},
                            enabled = false,
                            label = { Text("Fecha Nacimiento") },
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
                    ExposedDropdownMenuBox(
                        expanded = generoExpanded,
                        onExpandedChange = { generoExpanded = !generoExpanded },
                    ) {
                        OutlinedTextField(
                            value = genero.ifBlank { "Seleccionar" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Género") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(generoExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            singleLine = true,
                        )
                        ExposedDropdownMenu(expanded = generoExpanded, onDismissRequest = { generoExpanded = false }) {
                            generoOptions.forEach { g ->
                                DropdownMenuItem(text = { Text(g) }, onClick = { genero = g; generoExpanded = false })
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { if (!it.contains('\n') && it.length <= 20) password = it },
                        label = { Text("Nueva contraseña (dejar vacío para no cambiar)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null,
                                )
                            }
                        },
                        supportingText = { Text("Máx. 20 caracteres") },
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    nombres.takeIf { it.isNotBlank() },
                    email.takeIf { it.isNotBlank() },
                    telefono.takeIf { it.isNotBlank() },
                    password.takeIf { it.isNotBlank() },
                    dni.takeIf { it.isNotBlank() },
                    genero.takeIf { it.isNotBlank() },
                    fechaNacimiento.takeIf { it.isNotBlank() },
                )
            }) { Text("Guardar", color = Color.Black) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Black) }
        },
    )
}

/** Diálogo de CREACIÓN de cliente — igual que antes + singleLine */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateClientDialog(
    onDismiss: () -> Unit,
    onCreate: (nombres: String, fechaNacimiento: String, dni: String, genero: String, email: String?, telefono: String, password: String?) -> Unit,
) {
    val focusManager = LocalFocusManager.current

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
    var submitted       by remember { mutableStateOf(false) }
    val fechaPickerState = rememberDatePickerState()
    // Fix: TimeZone UTC evita retroceso de día al convertir millis (medianoche UTC) a fecha local
    val sdf = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply { timeZone = TimeZone.getTimeZone("UTC") } }
    val generoOptions = listOf("Masculino", "Femenino", "Otro")

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
        modifier = Modifier.imePadding(),
        properties = DialogProperties(decorFitsSystemWindows = false),
        title = { Text("Nuevo Cliente", color = Color.Black) },
        text = {
            LazyColumn(
                modifier = Modifier.heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    // Nombres — Máx. 100 caracteres
                    OutlinedTextField(
                        value = nombres,
                        onValueChange = { if (!it.contains('\n') && it.length <= 50) nombres = it },
                        label = { Text("Nombres*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = submitted && nombres.isBlank(),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        supportingText = {
                            if (submitted && nombres.isBlank()) Text("Campo requerido", color = Color(0xFFE53935))
                            else Text("Máx. 50 caracteres")
                        },
                    )
                }
                item {
                    // Fecha de nacimiento — selección mediante DatePickerDialog
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().clickable { showFechaPicker = true }) {
                            OutlinedTextField(
                                value = fechaNacimiento,
                                onValueChange = {},
                                enabled = false,
                                label = { Text("Fecha Nacimiento*") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    disabledTextColor = Color.Black,
                                    disabledBorderColor = if (submitted && fechaNacimiento.isBlank()) Color(0xFFE53935) else Color.Gray,
                                    disabledLabelColor = if (submitted && fechaNacimiento.isBlank()) Color(0xFFE53935) else Color.Gray,
                                ),
                            )
                        }
                        if (submitted && fechaNacimiento.isBlank()) {
                            Text(
                                "Campo requerido",
                                color = Color(0xFFE53935),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                            )
                        }
                    }
                }
                item {
                    // DNI — Máx. 10 dígitos
                    OutlinedTextField(
                        value = dni,
                        onValueChange = { if (!it.contains('\n') && it.length <= 8) dni = it },
                        label = { Text("DNI*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = submitted && dni.isBlank(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        supportingText = {
                            if (submitted && dni.isBlank()) Text("Campo requerido", color = Color(0xFFE53935))
                            else Text("Máx. 8 dígitos")
                        },
                    )
                }
                item {
                    // Selector de género
                    Column {
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
                                modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                singleLine = true,
                                isError = submitted && genero.isBlank(),
                            )
                            ExposedDropdownMenu(expanded = generoExpanded, onDismissRequest = { generoExpanded = false }) {
                                generoOptions.forEach { g ->
                                    DropdownMenuItem(text = { Text(g) }, onClick = { genero = g; generoExpanded = false })
                                }
                            }
                        }
                        if (submitted && genero.isBlank()) {
                            Text(
                                "Campo requerido",
                                color = Color(0xFFE53935),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                            )
                        }
                    }
                }
                item {
                    // Teléfono — Máx. 9 dígitos
                    OutlinedTextField(
                        value = telefono,
                        onValueChange = { if (!it.contains('\n') && it.length <= 9) telefono = it },
                        label = { Text("Teléfono*") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        isError = submitted && telefono.isBlank(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        supportingText = {
                            if (submitted && telefono.isBlank()) Text("Campo requerido", color = Color(0xFFE53935))
                            else Text("Máx. 9 dígitos")
                        },
                    )
                }
                item {
                    // Email — Máx. 100 caracteres (campo opcional)
                    OutlinedTextField(
                        value = email,
                        onValueChange = { if (!it.contains('\n') && it.length <= 100) email = it },
                        label = { Text("Email") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        supportingText = { Text("Máx. 100 caracteres") },
                    )
                }
                item {
                    // Contraseña con toggle de visibilidad — Máx. 50 caracteres (campo opcional)
                    OutlinedTextField(
                        value = password,
                        onValueChange = { if (!it.contains('\n') && it.length <= 20) password = it },
                        label = { Text("Contraseña") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        supportingText = { Text("Máx. 20 caracteres") },
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
                    submitted = true
                    if (canCreate) {
                        onCreate(
                            nombres.trim(), fechaNacimiento, dni.trim(), genero,
                            email.takeIf { it.isNotBlank() }, telefono.trim(),
                            password.takeIf { it.isNotBlank() },
                        )
                    }
                },
            ) { Text("Crear", color = Color.Black) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Black) }
        },
    )
}
