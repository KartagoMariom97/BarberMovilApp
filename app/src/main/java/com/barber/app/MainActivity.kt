package com.barber.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
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
import androidx.compose.runtime.LaunchedEffect
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
import com.barber.app.core.common.Constants
import com.barber.app.core.datastore.UserPreferencesRepository
import com.barber.app.core.navigation.BottomNavBar
import com.barber.app.core.navigation.NavGraph
import com.barber.app.core.navigation.Screen
import com.barber.app.core.network.TokenHolder
import com.barber.app.core.websocket.StompWebSocketManager
import com.barber.app.domain.repository.NotificationRepository
import com.barber.app.presentation.theme.BarberTheme
import com.barber.app.service.NotificationEventManager
import com.barber.app.worker.NotificationHelper
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var tokenHolder: TokenHolder

    @Inject
    lateinit var stompWebSocketManager: StompWebSocketManager

    @Inject
    lateinit var notificationRepository: NotificationRepository

    @Inject
    lateinit var notificationEventManager: NotificationEventManager

    private var startDestination by mutableStateOf<Screen?>(null)

    /** true cuando el 401 de JWT expira — activa diálogo y redirección al login */
    private var isSessionExpired by mutableStateOf(false)

    /** Cantidad de reservas confirmadas pendientes de notificar; 0 = sin evento activo */
    private var confirmedCount by mutableStateOf(0)

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        requestNotificationPermission()

        lifecycleScope.launch {
            val prefs = userPreferencesRepository.userPreferences.first()
            if (prefs.token.isNotEmpty()) {
                tokenHolder.accessToken = prefs.token
            }
            startDestination = when {
                prefs.isLoggedIn && prefs.role in listOf("ADMIN", "BARBER") -> Screen.AdminDashboard
                prefs.isLoggedIn -> Screen.Home
                else -> Screen.Login
            }

            if (prefs.isLoggedIn && prefs.token.isNotEmpty()) {
                stompWebSocketManager.connect(Constants.WS_URL, prefs.token)
                // [FIX] withContext en lugar de launch: así runCatching captura errores
                // de updateFcmToken (red, 401, etc.) y no quedan silenciados.
                // Con launch interno, la excepción escapaba al lifecycleScope padre
                // y el fcm_token quedaba NULL en BD sin ningún aviso.
                runCatching {
                    val fcmToken = FirebaseMessaging.getInstance().token.await()
                    withContext(Dispatchers.IO) { notificationRepository.updateFcmToken(fcmToken) }
                }.onFailure { e ->
                    Log.e("FCM_TOKEN", "Error registrando FCM token en backend: ${e.message}")
                }
            }

            // LOG PARA IDENTIFICAR EL FCM_TOKEN
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    Log.d("FCM_REAL", token)
                }
                .addOnFailureListener {
                    Log.e("FCM_REAL", "Error obteniendo token", it)
                }

            setContent {
                BarberTheme() {
                    startDestination?.let { start ->
                        MainContent(
                            startDestination = start,
                            isSessionExpired = isSessionExpired,
                            onSessionExpiredHandled = {
                                isSessionExpired = false
                                tokenHolder.sessionExpired.value = false
                            },
                            confirmedCount = confirmedCount,
                            onConfirmedBookingHandled = {
                                notificationEventManager.clearConfirmedEvent()
                            },
                        )
                    }
                }
            }
        }

        // Observa expiración de JWT — desconecta WebSocket al expirar sesión
        lifecycleScope.launch {
            tokenHolder.sessionExpired.collect { expired ->
                if (expired) {
                    isSessionExpired = true
                    stompWebSocketManager.disconnect()
                }
            }
        }

        // Colecta notificaciones WebSocket en primer plano
        lifecycleScope.launch {
            stompWebSocketManager.notifications.collect { notification ->
                val title = when (notification.status) {
                    "CONFIRMED"  -> "Reserva Confirmada"
                    "CANCELLED"  -> "Reserva Cancelada"
                    "COMPLETED"  -> "Servicio Completado"
                    "IN_PROGRESS"-> "Servicio en Progreso"
                    else         -> "Actualización de Reserva"
                }
                // Notificación del sistema siempre (para cuando la app está en background)
                NotificationHelper.showBookingStatusUpdate(
                    context = this@MainActivity,
                    bookingId = notification.bookingId,
                    title = title,
                    body = notification.message
                )
                // El dialog de confirmación es emitido por HomeViewModel vía NotificationEventManager
            }
        }

        // Colecta el conteo de confirmaciones desde NotificationEventManager (FCM + HomeViewModel)
        lifecycleScope.launch {
            notificationEventManager.confirmedCount.collect { count ->
                confirmedCount = count
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
private fun MainContent(
    startDestination: Screen,
    isSessionExpired: Boolean = false,
    onSessionExpiredHandled: () -> Unit = {},
    confirmedCount: Int = 0,
    onConfirmedBookingHandled: () -> Unit = {},
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val noBottomBarRoutes = listOf(
        Screen.Login::class.qualifiedName,
        Screen.Register::class.qualifiedName,
        Screen.AdminLogin::class.qualifiedName,
        Screen.AdminDashboard::class.qualifiedName,
        Screen.AdminProfile::class.qualifiedName,
        Screen.AdminBarbers::class.qualifiedName,
        Screen.AdminServices::class.qualifiedName,
        Screen.AdminClients::class.qualifiedName,
        Screen.AdminBookings::class.qualifiedName,
    )
    val showBottomBar = currentRoute != null && currentRoute !in noBottomBarRoutes

    val isOnBooking = currentRoute == Screen.Booking::class.qualifiedName

    var showExitBookingDialog by remember { mutableStateOf(false) }
    var pendingNavigation by remember { mutableStateOf<Screen?>(null) }
    var showSessionExpiredDialog by remember { mutableStateOf(false) }

    // Cuando el JWT expira: navega al login y muestra diálogo de aviso
    LaunchedEffect(isSessionExpired) {
        if (isSessionExpired) {
            navController.navigate(Screen.Login) {
                popUpTo(0) { inclusive = true }
            }
            showSessionExpiredDialog = true
        }
    }

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

        // Diálogo de confirmación para salir del flujo de reserva
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

        // Diálogo que avisa al usuario que su sesión JWT expiró
        if (showSessionExpiredDialog) {
            AlertDialog(
                onDismissRequest = {
                    showSessionExpiredDialog = false
                    onSessionExpiredHandled()
                },
                containerColor = Color.White,
                title = { Text("Sesión expirada", color = Color.Black, fontSize = 18.sp) },
                text = {
                    Text(
                        "Tu sesión ha expiró por inactividad. Por favor, inicia sesión nuevamente para continuar.",
                        color = Color.Black,
                        fontSize = 14.sp,
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showSessionExpiredDialog = false
                        onSessionExpiredHandled()
                    }) {
                        Text("Aceptar", color = Color.Black)
                    }
                },
            )
        }

        // Dialog global de reserva confirmada — aparece en cualquier pantalla
        // Mensaje unificado: centralizado aquí para evitar duplicidad con HomeScreen
        if (confirmedCount > 0) {
            AlertDialog(
                onDismissRequest = onConfirmedBookingHandled,
                containerColor = Color.White,
                title = { Text("¡Reserva Confirmada!", color = Color.Black, fontSize = 18.sp) },
                text = {
                    Text(
                        "Se confirmó tu reserva. Tienes $confirmedCount reserva(s) confirmada(s).",
                        color = Color.Black,
                        fontSize = 14.sp,
                    )
                },
                confirmButton = {
                    TextButton(onClick = onConfirmedBookingHandled) {
                        Text("Aceptar", color = Color.Black)
                    }
                },
            )
        }
    }
}
