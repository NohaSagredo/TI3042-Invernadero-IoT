package com.example.appinvernaderoautonomo.ui.navegacion

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.appinvernaderoautonomo.data.GreenhouseRepository
import com.example.appinvernaderoautonomo.iot.GreenhouseIotManager
import com.example.appinvernaderoautonomo.security.SessionManager
import com.example.appinvernaderoautonomo.ui.audit.AuditoriaScreen
import com.example.appinvernaderoautonomo.ui.components.FarmPastelBackground
import com.example.appinvernaderoautonomo.ui.configuracion.PantallaConfiguracion
import com.example.appinvernaderoautonomo.ui.dashboard.DashboardScreen
import com.example.appinvernaderoautonomo.ui.history.HistorialScreen
import com.example.appinvernaderoautonomo.ui.login.LoginScreen
import com.example.appinvernaderoautonomo.ui.perfil.PantallaPerfil

@Composable
fun NavegacionApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val repository = GreenhouseRepository.getInstance(context)
    val iotManager = GreenhouseIotManager.getInstance(context)

    val currentUser by SessionManager.currentUser.collectAsState()
    val isSessionExpired by SessionManager.isSessionExpired.collectAsState()
    val iotState by iotManager.state.collectAsState()

    // Observador activo de expiración de sesión por inactividad prolongada (15 min)
    androidx.compose.runtime.LaunchedEffect(isSessionExpired) {
        if (isSessionExpired) {
            navController.navigate("login") {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // Fondo vectorial animado de granja, cielo celeste y campo de girasoles
        FarmPastelBackground(
            modifier = Modifier.fillMaxSize(),
            mostrarCapaFrosted = true
        )

        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.fillMaxSize()
        ) {
            // Pantalla 1: Login Obligatorio con Protección Fail-Safe
            composable("login") {
                LoginScreen(
                    repository = repository,
                    onLoginSuccess = { user ->
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                )
            }

            // Pantalla 2: Dashboard Principal Filtrado por Rol (RBAC)
            composable("dashboard") {
                val user = currentUser
                if (user == null) {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    DashboardScreen(
                        state = iotState,
                        currentUser = user,
                        iotManager = iotManager,
                        onIrAHistorial = { navController.navigate("historial") },
                        onIrAAuditoria = { navController.navigate("auditoria") },
                        onIrAPerfil = { navController.navigate("perfil") },
                        onIrAConfiguracion = { navController.navigate("configuracion") },
                        onCerrarSesion = {
                            SessionManager.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Pantalla 3: Perfil del Operador Adaptado
            composable("perfil") {
                val user = currentUser
                if (user == null) {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                } else {
                    PantallaPerfil(
                        currentUser = user,
                        onIrAConfiguracion = { navController.navigate("configuracion") },
                        onVolver = { navController.popBackStack() },
                        onCerrarSesion = {
                            SessionManager.logout()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }

            // Pantalla 4: Ajustes del Invernadero Adaptados
            composable("configuracion") {
                PantallaConfiguracion(
                    onVolverAlPerfil = { navController.popBackStack() }
                )
            }

            // Pantalla 5: Historial Cronométrico de Telemetría (Cada 5 min)
            composable("historial") {
                HistorialScreen(
                    repository = repository,
                    onVolver = { navController.popBackStack() }
                )
            }

            // Pantalla 6: Bitácora de Auditoría
            composable("auditoria") {
                val user = currentUser
                if (user == null || !user.role.canViewAuditLogs()) {
                    navController.popBackStack()
                } else {
                    AuditoriaScreen(
                        repository = repository,
                        onVolver = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
