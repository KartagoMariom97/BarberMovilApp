package com.barber.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
    // [CAMBIO] containerColor blanco para toda la barra
    NavigationBar(containerColor = Color.White) {
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
                colors = NavigationBarItemDefaults.colors(
                    // [CAMBIO] indicador de selección negro absoluto
                    indicatorColor = Color.Black,
                    // [CAMBIO] icono blanco cuando el ítem está seleccionado (contraste sobre indicador negro)
                    selectedIconColor = Color.White,
                    // [CAMBIO] icono negro absoluto cuando el ítem NO está seleccionado
                    unselectedIconColor = Color.Black,
                    // [CAMBIO] etiqueta de texto siempre negra (seleccionado y no seleccionado)
                    selectedTextColor = Color.Black,
                    unselectedTextColor = Color.Black,
                ),
            )
        }
    }
}
