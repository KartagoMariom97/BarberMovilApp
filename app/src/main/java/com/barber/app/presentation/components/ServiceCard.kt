package com.barber.app.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.barber.app.domain.model.Service

@Composable
fun ServiceCard(
    service: Service,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onShowInfo: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onToggle,
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
            Column(
                modifier = Modifier.weight(1f).padding(start = 6.dp),
            ) {
                Text(service.name, style = MaterialTheme.typography.titleLarge)
                Text(
                    service.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

            }
            if (onShowInfo != null) {
                IconButton(onClick = onShowInfo, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Info, "Info", tint = Color(0xFF9E9E9E), modifier = Modifier.size(18.dp))
                }
            }
            Text(
                "S/ ${service.price}",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 4.dp),
            )
        }
    }
}

@Composable
fun ServiceDetailContent(service: Service) {
    val darkText = Color.Black.copy(alpha = 0.8f)
    Text(service.name, style = MaterialTheme.typography.titleMedium, color = Color.Black)
    Spacer(modifier = Modifier.height(4.dp))
    Text(service.description, style = MaterialTheme.typography.bodyMedium, color = darkText)
    Spacer(modifier = Modifier.height(4.dp))
    Text("Duración: ${service.estimatedMinutes} minutos", style = MaterialTheme.typography.bodyMedium, color = darkText)
    Text("Precio: S/ ${service.price}", style = MaterialTheme.typography.bodyMedium, color = darkText)
    Spacer(modifier = Modifier.height(6.dp))
    Text(
        "Imagen del servicio próximamente",
        style = MaterialTheme.typography.labelSmall,
        color = Color.Black.copy(alpha = 0.4f),
    )
}
