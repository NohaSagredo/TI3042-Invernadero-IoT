package com.example.appinvernaderoautonomo.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.Bluetooth
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Science
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Sensors
import androidx.compose.material.icons.rounded.SettingsRemote
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.appinvernaderoautonomo.data.GreenhouseRepository
import com.example.appinvernaderoautonomo.iot.GreenhouseIotManager
import com.example.appinvernaderoautonomo.model.ConnectionMode
import com.example.appinvernaderoautonomo.model.EstadoSuelo
import com.example.appinvernaderoautonomo.model.InvernaderoState
import com.example.appinvernaderoautonomo.model.PlantProfile
import com.example.appinvernaderoautonomo.model.Role
import com.example.appinvernaderoautonomo.model.User
import com.example.appinvernaderoautonomo.ui.components.CompuertaVentanaVisualizer
import com.example.appinvernaderoautonomo.ui.components.InvernaderoChamberVisualizer
import com.example.appinvernaderoautonomo.ui.components.PastelArcGauge
import com.example.appinvernaderoautonomo.ui.components.PastelOdometerText
import com.example.appinvernaderoautonomo.ui.components.VentiladorMosfetVisualizer
import com.example.appinvernaderoautonomo.ui.components.pastelBounceClickable
import com.example.appinvernaderoautonomo.ui.history.HistorialScreen
import com.example.appinvernaderoautonomo.ui.thresholds.ThresholdDialog

// Paleta cromática intencional en tonos pastel (Granja, Cielo Celeste y Girasoles)
private val ColorFondo = Color(0x22F7FBF8) // Semitransparente para dejar lucir el fondo animado
private val ColorVerdePrimario = Color(0xFF4CAF50)
private val ColorVerdeOscuro = Color(0xFF1B4D28)
private val ColorVerdeSuave = Color(0xFFE8F5E9)
private val ColorVerdeBorde = Color(0xFFC8E6C9)
private val ColorTextoPrincipal = Color(0xFF142C1D)
private val ColorTextoSecundario = Color(0xFF43604E)
private val ColorBordeCard = Color(0xFFD8E8DE)

enum class DashboardTab(
    val titulo: String,
    val icono: ImageVector
) {
    CULTIVO("Cultivo", Icons.Rounded.Eco),
    ACTUADORES("Actuadores", Icons.Rounded.Tune),
    CONEXION("Conexión", Icons.Rounded.Sensors),
    HISTORIAL("Historial", Icons.Rounded.History)
}

@Composable
fun DashboardScreen(
    state: InvernaderoState,
    currentUser: User,
    iotManager: GreenhouseIotManager,
    onIrAHistorial: () -> Unit,
    onIrAAuditoria: () -> Unit,
    onCerrarSesion: () -> Unit,
    onIrAPerfil: () -> Unit = {},
    onIrAConfiguracion: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(DashboardTab.CULTIVO) }
    var mostrarDialogoUmbrales by remember { mutableStateOf(false) }
    var mostrarDialogoIp by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val repository = remember { GreenhouseRepository.getInstance(context) }

    if (mostrarDialogoIp) {
        ConfiguracionIpDialog(
            ipActual = state.ipNodoEsp32,
            onDismiss = { mostrarDialogoIp = false },
            onGuardar = { nuevaIp ->
                iotManager.actualizarIpEsp32(nuevaIp, currentUser)
                mostrarDialogoIp = false
            },
            onTestPing = { ip -> iotManager.pingEsp32(ip) }
        )
    }

    if (mostrarDialogoUmbrales) {
        ThresholdDialog(
            profile = state.rutinaActiva,
            onDismiss = { mostrarDialogoUmbrales = false },
            onConfirm = { updated ->
                iotManager.calibrarUmbrales(updated, currentUser)
                mostrarDialogoUmbrales = false
            }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NavigationBar(
                containerColor = Color(0xF7FFFFFF),
                tonalElevation = 2.dp,
                modifier = Modifier.border(BorderStroke(1.dp, ColorBordeCard))
            ) {
                DashboardTab.values().forEach { tab ->
                    val seleccionado = selectedTab == tab
                    NavigationBarItem(
                        selected = seleccionado,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icono,
                                contentDescription = tab.titulo
                            )
                        },
                        label = {
                            Text(
                                text = tab.titulo,
                                fontSize = 11.sp,
                                fontWeight = if (seleccionado) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = ColorVerdeOscuro,
                            selectedTextColor = ColorVerdeOscuro,
                            indicatorColor = Color(0xFFD8F3DC),
                            unselectedIconColor = ColorTextoSecundario,
                            unselectedTextColor = ColorTextoSecundario
                        )
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                DashboardTab.CULTIVO -> TabCultivo(
                    state = state,
                    currentUser = currentUser,
                    iotManager = iotManager,
                    onIrAPerfil = onIrAPerfil,
                    onAbrirDialogoIp = { mostrarDialogoIp = true },
                    onCerrarSesion = onCerrarSesion
                )
                DashboardTab.ACTUADORES -> TabActuadores(
                    state = state,
                    currentUser = currentUser,
                    iotManager = iotManager,
                    onAbrirCalibracion = { mostrarDialogoUmbrales = true }
                )
                DashboardTab.CONEXION -> TabConexion(
                    state = state,
                    currentUser = currentUser,
                    iotManager = iotManager,
                    onAbrirDialogoIp = { mostrarDialogoIp = true }
                )
                DashboardTab.HISTORIAL -> TabHistorial(
                    repository = repository,
                    currentUser = currentUser,
                    onIrAAuditoria = onIrAAuditoria
                )
            }
        }
    }
}

