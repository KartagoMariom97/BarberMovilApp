package com.barber.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    LaunchedEffect(state.saveSuccess) {
        if (state.saveSuccess) viewModel.clearSaveSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showEditDialog() }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar perfil", tint = Color(0xFFFFC107))
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Black,
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = state.nombres.ifBlank { "Administrador" },
                style = MaterialTheme.typography.headlineMedium,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = state.role.ifBlank { "" },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            ) {
                Column {
                    ListItem(
                        headlineContent = {
                            Column {
                                Text("Nombre", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    state.nombres.ifBlank { "—" },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                    )
                    ListItem(
                        headlineContent = {
                            Column {
                                Text("Email", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    state.email.ifBlank { "—" },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                    )
                    ListItem(
                        headlineContent = {
                            Column {
                                Text("Rol", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    state.role.ifBlank { "—" },
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                    )
                    ListItem(
                        headlineContent = {
                            Column {
                                Text("ID de entidad", style = MaterialTheme.typography.labelSmall)
                                Text(
                                    if (state.entityId > 0) state.entityId.toString() else "—",
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón principal de editar perfil
            Button(
                onClick = { viewModel.showEditDialog() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = Color.White),
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                Text("Editar Perfil")
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Dialog de edición de perfil
    if (state.showEditDialog) {
        EditAdminProfileDialog(
            initialNombres = state.nombres,
            initialEmail   = state.email,
            isSaving       = state.isSaving,
            saveError      = state.saveError,
            onDismiss      = { viewModel.dismissEditDialog() },
            onSave         = { nombres, email, password ->
                viewModel.updateProfile(nombres, email, password)
            },
        )
    }
}

@Composable
private fun EditAdminProfileDialog(
    initialNombres: String,
    initialEmail: String,
    isSaving: Boolean,
    saveError: String?,
    onDismiss: () -> Unit,
    onSave: (nombres: String, email: String, password: String?) -> Unit,
) {
    val focusManager = LocalFocusManager.current

    var nombres         by remember { mutableStateOf(initialNombres) }
    var email           by remember { mutableStateOf(initialEmail) }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    val passwordMismatch = password.isNotBlank() && confirmPassword.isNotBlank() && password != confirmPassword
    val canSave = nombres.isNotBlank() && email.isNotBlank() && !passwordMismatch && !isSaving

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = Color.White,
        title = { Text("Editar Perfil", color = Color.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = nombres,
                    onValueChange = { if (!it.contains('\n')) nombres = it },
                    label = { Text("Nombres*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { if (!it.contains('\n')) email = it },
                    label = { Text("Email*") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { if (!it.contains('\n') && it.length <= 50) password = it },
                    label = { Text("Nueva contraseña (opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                            )
                        }
                    },
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { if (!it.contains('\n') && it.length <= 50) confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    isError = passwordMismatch,
                    supportingText = if (passwordMismatch) {
                        { Text("Las contraseñas no coinciden", color = Color.Red) }
                    } else null,
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(
                                if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                            )
                        }
                    },
                )
                if (saveError != null) {
                    Text(saveError, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                }
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        nombres.trim(),
                        email.trim(),
                        password.takeIf { it.isNotBlank() },
                    )
                },
                enabled = canSave,
            ) {
                Text("Guardar", color = if (canSave) Color.Black else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isSaving) onDismiss() }) {
                Text("Cancelar", color = Color.Black)
            }
        },
    )
}
