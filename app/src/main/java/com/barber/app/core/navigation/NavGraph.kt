package com.barber.app.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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
    }
}