@Composable
private fun TabCultivo(
    state: InvernaderoState,
    currentUser: User,
    iotManager: GreenhouseIotManager,
    onIrAPerfil: () -> Unit,
    onAbrirDialogoIp: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HeaderUsuarioRBAC(
            user = currentUser,
            estaConectado = state.estaConectado,
            ipNodo = state.ipNodoEsp32,
            onConfigurarIp = onAbrirDialogoIp,
            onSimularConexion = { iotManager.alternarSimulacionFalloConexion(currentUser) },
            onIrAPerfil = onIrAPerfil,
            onCerrarSesion = onCerrarSesion
        )

        if (!state.estaConectado || state.modoFailSafeBorde) {
            BannerFailSafeBorde(
                perfil = state.rutinaActiva,
                onReconectar = { iotManager.alternarSimulacionFalloConexion(currentUser) }
            )
        }

        if (state.alertaSeguridad != null) {
            BannerAlertaAgronomica(mensaje = state.alertaSeguridad)
        }

        if (state.hayModoManualActivo) {
            BannerModoManual(
                puedeResetear = currentUser.role.canOverrideActuators(),
                onReactivarAuto = { iotManager.reactivarModoAuto(currentUser) }
            )
        }

        if (currentUser.role.isReadOnly()) {
            BannerPantallaTerreno()
        }

        EncabezadoSeccion(
            titulo = "TELEMETRÍA AMBIENTAL EN VIVO",
            subtitulo = "Sensores DHT22 (Clima) y FC-28 (Sustrato del suelo)",
            icono = Icons.Rounded.Sensors
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TarjetaSensor(
                titulo = "TEMPERATURA",
                valor = "${String.format("%.1f", state.temperatura)} °C",
                valorNum = state.temperatura,
                minVal = 10f,
                maxVal = 40f,
                unidad = "°C",
                rangoEsperado = state.rutinaActiva.tempRangeText,
                estaEnRango = state.estaEnRangoTemperatura,
                icono = Icons.Rounded.Thermostat,
                colorIcono = if (state.estaEnRangoTemperatura) Color(0xFF4CAF50) else Color(0xFFF87171),
                modifier = Modifier.weight(1f)
            )

            TarjetaSensor(
                titulo = "HUMEDAD AIRE",
                valor = "${String.format("%.1f", state.humedadAmbiente)} %",
                valorNum = state.humedadAmbiente,
                minVal = 20f,
                maxVal = 100f,
                unidad = "% HR",
                rangoEsperado = state.rutinaActiva.humidityRangeText,
                estaEnRango = state.estaEnRangoHumedad,
                icono = Icons.Rounded.WaterDrop,
                colorIcono = if (state.estaEnRangoHumedad) Color(0xFF38BDF8) else Color(0xFFF87171),
                modifier = Modifier.weight(1f)
            )
        }

        TarjetaHumedadSuelo(
            raw = state.humedadSueloRaw,
            estado = state.estadoSuelo,
            rangoEsperado = state.rutinaActiva.soilRangeText,
            estaEnRango = state.estaEnRangoSuelo
        )

        EncabezadoSeccion(
            titulo = "CÁMARA DE POLICARBONATO EN TIEMPO REAL",
            subtitulo = "Compuerta lateral batiente y extractor de ventilación cruzada",
            icono = Icons.Rounded.Eco
        )

        InvernaderoChamberVisualizer(
            anguloCompuerta = state.anguloVentana,
            velocidadFanPwm = state.velocidadVentiladorPwm,
            humedadSueloRaw = state.humedadSueloRaw,
            temperatura = state.temperatura,
            modifier = Modifier.fillMaxWidth()
        )

        TarjetaEstadoEnlace(mensaje = state.ultimoMensaje, estaConectado = state.estaConectado)
        Spacer(modifier = Modifier.height(44.dp))
    }
}

