package com.example.appinvernaderoautonomo.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.Autorenew
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MeetingRoom
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appinvernaderoautonomo.iot.GreenhouseIotManager
import com.example.appinvernaderoautonomo.model.EstadoSuelo
import com.example.appinvernaderoautonomo.model.InvernaderoState
import com.example.appinvernaderoautonomo.model.PlantProfile
import com.example.appinvernaderoautonomo.model.Role
import com.example.appinvernaderoautonomo.model.User
import com.example.appinvernaderoautonomo.ui.thresholds.ThresholdDialog

@Composable
fun DashboardScreen(
    state: InvernaderoState,
    currentUser: User,
    iotManager: GreenhouseIotManager,
    onIrAHistorial: () -> Unit,
    onIrAAuditoria: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var mostrarDialogoUmbrales by remember { mutableStateOf(false) }
    var mostrarDialogoIp by remember { mutableStateOf(false) }

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

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cabecera de Usuario y Rol RBAC
        HeaderUsuarioRBAC(
            user = currentUser,
            estaConectado = state.estaConectado,
            ipNodo = state.ipNodoEsp32,
            onConfigurarIp = { mostrarDialogoIp = true },
            onSimularConexion = { iotManager.alternarSimulacionFalloConexion(currentUser) },
            onCerrarSesion = onCerrarSesion
        )

        // Banner Fail-Safe de Borde si se pierde la conexión Wi-Fi
        if (!state.estaConectado || state.modoFailSafeBorde) {
            BannerFailSafeBorde(
                perfil = state.rutinaActiva,
                onReconectar = { iotManager.alternarSimulacionFalloConexion(currentUser) }
            )
        }

        // Alerta Agronómica (Humedad radicular / Estrés)
        if (state.alertaSeguridad != null) {
            BannerAlertaAgronomica(mensaje = state.alertaSeguridad)
        }

        // Banner de Alerta si algún actuador está en modo Manual Override
        if (state.hayModoManualActivo) {
            BannerModoManual(
                puedeResetear = currentUser.role.canOverrideActuators(),
                onReactivarAuto = { iotManager.reactivarModoAuto(currentUser) }
            )
        }

        // SECCIÓN 1: Telemetría Ambiental en Tiempo Real
        Text(
            text = "Telemetría Ambiental en Vivo (DHT22 & FC-28)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TarjetaSensor(
                titulo = "Temperatura",
                valor = "${String.format("%.1f", state.temperatura)} °C",
                rangoEsperado = state.rutinaActiva.tempRangeText,
                estaEnRango = state.estaEnRangoTemperatura,
                icono = Icons.Rounded.Thermostat,
                colorIcono = if (state.estaEnRangoTemperatura) Color(0xFF2E7D32) else Color(0xFFE65100),
                modifier = Modifier.weight(1f)
            )

            TarjetaSensor(
                titulo = "Humedad Aire",
                valor = "${String.format("%.1f", state.humedadAmbiente)} %",
                rangoEsperado = state.rutinaActiva.humidityRangeText,
                estaEnRango = state.estaEnRangoHumedad,
                icono = Icons.Rounded.WaterDrop,
                colorIcono = if (state.estaEnRangoHumedad) Color(0xFF0288D1) else Color(0xFFE65100),
                modifier = Modifier.weight(1f)
            )
        }

        // Tarjeta Humedad de Suelo
        TarjetaHumedadSuelo(
            raw = state.humedadSueloRaw,
            estado = state.estadoSuelo,
            rangoEsperado = state.rutinaActiva.soilRangeText,
            estaEnRango = state.estaEnRangoSuelo
        )

        // SECCIÓN 2: Rutina Activa y Parámetros Agronómicos
        TarjetaRutinaCultivo(
            perfilActivo = state.rutinaActiva,
            userRole = currentUser.role,
            onSeleccionarRutina = { perfil -> iotManager.cambiarRutina(perfil, currentUser) },
            onAbrirCalibracion = { mostrarDialogoUmbrales = true }
        )

        // SECCIÓN 3: Actuadores Físicos (Compuerta Servo SG90 y Extractor 12V MOSFET)
        Text(
            text = "Actuadores Físicos del Invernadero",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1B5E20)
        )

        // Compuerta Batiente (Servo SG90)
        TarjetaCompuertaServo(
            anguloActual = state.anguloVentana,
            porcentajeAbierto = state.porcentajeVentanaAbierta,
            esModoManual = state.modoManualVentana,
            puedeControlar = currentUser.role.canOverrideActuators(),
            onAbrirCompuerta = { iotManager.setManualVentana(90, currentUser) },
            onCerrarCompuerta = { iotManager.setManualVentana(0, currentUser) },
            onPosicionMedia = { iotManager.setManualVentana(45, currentUser) }
        )

        // Extractor de Calor (Ventilador 12V con MOSFET IRF520N)
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

        // SECCIÓN 4: Acciones y Navegación según Rol RBAC
        SeccionAccionesNavegacion(
            role = currentUser.role,
            onIrAHistorial = onIrAHistorial,
            onIrAAuditoria = onIrAAuditoria
        )

        // Barra inferior de estado del enlace
        TarjetaEstadoEnlace(mensaje = state.ultimoMensaje, estaConectado = state.estaConectado)

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun HeaderUsuarioRBAC(
    user: User,
    estaConectado: Boolean,
    ipNodo: String,
    onConfigurarIp: () -> Unit,
    onSimularConexion: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = user.fullName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2923)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(user.role.badgeColorHex)
                        ) {
                            Text(
                                text = user.role.displayTitle.uppercase(),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            onClick = onConfigurarIp,
                            enabled = user.role == Role.ADMIN,
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFECEFF1)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ipNodo,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF37474F)
                                )
                                if (user.role == Role.ADMIN) {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Rounded.Tune,
                                        contentDescription = "Editar IP",
                                        tint = Color(0xFF546E7A),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Botón para simular caída de Wi-Fi y probar el Fail-Safe en vivo
                    IconButton(
                        onClick = onSimularConexion,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (estaConectado) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
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

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = onCerrarSesion,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFF5F5F5), shape = CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PowerSettingsNew,
                            contentDescription = "Cerrar Sesión",
                            tint = Color(0xFF757575),
                            modifier = Modifier.size(18.dp)
                        )
                    }
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
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFEF9A9A)))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.WarningAmber,
                    contentDescription = "Alerta Fail-Safe",
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "FAIL-SAFE AUTÓNOMO DE BORDE ACTIVO",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.titleSmall
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Se interrumpió el enlace Wi-Fi. El microcontrolador ESP32 continúa regulando en lazo cerrado autónomo para proteger el '${perfil.name}'. Compuerta en 45° de resguardo biológico.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFC62828)
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onReconectar,
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB71C1C))
            ) {
                Icon(imageVector = Icons.Rounded.Autorenew, contentDescription = "Restablecer", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reintentar Enlace Wi-Fi", fontSize = 12.sp)
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
        border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFFFE082)))
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
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
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
                    text = "Control Manual Override Activo.",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFBF360C)
                )
            }

            if (puedeResetear) {
                Button(
                    onClick = onReactivarAuto,
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
                ) {
                    Text("Auto", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
private fun TarjetaSensor(
    titulo: String,
    valor: String,
    rangoEsperado: String,
    estaEnRango: Boolean,
    icono: ImageVector,
    colorIcono: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = titulo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF616161),
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icono,
                    contentDescription = titulo,
                    tint = colorIcono,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = valor,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFeatureSettings = "tnum"
                ),
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2923)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (estaEnRango) Color(0xFF2E7D32) else Color(0xFFE65100))
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Óptimo: $rangoEsperado",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575)
                )
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Air,
                        contentDescription = "FC-28",
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Humedad Radicular (Suelo FC-28)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2923)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (estaEnRango) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                ) {
                    Text(
                        text = estado.titulo,
                        style = MaterialTheme.typography.labelSmall,
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
                Text(
                    text = "Valor ADC: $raw / 1023",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Rango Seguro: $rangoEsperado",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            val progreso = (1023 - raw.coerceIn(0, 1023)) / 1023f
            LinearProgressIndicator(
                progress = { progreso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFF388E3C),
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Eco,
                        contentDescription = "Rutina",
                        tint = Color(0xFF1B5E20),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Rutina Activa del Invernadero",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20)
                    )
                }

                if (userRole.canEditThresholds()) {
                    FilledTonalButton(
                        onClick = onAbrirCalibracion,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Rounded.Tune, contentDescription = "Calibrar", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calibrar", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = perfilActivo.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2923)
            )

            Text(
                text = perfilActivo.description,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF616161)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Selector de rutinas preestablecidas
            if (userRole.canChangeRoutines()) {
                Text(
                    text = "Conmutar Rutina Preestablecida:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PlantProfile.DEFAULT_PROFILES.forEach { perfil ->
                        val esSeleccionado = perfil.id == perfilActivo.id
                        Surface(
                            onClick = { onSeleccionarRutina(perfil) },
                            shape = RoundedCornerShape(8.dp),
                            color = if (esSeleccionado) Color(0xFF1B5E20) else Color(0xFFF1F8E9),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = perfil.id.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (esSeleccionado) Color.White else Color(0xFF1B5E20)
                                )
                            }
                        }
                    }
                }
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Lock,
                        contentDescription = "Bloqueado",
                        tint = Color(0xFF9E9E9E),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Cambio de rutina restringido para el rol Auditor.",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9E9E9E)
                    )
                }
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.MeetingRoom,
                        contentDescription = "Servo",
                        tint = Color(0xFF1B5E20),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Ventilación Pasiva (Servo SG90)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2923)
                        )
                        Text(
                            text = "Compuerta Batiente Superior",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (esModoManual) Color(0xFFFFE0B2) else Color(0xFFC8E6C9)
                ) {
                    Text(
                        text = if (esModoManual) "OVERRIDE" else "AUTO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (esModoManual) Color(0xFFE65100) else Color(0xFF1B5E20)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ángulo: $anguloActual° ($porcentajeAbierto% abierta)",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

                if (puedeControlar) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilledTonalButton(
                            onClick = onCerrarCompuerta,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("0°", fontSize = 11.sp)
                        }
                        FilledTonalButton(
                            onClick = onPosicionMedia,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("45°", fontSize = 11.sp)
                        }
                        FilledTonalButton(
                            onClick = onAbrirCompuerta,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("90°", fontSize = 11.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Regulación automática",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF757575)
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
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Air,
                        contentDescription = "Ventilador",
                        tint = Color(0xFF1B5E20),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Ventilación Forzada (12V MOSFET)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F2923)
                        )
                        Text(
                            text = "Driver IRF520N + Flyback 1N4004",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF757575)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (esModoManual) Color(0xFFFFE0B2) else Color(0xFFC8E6C9)
                ) {
                    Text(
                        text = if (esModoManual) "OVERRIDE" else "AUTO",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (esModoManual) Color(0xFFE65100) else Color(0xFF1B5E20)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "$porcentaje % (PWM $pwmActual)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (puedeControlar) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilledTonalButton(
                            onClick = onBajarVelocidad,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.KeyboardArrowDown, contentDescription = "Bajar", modifier = Modifier.size(16.dp))
                        }
                        FilledTonalButton(
                            onClick = onSubirVelocidad,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Icon(imageVector = Icons.Rounded.KeyboardArrowUp, contentDescription = "Subir", modifier = Modifier.size(16.dp))
                        }
                        Button(
                            onClick = onMaximo,
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
                        ) {
                            Text("MAX", fontSize = 10.sp)
                        }
                        OutlinedButton(
                            onClick = onApagar,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("OFF", fontSize = 10.sp)
                        }
                    }
                } else {
                    Text(
                        text = "Regulación automática",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF757575)
                    )
                }
            }
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
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
        ) {
            Icon(imageVector = Icons.Rounded.History, contentDescription = "Historial", modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Historial (5 min)", fontSize = 13.sp)
        }

        if (role.canViewAuditLogs()) {
            Button(
                onClick = onIrAAuditoria,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0277BD))
            ) {
                Icon(imageVector = Icons.Rounded.Security, contentDescription = "Auditoría", modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Auditoría ISO", fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun TarjetaEstadoEnlace(mensaje: String, estaConectado: Boolean) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFECEFF1),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (estaConectado) Color(0xFF2E7D32) else Color(0xFFC62828))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF37474F)
            )
        }
    }
}
