package com.barber.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val screen: Screen,
)

val bottomNavItems = listOf(
    BottomNavItem("Inicio", Icons.Default.Home, Screen.Home),
    BottomNavItem("Reservar", Icons.Default.ContentCut, Screen.Booking),
    BottomNavItem("Mis Citas", Icons.Default.CalendarMonth, Screen.Appointments),
    BottomNavItem("Perfil", Icons.Default.Person, Screen.Profile),
)

@Composable
fun BottomNavBar(
    currentRoute: String?,
    onItemClick: (Screen) -> Unit,
    onBeforeNavigate: ((Screen) -> Boolean)? = null,
) {
    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentRoute == item.screen::class.qualifiedName
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (onBeforeNavigate == null || onBeforeNavigate(item.screen)) {
                        onItemClick(item.screen)
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