@Composable
private fun TabActuadores(
    state: InvernaderoState,
    currentUser: User,
    iotManager: GreenhouseIotManager,
    onAbrirCalibracion: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EncabezadoSeccion(
            titulo = "VENTILACIÓN Y CLIMATIZACIÓN",
            subtitulo = "Control de compuerta batiente y extractor 12V",
            icono = Icons.Rounded.Air
        )

        TarjetaCompuertaServo(
            anguloActual = state.anguloVentana,
            porcentajeAbierto = state.porcentajeVentanaAbierta,
            esModoManual = state.modoManualVentana,
            puedeControlar = currentUser.role.canOverrideActuators(),
            onAbrirCompuerta = { iotManager.setManualVentana(90, currentUser) },
            onCerrarCompuerta = { iotManager.setManualVentana(0, currentUser) },
            onPosicionMedia = { iotManager.setManualVentana(45, currentUser) }
        )

        TarjetaVentiladorMosfet(
            pwmActual = state.velocidadVentiladorPwm,
            porcentaje = state.porcentajeVentilador,
            esModoManual = state.modoManualVentilador,
            puedeControlar = currentUser.role.canOverrideActuators(),
            onSubirVelocidad = { iotManager.setManualVentilador(state.velocidadVentiladorPwm + 40, currentUser) },
            onBajarVelocidad = { iotManager.setManualVentilador(state.velocidadVentiladorPwm - 40, currentUser) },
            onMaximo = { iotManager.setManualVentilador(255, currentUser) },
            onApagar = { iotManager.setManualVentilador(0, currentUser) }
        )

        EncabezadoSeccion(
            titulo = "PERFILES DE CULTIVO",
            subtitulo = "Límites ambientales y calibración agronómica",
            icono = Icons.Rounded.Eco
        )

        TarjetaRutinaCultivo(
            perfilActivo = state.rutinaActiva,
            userRole = currentUser.role,
            onSeleccionarRutina = { perfil -> iotManager.cambiarRutina(perfil, currentUser) },
            onAbrirCalibracion = onAbrirCalibracion
        )

        TarjetaEstadoEnlace(mensaje = state.ultimoMensaje, estaConectado = state.estaConectado)
        Spacer(modifier = Modifier.height(44.dp))
    }
}

@Composable
private fun TabConexion(
    state: InvernaderoState,
    currentUser: User,
    iotManager: GreenhouseIotManager,
    onAbrirDialogoIp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ColorFondo)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        EncabezadoSeccion(
            titulo = "MODO DE COMUNICACIÓN",
            subtitulo = "Selector tri-modal (Simulación, Wi-Fi local o Bluetooth)",
            icono = Icons.Rounded.SettingsRemote
        )

        TarjetaSelectorModoConexion(
            modoActual = state.modoConexion,
            puedeCambiarModo = !currentUser.role.isReadOnly(),
            onCambiarModo = { nuevoModo ->
                if (!currentUser.role.isReadOnly()) {
                    iotManager.cambiarModoConexion(nuevoModo, currentUser)
                }
            }
        )

        TarjetaDetalleModoActivo(
            state = state,
            currentUser = currentUser,
            iotManager = iotManager,
            onAbrirDialogoIp = onAbrirDialogoIp
        )

        EncabezadoSeccion(
            titulo = "CENTRO DE NOTIFICACIONES PUSH",
            subtitulo = "Pruebas de alerta acústica y contingencia en tiempo real",
            icono = Icons.Rounded.NotificationsActive
        )

        TarjetaCentroNotificacionesPush(
            onEnviarPrueba = { iotManager.enviarNotificacionPrueba(currentUser) },
            puedeEnviar = !currentUser.role.isReadOnly()
        )

        TarjetaEstadoEnlace(mensaje = state.ultimoMensaje, estaConectado = state.estaConectado)
        Spacer(modifier = Modifier.height(44.dp))
    }
}

