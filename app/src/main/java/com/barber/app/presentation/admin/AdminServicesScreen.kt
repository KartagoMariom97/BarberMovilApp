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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.barber.app.domain.model.Service
import com.barber.app.presentation.components.ErrorOverlay
import com.barber.app.presentation.components.LoadingIndicator
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminServicesScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminServicesViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editingService by remember { mutableStateOf<Service?>(null) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var deletingServiceId by remember { mutableStateOf<Long?>(null) }

    LaunchedEffect(state.successMessage) {
        if (state.successMessage != null) viewModel.clearSuccess()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Servicios") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo servicio")
            }
        },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (state.isLoading) {
                LoadingIndicator()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(state.services) { service ->
                        ServiceAdminCard(
                            service = service,
                            onEdit = { editingService = service },
                            onDelete = { deletingServiceId = service.id },
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }

            ErrorOverlay(
                message = state.error ?: "",
                visible = state.error != null,
                onDismiss = viewModel::clearError,
            )
        }
    }

    if (showCreateDialog) {
        ServiceFormDialog(
            title = "Nuevo Servicio",
            onDismiss = { showCreateDialog = false },
            onSave = { name, description, minutes, price ->
                viewModel.createService(
                    name,
                    description,
                    minutes.toIntOrNull() ?: 0,
                    price.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                )
                showCreateDialog = false
            },
        )
    }

    editingService?.let { service ->
        ServiceFormDialog(
            title = "Editar Servicio",
            initialName = service.name,
            initialDescription = service.description,
            initialMinutes = service.estimatedMinutes.toString(),
            initialPrice = service.price.toPlainString(),
            onDismiss = { editingService = null },
            onSave = { name, description, minutes, price ->
                viewModel.updateService(
                    service.id,
                    name.takeIf { it.isNotBlank() },
                    description,
                    minutes.toIntOrNull(),
                    price.toBigDecimalOrNull(),
                )
                editingService = null
            },
        )
    }

    deletingServiceId?.let { id ->
        AlertDialog(
            onDismissRequest = { deletingServiceId = null },
            containerColor = Color.White,
            title = { Text("Eliminar servicio", color = Color.Black) },
            text = { Text("¿Estás seguro de que deseas eliminar este servicio?", color = Color.Black) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteService(id)
                    deletingServiceId = null
                }) { Text("Eliminar", color = Color(0xFFE53935)) }
            },
            dismissButton = {
                TextButton(onClick = { deletingServiceId = null }) { Text("Cancelar", color = Color.Black) }
            },
        )
    }
}

@Composable
private fun ServiceAdminCard(
    service: Service,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
                Text(service.name, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFFFFC107))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color(0xFFE53935))
                }
            }
            if (service.description.isNotBlank()) {
                Text(service.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("${service.estimatedMinutes} min", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(20.dp))

            Text("S/ ${service.price}", style = MaterialTheme.typography.bodySmall, color = Color.Black)
            
        }
    }
}

@Composable
private fun ServiceFormDialog(
    title: String,
    initialName: String = "",
    initialDescription: String = "",
    initialMinutes: String = "",
    initialPrice: String = "",
    onDismiss: () -> Unit,
    onSave: (name: String, description: String?, minutes: String, price: String) -> Unit,
) {
    var name        by remember { mutableStateOf(initialName) }
    var description by remember { mutableStateOf(initialDescription) }
    var minutes     by remember { mutableStateOf(initialMinutes) }
    var price       by remember { mutableStateOf(initialPrice) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(title, color = Color.Black) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nombre") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descripción") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = minutes, onValueChange = { minutes = it }, label = { Text("Minutos estimados") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Precio (S/)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, description.takeIf { it.isNotBlank() }, minutes, price) },
                enabled = name.isNotBlank() && minutes.isNotBlank() && price.isNotBlank(),
            ) { Text("Guardar", color = Color.Black) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = Color.Black) }
        },
    )
}
