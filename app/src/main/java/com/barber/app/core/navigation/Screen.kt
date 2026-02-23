package com.barber.app.core.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {
    @Serializable data object Login : Screen()
    @Serializable data object Register : Screen()
    @Serializable data object Home : Screen()
    @Serializable data object Booking : Screen()
    @Serializable data object Appointments : Screen()
    @Serializable data object Profile : Screen()

    // Admin
    @Serializable data object AdminDashboard : Screen()
    @Serializable data object AdminBarbers : Screen()
    @Serializable data object AdminServices : Screen()
    @Serializable data object AdminClients : Screen()
    @Serializable data object AdminBookings : Screen()
}