@Composable
private fun TabHistorial(
    repository: GreenhouseRepository,
    currentUser: User,
    onIrAAuditoria: () -> Unit
) {
    HistorialScreen(
        repository = repository,
        onVolver = null,
        onIrAAuditoria = if (currentUser.role.canViewAuditLogs()) onIrAAuditoria else null,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun EncabezadoSeccion(
    titulo: String,
    subtitulo: String,
    icono: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .background(ColorVerdeSuave, shape = RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icono,
                contentDescription = null,
                tint = ColorVerdePrimario,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = titulo,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ColorVerdeOscuro,
                letterSpacing = 0.5.sp
            )
            Text(
                text = subtitulo,
                fontSize = 11.sp,
                color = ColorTextoSecundario
            )
        }
    }
}

@Composable
private fun HeaderUsuarioRBAC(
    user: User,
    estaConectado: Boolean,
    ipNodo: String,
    onConfigurarIp: () -> Unit,
    onSimularConexion: () -> Unit,
    onIrAPerfil: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ColorBordeCard)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onIrAPerfil() }
            ) {
                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextoPrincipal
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(user.role.badgeColorHex)
                    ) {
                        Text(
                            text = user.role.displayTitle.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = onConfigurarIp,
                        enabled = user.role == Role.ADMIN,
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F2),
                        border = CardDefaults.outlinedCardBorder().copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFDCE5DF))
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ipNodo,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF2C3E50)
                            )
                            if (user.role == Role.ADMIN) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = "Editar IP",
                                    tint = ColorVerdePrimario,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón Perfil de Usuario
                IconButton(
                    onClick = onIrAPerfil,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFE8F5E9), shape = CircleShape)
                        .border(1.dp, Color(0xFFC8E6C9), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Person,
                        contentDescription = "Perfil del Operador",
                        tint = ColorVerdeOscuro,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Botón Wi-Fi (Simulador / Estado de enlace)
                IconButton(
                    onClick = onSimularConexion,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (estaConectado) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = if (estaConectado) Color(0xFFA5D6A7) else Color(0xFFFFCDD2),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = if (estaConectado) Icons.Rounded.Wifi else Icons.Rounded.WifiOff,
                        contentDescription = "Estado Wi-Fi",
                        tint = if (estaConectado) Color(0xFF1B5E20) else Color(0xFFC62828),
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Botón Cerrar Sesión
                IconButton(
                    onClick = onCerrarSesion,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFF5F7F6), shape = CircleShape)
                        .border(1.dp, Color(0xFFE0E6E2), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PowerSettingsNew,
                        contentDescription = "Cerrar Sesión",
                        tint = Color(0xFF5D6D7E),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BannerFailSafeBorde(
    perfil: PlantProfile,
    onReconectar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF9A9A))
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = "Alerta Fail-Safe",
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "FAIL-SAFE DE BORDE ACTIVO",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Enlace Wi-Fi interrumpido. El nodo ESP32 regula de forma autónoma para proteger el cultivo de '${perfil.name}' (Compuerta en 45°).",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onReconectar,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB71C1C)),
                border = BorderStroke(1.dp, Color(0xFFEF9A9A))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Autorenew,
                    contentDescription = "Reintentar",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reintentar Enlace Wi-Fi", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun BannerAlertaAgronomica(mensaje: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFE082))
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.WarningAmber,
                contentDescription = "Alerta",
                tint = Color(0xFFF57F17),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFE65100)
            )
        }
    }
}

