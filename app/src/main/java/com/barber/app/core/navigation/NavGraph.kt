package com.barber.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.barber.app.presentation.admin.AdminBookingsScreen
import com.barber.app.presentation.admin.AdminBarbersScreen
import com.barber.app.presentation.admin.AdminClientsScreen
import com.barber.app.presentation.admin.AdminDashboardScreen
import com.barber.app.presentation.admin.AdminLoginScreen
import com.barber.app.presentation.admin.AdminProfileScreen
import com.barber.app.presentation.admin.AdminServicesScreen
import com.barber.app.presentation.appointments.AppointmentsScreen
import com.barber.app.presentation.auth.LoginScreen
import com.barber.app.presentation.auth.RegisterScreen
import com.barber.app.presentation.booking.BookingScreen
import com.barber.app.presentation.home.HomeScreen
import com.barber.app.presentation.profile.ProfileScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    startDestination: Screen,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {
        composable<Screen.Login> {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminLogin)
                },
            )
        }

        composable<Screen.Register> {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Login) { inclusive = true }
                    }
                },
            )
        }

        composable<Screen.Home> {
            HomeScreen(
                onNavigateToBooking = {
                    navController.navigate(Screen.Booking)
                },
                onNavigateToAppointments = {
                    navController.navigate(Screen.Appointments)
                },
            )
        }

        composable<Screen.Booking> {
            BookingScreen(
                onBookingSuccess = {
                    navController.navigate(Screen.Home) {
                        popUpTo(Screen.Home) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Screen.Appointments> {
            AppointmentsScreen(
                onNavigateToBooking = {
                    navController.navigate(Screen.Booking)
                },
            )
        }

        composable<Screen.Profile> {
            ProfileScreen(
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        // ─── Admin ────────────────────────────────────────────────────────────────
        composable<Screen.AdminLogin> {
            AdminLoginScreen(
                onLoginSuccess = { nombres ->
                    navController.navigate(Screen.AdminDashboard) {
                        popUpTo(Screen.AdminLogin) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() },
            )
        }

        composable<Screen.AdminDashboard> {
            AdminDashboardScreen(
                onNavigateToBarbers  = { navController.navigate(Screen.AdminBarbers) },
                onNavigateToServices = { navController.navigate(Screen.AdminServices) },
                onNavigateToClients  = { navController.navigate(Screen.AdminClients) },
                onNavigateToBookings = { navController.navigate(Screen.AdminBookings) },
                onNavigateToProfile  = { navController.navigate(Screen.AdminProfile) },
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable<Screen.AdminProfile> {
            AdminProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate(Screen.Login) {
                        popUpTo(0) { inclusive = true }
                    }
                },
            )
        }

        composable<Screen.AdminBarbers> {
            AdminBarbersScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<Screen.AdminServices> {
            AdminServicesScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<Screen.AdminClients> {
            AdminClientsScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable<Screen.AdminBookings> {
            AdminBookingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
