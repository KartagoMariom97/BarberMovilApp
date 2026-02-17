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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var showEditDialog by remember { mutableStateOf(false) }
    var editNombres by remember { mutableStateOf("") }
    var editEmail by remember { mutableStateOf("") }
    var editTelefono by remember { mutableStateOf("") }
    var editDni by remember { mutableStateOf("") }

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            containerColor = Color.White,
            title = { Text("Editar Perfil", color = Color.Black) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editNombres,
                        onValueChange = { editNombres = it },
                        label = { Text("Nombres") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editEmail,
                        onValueChange = { editEmail = it },
                        label = { Text("E-mail") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editTelefono,
                        onValueChange = { editTelefono = it },
                        label = { Text("Teléfono") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = editDni,
                        onValueChange = { editDni = it },
                        label = { Text("DNI") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateProfile(editNombres, editEmail, editTelefono, editDni)
                    showEditDialog = false
                }) { Text("Guardar", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Cancelar", color = Color.Black) }
            },
        )
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
                        headlineContent = { Text(state.profile?.email ?: "Sin email") },
                        supportingContent = { Text("E-mail") },
                        leadingContent = {
                            Icon(Icons.Default.Email, contentDescription = null)
                        },
                    )
                    ListItem(
                        headlineContent = { Text(state.profile?.telefono ?: "Sin teléfono") },
                        supportingContent = { Text("Teléfono") },
                        leadingContent = {
                            Icon(Icons.Default.Phone, contentDescription = null)
                        },
                    )
                    ListItem(
                        headlineContent = { Text(state.profile?.dni?.ifBlank { "Sin DNI" } ?: "Sin DNI") },
                        supportingContent = { Text("DNI") },
                        leadingContent = {
                            Icon(Icons.Default.Badge, contentDescription = null)
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    editNombres = state.profile?.nombres ?: ""
                    editEmail = state.profile?.email ?: ""
                    editTelefono = state.profile?.telefono ?: ""
                    editDni = state.profile?.dni ?: ""
                    showEditDialog = true
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
            ) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp),
                )
                Text("Editar Perfil")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = viewModel::logout,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                ),
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Text("  Cerrar Sesión")
            }
        }

        if (state.isLoading) {
            LoadingIndicator()
        }
    }
}
