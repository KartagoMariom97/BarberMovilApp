package com.barber.app.presentation.profile

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barber.app.presentation.components.LoadingIndicator

import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.material3.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember

import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.imePadding
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager

import androidx.compose.ui.text.style.TextAlign
import com.barber.app.presentation.components.EditProfileDialog
import androidx.compose.ui.zIndex


@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }
    var editNombres by remember { mutableStateOf("") }
    var editGenero by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editTelefono by remember { mutableStateOf("") }
    var editDni by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val keyboardController = LocalSoftwareKeyboardController.current

    var showSuccessDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showPasswordSuccessDialog by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    LaunchedEffect(state.updateSuccess) {
        if (state.updateSuccess) {
            showEditDialog = false
            showSuccessDialog = true
            viewModel.clearUpdateSuccess()
        }
    }

    LaunchedEffect(state.updateError) {
        val error = state.updateError
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearUpdateError()
        }
    }

    LaunchedEffect(state.changePasswordSuccess) {
        if (state.changePasswordSuccess) {
            showChangePasswordDialog = false
            showPasswordSuccessDialog = true
            viewModel.clearChangePasswordSuccess()
        }
    }

    LaunchedEffect(state.changePasswordError) {
        val error = state.changePasswordError
        if (error != null) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearChangePasswordError()
        }
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            isLoading = state.isChangingPassword,
            onDismiss = { showChangePasswordDialog = false },
            onSave    = { newPassword -> viewModel.changePassword(newPassword) },
        )
    }

    if (showPasswordSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordSuccessDialog = false },
            containerColor   = Color.White,
            title = { Text("Contraseña actualizada", color = Color.Black) },
            text  = { Text("Tu contraseña se cambió exitosamente.", color = Color.Black) },
            confirmButton = {
                TextButton(onClick = { showPasswordSuccessDialog = false }) {
                    Text("Aceptar", color = Color.Black)
                }
            },
        )
    }

    if (showEditDialog) {
    EditProfileDialog(
        nombres = state.profile?.nombres ?: "",
        genero = state.profile?.genero ?: "",
        email = state.profile?.email ?: "",
        telefono = state.profile?.telefono ?: "",
        dni = state.profile?.dni ?: "",
        isUpdating = state.isUpdating,
        onDismiss = { showEditDialog = false },
        onSave = { n, g, e, t, d ->
            viewModel.updateProfile(n, g, e, t, d)
            }
        )
    }
    // 👇👇👇 AQUÍ MISMO VA EL DIALOG DE ÉXITO 👇👇👇
    if (showSuccessDialog) {
        
        Dialog(
            onDismissRequest = { showSuccessDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true,
                usePlatformDefaultWidth = false,   // 🔥 IMPORTANTE
                decorFitsSystemWindows = false
            )
        ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(24.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White,
                    )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "Perfil actualizado",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Tu perfil se actualizó correctamente.",
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = { showSuccessDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                            )
                        ) {
                        Text(
                            "Aceptar",
                            color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary,
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = state.profile?.nombres ?: "",
                style = MaterialTheme.typography.headlineMedium,
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
                                    Text(
                                        "Genero",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                    Text(
                                        state.profile?.genero ?: "Sin genero",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            },
                        leadingContent = {
                                    Icon(
                                        Icons.Default.Face,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(45.dp) // ← forzamos altura fija
                    )
                    ListItem(
                        headlineContent = { 
                            Column {
                                Text(
                                    "Email",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    state.profile?.email ?: "Sin email",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Email, 
                                contentDescription = null,
                                modifier = Modifier.size(20.dp))
                        },
                                modifier = Modifier
                                            .fillMaxWidth()
                                            .height(45.dp) // ← forzamos altura fija
                    )
                    ListItem(
                        headlineContent = { 
                            Column {
                                Text(
                                    "Telefono",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    state.profile?.telefono ?: "Sin telefono",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        leadingContent = {
                            Icon(
                                Icons.Default.Phone, 
                                contentDescription = null,
                                modifier = Modifier.size(20.dp))
                        },
                                modifier = Modifier
                                            .fillMaxWidth()
                                            .height(45.dp) // ← forzamos altura fija
                    )
                    ListItem(
                        headlineContent = {
                            Column {
                                Text(
                                    "DNI",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    state.profile?.dni?.ifBlank { "Sin DNI" } ?: "Sin DNI",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Badge,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp))
                        },
                            modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                    )
                    ListItem(
                        headlineContent = {
                            Column {
                                Text(
                                    "Contraseña",
                                    style = MaterialTheme.typography.labelSmall
                                )
                                Text(
                                    "●●●●●●●●",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        },
                        leadingContent = {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        },
                        trailingContent = {
                            IconButton(onClick = { showChangePasswordDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Cambiar contraseña", modifier = Modifier.size(18.dp))
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    editNombres = state.profile?.nombres ?: ""
                    editGenero = state.profile?.genero ?: ""
                    editEmail = state.profile?.email ?: ""
                    editTelefono = state.profile?.telefono ?: ""
                    editDni = state.profile?.dni ?: ""
                    showEditDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f) // ← ahora ocupa 95% del ancho
                    .height(44.dp),     // ← antes era 50.dp, ahora más compacto
                contentPadding = PaddingValues(
                    vertical = 6.dp,    // ← reduce espacio interno vertical
                    horizontal = 12.dp  // ← reduce espacio horizontal
                )
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 6.dp) // ← menor separación icono-texto
                )
                Text("Editar Perfil")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                    onClick = viewModel::logout,
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .height(44.dp),
                    contentPadding = PaddingValues(
                        vertical = 6.dp,
                        horizontal = 12.dp
                    ),
                    colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    ),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(6.dp)) // ← separación correcta

                Text("Cerrar Sesión")
            }
        }

        if (state.isLoading) {
            LoadingIndicator()
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var newVisible      by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }

    val mismatch = newPassword.isNotBlank() && confirmPassword.isNotBlank() && newPassword != confirmPassword
    val canSave  = newPassword.isNotBlank() && confirmPassword.isNotBlank() && !mismatch && !isLoading

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor   = Color.White,
        title = { Text("Cambiar contraseña", color = Color.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { if (!it.contains('\n') && it.length <= 50) newPassword = it },
                    label = { Text("Nueva contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (newVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    trailingIcon = {
                        IconButton(onClick = { newVisible = !newVisible }) {
                            Icon(if (newVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                        }
                    },
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { if (!it.contains('\n') && it.length <= 50) confirmPassword = it },
                    label = { Text("Confirmar contraseña") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = mismatch,
                    visualTransformation = if (confirmVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    supportingText = if (mismatch) { { Text("Las contraseñas no coinciden", color = Color.Red) } } else null,
                    trailingIcon = {
                        IconButton(onClick = { confirmVisible = !confirmVisible }) {
                            Icon(if (confirmVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
                        }
                    },
                )
                if (isLoading) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally).size(24.dp))
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(newPassword) }, enabled = canSave) {
                Text("Guardar", color = if (canSave) Color.Black else Color.Gray)
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isLoading) onDismiss() }) { Text("Cancelar", color = Color.Black) }
        },
    )
}
