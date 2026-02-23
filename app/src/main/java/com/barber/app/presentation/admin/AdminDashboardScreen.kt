package com.barber.app.presentation.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBarbers: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToClients: () -> Unit,
    onNavigateToBookings: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel Admin") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                Icons.Default.ContentCut,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("BarberApp Admin", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Selecciona una sección para administrar",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(40.dp))

            AdminMenuItem(
                label = "Barberos",
                icon = { Icon(Icons.Default.Person, contentDescription = null) },
                onClick = onNavigateToBarbers,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdminMenuItem(
                label = "Servicios",
                icon = { Icon(Icons.Default.ContentCut, contentDescription = null) },
                onClick = onNavigateToServices,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdminMenuItem(
                label = "Clientes",
                icon = { Icon(Icons.Default.Group, contentDescription = null) },
                onClick = onNavigateToClients,
            )
            Spacer(modifier = Modifier.height(12.dp))
            AdminMenuItem(
                label = "Reservas",
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                onClick = onNavigateToBookings,
            )
        }
    }
}

@Composable
private fun AdminMenuItem(
    label: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
    ) {
        icon()
        Spacer(modifier = Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.bodyLarge)
    }
}