@Composable
private fun BannerModoManual(
    puedeResetear: Boolean,
    onReactivarAuto: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFCC80))
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = "Manual Override",
                    tint = Color(0xFFE65100),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Control Manual Override Activo",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBF360C)
                )
            }

            if (puedeResetear) {
                Button(
                    onClick = onReactivarAuto,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ColorVerdePrimario)
                ) {
                    Text("Reactivar Auto", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun TarjetaSensor(
    titulo: String,
    valor: String,
    valorNum: Float,
    minVal: Float,
    maxVal: Float,
    unidad: String,
    rangoEsperado: String,
    estaEnRango: Boolean,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ColorBordeCard)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    fontSize = 10.5.sp,
                    color = ColorTextoSecundario,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (estaEnRango) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = if (estaEnRango) "Estable ~" else "Alerta ▲",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (estaEnRango) Color(0xFF1B5E20) else Color(0xFFC62828),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = icono,
                        contentDescription = titulo,
                        tint = colorIcono,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Tacómetro semicircular con odómetro integrado
            PastelArcGauge(
                valor = valorNum,
                minimo = minVal,
                maximo = maxVal,
                unidad = unidad,
                estaEnRango = estaEnRango,
                colorArco = colorIcono,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (estaEnRango) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (estaEnRango) Color(0xFF2E7D32) else Color(0xFFC62828))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (estaEnRango) "Óptimo ($rangoEsperado)" else "Alerta ($rangoEsperado)",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estaEnRango) Color(0xFF1B5E20) else Color(0xFFC62828),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaHumedadSuelo(
    raw: Int,
    estado: EstadoSuelo,
    rangoEsperado: String,
    estaEnRango: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ColorBordeCard)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Grass,
                        contentDescription = "FC-28",
                        tint = ColorVerdePrimario,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Humedad Radicular (Suelo FC-28)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextoPrincipal
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (estaEnRango) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = estado.titulo.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (estaEnRango) Color(0xFF1B5E20) else Color(0xFFE65100),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ADC: ",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextoPrincipal
                    )
                    PastelOdometerText(
                        value = "$raw",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextoPrincipal
                    )
                    Text(
                        text = " / 1023",
                        style = MaterialTheme.typography.bodySmall,
                        color = ColorTextoSecundario
                    )
                }
                Text(
                    text = "Óptimo: $rangoEsperado",
                    style = MaterialTheme.typography.bodySmall,
                    color = ColorTextoSecundario,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val progreso = (1023 - raw.coerceIn(0, 1023)) / 1023f
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = ColorVerdePrimario,
                trackColor = Color(0xFFE0E0E0)
            )
        }
    }
}

