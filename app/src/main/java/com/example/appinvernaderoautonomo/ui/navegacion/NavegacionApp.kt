package com.example.appinvernaderoautonomo.ui.navegacion

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
import com.example.appinvernaderoautonomo.ui.dashboard.DashboardScreen
import com.example.appinvernaderoautonomo.ui.history.HistorialScreen
import com.example.appinvernaderoautonomo.ui.login.LoginScreen

@Composable
fun NavegacionApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val repository = GreenhouseRepository.getInstance(context)
    val iotManager = GreenhouseIotManager.getInstance(context)

    val currentUser by SessionManager.currentUser.collectAsState()
    val iotState by iotManager.state.collectAsState()

    NavHost(
        navController = navController,
        startDestination = "login",
        modifier = modifier
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
                // Redirección de seguridad si no hay sesión activa
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
                    onCerrarSesion = {
                        SessionManager.logout()
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }

        // Pantalla 3: Historial Cronométrico de Telemetría (Cada 5 min)
        composable("historial") {
            HistorialScreen(
                repository = repository,
                onVolver = { navController.popBackStack() }
            )
        }

        // Pantalla 4: Bitácora de Auditoría de Ciberseguridad ISO 27400
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
