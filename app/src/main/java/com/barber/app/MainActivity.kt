package com.barber.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.core.navigation.BottomNavBar
import com.barber.app.core.navigation.NavGraph
import com.barber.app.core.navigation.Screen
import com.barber.app.presentation.theme.BarberTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    private var startDestination by mutableStateOf<Screen?>(null)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()

        lifecycleScope.launch {
            val prefs = userPreferencesRepository.userPreferences.first()
            startDestination = if (prefs.isLoggedIn) Screen.Home else Screen.Login

            setContent {
                BarberTheme() {
                    startDestination?.let { start ->
                        MainContent(startDestination = start)
                    }
                }
            }
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
private fun MainContent(startDestination: Screen) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val authRoutes = listOf(
        Screen.Login::class.qualifiedName,
        Screen.Register::class.qualifiedName,
    )
    val showBottomBar = currentRoute != null && currentRoute !in authRoutes

    val isOnBooking = currentRoute == Screen.Booking::class.qualifiedName

    var showExitBookingDialog by remember { mutableStateOf(false) }
    var pendingNavigation by remember { mutableStateOf<Screen?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    BottomNavBar(
                        currentRoute = currentRoute,
                        onItemClick = { screen ->
                            navController.navigate(screen) {
                                popUpTo(Screen.Home) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        onBeforeNavigate = { screen ->
                            if (isOnBooking && screen != Screen.Booking) {
                                pendingNavigation = screen
                                showExitBookingDialog = true
                                false
                            } else {
                                true
                            }
                        },
                    )
                }
            }
        ) { padding ->
            NavGraph(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(padding),
            )
        }

        if (showExitBookingDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .zIndex(10f),
            )
            AlertDialog(
                onDismissRequest = {
                    showExitBookingDialog = false
                    pendingNavigation = null
                },
                containerColor = Color.White,
                title = {
                    Text(
                        "Cancelar reserva",
                        color = Color.Black,
                        fontSize = 18.sp,
                    )
                },
                text = {
                    Text(
                        "Se perderá el progreso de tu reserva.",
                        color = Color.Black,
                        fontSize = 14.sp,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showExitBookingDialog = false
                        pendingNavigation?.let { screen ->
                            navController.navigate(screen) {
                                popUpTo(Screen.Home) {
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                        pendingNavigation = null
                    }) {
                        Text("Salir", color = Color(0xFFE53935))
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showExitBookingDialog = false
                        pendingNavigation = null
                    }) {
                        Text("Continuar", color = Color.Black)
                    }
                },
            )
        }
    }
}
