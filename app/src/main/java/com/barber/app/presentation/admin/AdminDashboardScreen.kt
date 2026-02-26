package com.barber.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateToBarbers: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToBookings: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit,
    viewModel: AdminDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.isLoggedOut) {
        if (uiState.isLoggedOut) onLogout()
    }

    // AlertDialog con conteo de reservas pendientes al abrir el dashboard
    if (uiState.showPendingAlert) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissPendingAlert() },
            containerColor = Color.White,
            title = { Text("Reservas Pendientes", color = Color.Black) },
            text = {
                Text(
                    "Tienes ${uiState.pendingCount} reserva(s) pendiente(s) de atención.",
                    color = Color.Black,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.dismissPendingAlert()
                    onNavigateToBookings()
                }) { Text("Ver Reservas", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissPendingAlert() }) {
                    Text("Más tarde", color = Color.Black)
                }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Admin") },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.ContentCut,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Black,
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text("BarberApp Admin", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(4.dp))

            if (uiState.nombres.isNotBlank()) {
                Text(
                    text = "Bienvenido, ${uiState.nombres}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = "Selecciona una sección para administrar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(36.dp))

            AdminMenuItem(
                label = "Barberos",
                icon = Icons.Default.Person,
                onClick = onNavigateToBarbers,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdminMenuItem(
                label = "Servicios",
                icon = Icons.Default.ContentCut,
                onClick = onNavigateToServices,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdminMenuItem(
                label = "Clientes",
                icon = Icons.Default.Group,
                onClick = onNavigateToClients,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdminMenuItem(
                label = "Reservas",
                icon = Icons.Default.CalendarMonth,
                onClick = onNavigateToBookings,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile button
            Button(
                onClick = onNavigateToProfile,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF333333),
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Mi Perfil", style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Logout button
            Button(
                onClick = viewModel::logout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = Color.White,
                ),
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}

@Composable
private fun AdminMenuItem(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White,
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            // Texto centrado en los botones del menú admin
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