@Composable
private fun TarjetaRutinaCultivo(
    perfilActivo: PlantProfile,
    userRole: Role,
    onSeleccionarRutina: (PlantProfile) -> Unit,
    onAbrirCalibracion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ColorBordeCard)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila de Encabezado: Título y Nombre Hero
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "PERFIL BIOLÓGICO ACTIVO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ColorTextoSecundario,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = perfilActivo.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = ColorVerdeOscuro
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = ColorVerdeSuave,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Eco,
                            contentDescription = "Rutina",
                            tint = ColorVerdePrimario,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = perfilActivo.description,
                style = MaterialTheme.typography.bodySmall,
                color = ColorTextoSecundario
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Micro-Grid de Parámetros Activos
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF7FBF8),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(ColorVerdeBorde)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("TEMP", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorTextoSecundario)
                        Text(perfilActivo.tempRangeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("HUMEDAD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorTextoSecundario)
                        Text(perfilActivo.humidityRangeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                        Text("SUELO ADC", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ColorTextoSecundario)
                        Text(perfilActivo.soilRangeText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = ColorTextoPrincipal)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Selector de Rutinas (Botones de igual tamaño en cuadrícula 2x2 consistente)
            if (userRole.canChangeRoutines()) {
                Text(
                    text = "CONMUTAR RUTINA DE CULTIVO:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorTextoSecundario,
                    letterSpacing = 0.4.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Fila 1: Tomate y Frutilla
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonSelectorRutina(
                        titulo = "Tomate",
                        esSeleccionado = perfilActivo.id == "TOMATE",
                        onClick = {
                            PlantProfile.DEFAULT_PROFILES.find { it.id == "TOMATE" }?.let(onSeleccionarRutina)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    BotonSelectorRutina(
                        titulo = "Frutilla",
                        esSeleccionado = perfilActivo.id == "FRUTILLA",
                        onClick = {
                            PlantProfile.DEFAULT_PROFILES.find { it.id == "FRUTILLA" }?.let(onSeleccionarRutina)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Fila 2: Lechuga y Personalizado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonSelectorRutina(
                        titulo = "Lechuga",
                        esSeleccionado = perfilActivo.id == "LECHUGA",
                        onClick = {
                            PlantProfile.DEFAULT_PROFILES.find { it.id == "LECHUGA" }?.let(onSeleccionarRutina)
                        },
                        modifier = Modifier.weight(1f)
                    )

                    BotonSelectorRutina(
                        titulo = "Personalizado",
                        esSeleccionado = perfilActivo.id == "CUSTOM",
                        onClick = {
                            PlantProfile.DEFAULT_PROFILES.find { it.id == "CUSTOM" }?.let(onSeleccionarRutina)
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Bloqueado",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (userRole.isReadOnly()) "Cambio de rutina bloqueado en terminal de terreno." else "Cambio de rutina restringido para el rol ${userRole.displayTitle}.",
                        fontSize = 12.sp,
                        color = Color(0xFF757575)
                    )
                }
            }

            // BOTÓN DE CALIBRACIÓN: Destacado, tamaño completo, texto visible sin cortes
            if (userRole.canEditThresholds()) {
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onAbrirCalibracion,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ColorVerdePrimario,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Calibrar",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Calibrar Umbrales Biológicos",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun BotonSelectorRutina(
    titulo: String,
    esSeleccionado: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (esSeleccionado) ColorVerdePrimario else Color(0xFFF4F7F5),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(
                if (esSeleccionado) ColorVerdeOscuro else Color(0xFFD5DFD8)
            )
        ),
        modifier = modifier.height(42.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (esSeleccionado) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = titulo,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (esSeleccionado) Color.White else ColorTextoPrincipal
                )
            }
        }
    }
}

@Composable
private fun TarjetaCompuertaServo(
    anguloActual: Int,
    porcentajeAbierto: Int,
    esModoManual: Boolean,
    puedeControlar: Boolean,
    onAbrirCompuerta: () -> Unit,
    onCerrarCompuerta: () -> Unit,
    onPosicionMedia: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ColorBordeCard)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila 1: Título y Badge de Modo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MeetingRoom,
                        contentDescription = "Servo",
                        tint = ColorVerdePrimario,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ventilación Pasiva (Servo)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextoPrincipal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Compuerta batiente • Extremo lateral",
                            fontSize = 11.sp,
                            color = ColorTextoSecundario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (esModoManual) Color(0xFFFFE0B2) else Color(0xFFC8E6C9)
                ) {
                    Text(
                        text = if (esModoManual) "OVERRIDE" else "AUTO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (esModoManual) Color(0xFFE65100) else Color(0xFF1B5E20),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visualizador dinámico en vivo de la compuerta y brisa
            CompuertaVentanaVisualizer(
                angulo = anguloActual,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Métrica de Ángulo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ángulo: $anguloActual°",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                    color = ColorTextoPrincipal
                )
                Text(
                    text = "$porcentajeAbierto% abierta",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ColorVerdeOscuro
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { porcentajeAbierto / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = ColorVerdePrimario,
                trackColor = Color(0xFFE0E0E0)
            )

            // Fila 2: Botones de Control Manual (Mismo tamaño y altura)
            if (puedeControlar) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonControlActuador(
                        texto = "0° (Cerrar)",
                        esActivo = anguloActual == 0,
                        onClick = onCerrarCompuerta,
                        modifier = Modifier.weight(1f)
                    )
                    BotonControlActuador(
                        texto = "45° (Media)",
                        esActivo = anguloActual == 45,
                        onClick = onPosicionMedia,
                        modifier = Modifier.weight(1f)
                    )
                    BotonControlActuador(
                        texto = "90° (Abrir)",
                        esActivo = anguloActual == 90,
                        onClick = onAbrirCompuerta,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TarjetaVentiladorMosfet(
    pwmActual: Int,
    porcentaje: Int,
    esModoManual: Boolean,
    puedeControlar: Boolean,
    onSubirVelocidad: () -> Unit,
    onBajarVelocidad: () -> Unit,
    onMaximo: () -> Unit,
    onApagar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(ColorBordeCard)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Fila 1: Título y Modo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Air,
                        contentDescription = "Ventilador",
                        tint = ColorVerdePrimario,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Ventilación Forzada (12V)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = ColorTextoPrincipal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Extractor PWM • Extremo opuesto",
                            fontSize = 11.sp,
                            color = ColorTextoSecundario,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (esModoManual) Color(0xFFFFE0B2) else Color(0xFFC8E6C9)
                ) {
                    Text(
                        text = if (esModoManual) "OVERRIDE" else "AUTO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (esModoManual) Color(0xFFE65100) else Color(0xFF1B5E20),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visualizador dinámico de aspas giratorias según potencia PWM
            VentiladorMosfetVisualizer(
                pwm = pwmActual,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Métrica de Potencia
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Potencia: $porcentaje%",
                    style = MaterialTheme.typography.bodyMedium.copy(fontFeatureSettings = "tnum"),
                    fontWeight = FontWeight.Bold,
                    color = ColorTextoPrincipal
                )
                Text(
                    text = "PWM: $pwmActual / 255",
                    fontSize = 12.sp,
                    color = ColorTextoSecundario
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { porcentaje / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF0288D1),
                trackColor = Color(0xFFE0E0E0)
            )

            // Fila 2: Botones de Control Manual (4 botones de idéntico tamaño y altura)
            if (puedeControlar) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BotonControlActuador(
                        texto = "– 15%",
                        esActivo = false,
                        onClick = onBajarVelocidad,
                        modifier = Modifier.weight(1f)
                    )
                    BotonControlActuador(
                        texto = "+ 15%",
                        esActivo = false,
                        onClick = onSubirVelocidad,
                        modifier = Modifier.weight(1f)
                    )
                    BotonControlActuador(
                        texto = "100% MAX",
                        esActivo = pwmActual >= 250,
                        onClick = onMaximo,
                        colorActivo = Color(0xFFD32F2F),
                        modifier = Modifier.weight(1f)
                    )
                    BotonControlActuador(
                        texto = "0% OFF",
                        esActivo = pwmActual == 0,
                        onClick = onApagar,
                        colorActivo = Color(0xFF455A64),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun BotonControlActuador(
    texto: String,
    esActivo: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colorActivo: Color = ColorVerdePrimario
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (esActivo) colorActivo else Color(0xFFF1F5F2),
        border = BorderStroke(
            1.dp,
            if (esActivo) colorActivo else Color(0xFFD0DCD4)
        ),
        modifier = modifier
            .height(42.dp)
            .pastelBounceClickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = texto,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = if (esActivo) Color.White else ColorTextoPrincipal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun SeccionAccionesNavegacion(
    role: Role,
    onIrAHistorial: () -> Unit,
    onIrAAuditoria: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onIrAHistorial,
            modifier = Modifier
                .weight(1f)
                .height(46.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorVerdePrimario),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.History,
                contentDescription = "Historial",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Historial (5 min)", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }

        if (role.canViewAuditLogs()) {
            Button(
                onClick = onIrAAuditoria,
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = "Auditoría",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Auditoría ISO", fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            }
        }
    }
}

@Composable
private fun TarjetaEstadoEnlace(mensaje: String, estaConectado: Boolean) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = Color(0xF2F1F5F2),
        border = BorderStroke(1.dp, ColorBordeCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (estaConectado) Color(0xFF2E7D32) else Color(0xFFC62828))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = mensaje,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF37474F)
            )
        }
    }
}

@Composable
private fun TarjetaSelectorModoConexion(
    modoActual: ConnectionMode,
    onCambiarModo: (ConnectionMode) -> Unit,
    puedeCambiarModo: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, ColorBordeCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.SettingsRemote,
                    contentDescription = null,
                    tint = ColorVerdePrimario,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "MODO DE COMUNICACIÓN DEL SISTEMA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorVerdeOscuro,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConnectionMode.values().forEach { modo ->
                    val esSeleccionado = modo == modoActual
                    val (icono, colorActivo) = when (modo) {
                        ConnectionMode.SIMULACION -> Icons.Rounded.Science to Color(0xFF0D9488)
                        ConnectionMode.WIFI -> Icons.Rounded.Wifi to Color(0xFF1B5E20)
                        ConnectionMode.BLUETOOTH -> Icons.Rounded.Bluetooth to Color(0xFF0284C7)
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .pastelBounceClickable(enabled = puedeCambiarModo) {
                                if (puedeCambiarModo) onCambiarModo(modo)
                            },
                        shape = RoundedCornerShape(10.dp),
                        color = if (esSeleccionado) colorActivo else Color(0xFFF1F5F2),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (esSeleccionado) colorActivo else Color(0xFFD4DFD7)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = icono,
                                contentDescription = modo.title,
                                tint = if (esSeleccionado) Color.White else ColorTextoPrincipal,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = when (modo) {
                                    ConnectionMode.SIMULACION -> "Simular"
                                    ConnectionMode.WIFI -> "Wi-Fi"
                                    ConnectionMode.BLUETOOTH -> "Bluetooth"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (esSeleccionado) Color.White else ColorTextoPrincipal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaDetalleModoActivo(
    state: InvernaderoState,
    currentUser: User,
    iotManager: GreenhouseIotManager,
    onAbrirDialogoIp: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, ColorBordeCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(state.modoConexion.badgeColorHex)
                    ) {
                        Text(
                            text = state.modoConexion.title.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = state.nombreDispositivo,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorTextoPrincipal
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (state.estaConectado) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .padding(2.dp)
                            .background(
                                color = if (state.estaConectado) Color(0xFF2E7D32) else Color(0xFFC62828),
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = when (state.modoConexion) {
                    ConnectionMode.SIMULACION ->
                        "Ambiente sintético activo: ideal para probar respuestas climáticas y límites de cultivo sin depender de hardware físico."
                    ConnectionMode.WIFI ->
                        "Conexión TCP/IP mediante solicitudes HTTP GET/POST contra el firmware del ESP32 en la IP local ${state.ipNodoEsp32}."
                    ConnectionMode.BLUETOOTH ->
                        "Enlace serial inalámbrico Bluetooth SPP activo con ${state.bluetoothDeviceName} (${state.bluetoothMac})."
                },
                fontSize = 11.sp,
                color = ColorTextoSecundario,
                lineHeight = 15.sp
            )

            if (!currentUser.role.isReadOnly()) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (state.modoConexion) {
                        ConnectionMode.SIMULACION -> {
                            Button(
                                onClick = { iotManager.simularAlertaTemperatura(currentUser) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF87171)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                            ) {
                                Text(
                                    text = "Simular Calor",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = { iotManager.simularAlertaSequia(currentUser) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                            ) {
                                Text(
                                    text = "Simular Sequía",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        ConnectionMode.WIFI -> {
                            Button(
                                onClick = onAbrirDialogoIp,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ColorVerdePrimario),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Configurar IP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = { iotManager.alternarSimulacionFalloConexion(currentUser) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (state.estaConectado) Color(0xFFF87171) else ColorVerdePrimario
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.estaConectado) Icons.Rounded.WifiOff else Icons.Rounded.Wifi,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (state.estaConectado) "Fail-Safe" else "Reconectar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        ConnectionMode.BLUETOOTH -> {
                            Button(
                                onClick = { iotManager.reconectarBluetooth(currentUser) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Bluetooth,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reconectar",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = { iotManager.simularAlertaTemperatura(currentUser) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                            ) {
                                Text(
                                    text = "Test SPP",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE0F2F1),
                    border = BorderStroke(1.dp, Color(0xFFB2DFDB)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = Color(0xFF00796B),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Terminal en Terreno: Acciones de red bloqueadas en modo pantalla.",
                            fontSize = 10.5.sp,
                            color = Color(0xFF004D40)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaCentroNotificacionesPush(
    onEnviarPrueba: () -> Unit,
    puedeEnviar: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF2FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, ColorBordeCard)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.NotificationsActive,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "SISTEMA DE NOTIFICACIONES PUSH",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF92400E),
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Envío automático de notificaciones push prioritarias cuando se superan límites térmicos, falta de humedad radicular o falla del enlace.",
                fontSize = 11.sp,
                color = ColorTextoSecundario,
                lineHeight = 15.sp
            )

            if (puedeEnviar) {
                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onEnviarPrueba,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.NotificationsActive,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Enviar Notificación Push de Prueba",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "• Envío de notificaciones activo en canal prioritario para operadores remotos.",
                    fontSize = 10.5.sp,
                    color = ColorTextoSecundario
                )
            }
        }
    }
}

@Composable
private fun BannerPantallaTerreno() {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xF2E0F2F1),
        border = BorderStroke(1.dp, Color(0xFF80CBC4)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(Color(0xFF00796B), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Visibility,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = "Pantalla de Terreno • Solo Visualización",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF004D40)
                )
                Text(
                    text = "Parámetros en vivo de la cámara • Control y ajustes bloqueados",
                    fontSize = 10.5.sp,
                    color = Color(0xFF00695C)
                )
            }
        }
    }
}
