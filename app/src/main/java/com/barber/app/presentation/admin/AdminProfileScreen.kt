package com.barber.app.presentation.admin

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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

            Spacer(modifier = Modifier.height(40.dp))
            // Botón de cerrar sesión removido — usar el del Panel Admin
        }
    }
}
