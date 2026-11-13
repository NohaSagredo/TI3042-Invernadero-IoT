package com.example.appinvernaderoautonomo.ui.history

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Grass
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appinvernaderoautonomo.data.GreenhouseRepository
import com.example.appinvernaderoautonomo.model.TelemetryLog
import com.example.appinvernaderoautonomo.ui.components.PastelSunflowerLoader
import com.example.appinvernaderoautonomo.ui.theme.PastelBorder
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenDark
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenLight
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenPrimary
import com.example.appinvernaderoautonomo.ui.theme.PastelSkyPrimary
import com.example.appinvernaderoautonomo.ui.theme.PastelTextMuted
import com.example.appinvernaderoautonomo.ui.theme.PastelTextPrimary
import com.example.appinvernaderoautonomo.ui.theme.PastelTextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    repository: GreenhouseRepository,
    onVolver: (() -> Unit)? = null,
    onIrAAuditoria: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var logs by remember { mutableStateOf<List<TelemetryLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    fun cargarLogs() {
        scope.launch {
            isLoading = true
            logs = repository.getRecentTelemetry(60)
            isLoading = false
        }
    }

    fun exportarCsv() {
        scope.launch {
            val csv = repository.exportTelemetryToCsv(100)
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, csv)
                putExtra(Intent.EXTRA_SUBJECT, "Telemetría Invernadero IoT")
                type = "text/csv"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Exportar Telemetría Agronómica (CSV)")
            context.startActivity(shareIntent)
        }
    }

    LaunchedEffect(Unit) {
        cargarLogs()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Historial de Telemetría",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = PastelGreenDark
                        )
                        Text(
                            text = "Muestreo Cronométrico (Cada 5 Minutos)",
                            style = MaterialTheme.typography.bodySmall,
                            color = PastelTextSecondary
                        )
                    }
                },
                navigationIcon = {
                    if (onVolver != null) {
                        IconButton(onClick = onVolver) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = "Volver",
                                tint = PastelGreenDark
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { exportarCsv() }) {
                        Icon(
                            imageVector = Icons.Rounded.Share,
                            contentDescription = "Exportar a CSV",
                            tint = PastelGreenDark
                        )
                    }
                    IconButton(onClick = { cargarLogs() }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Recargar",
                            tint = PastelGreenDark
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xF2FFFFFF),
                    titleContentColor = PastelGreenDark,
                    navigationIconContentColor = PastelGreenDark,
                    actionIconContentColor = PastelGreenDark
                )
            )
        },
        containerColor = Color.Transparent,
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x22F8FAF9))
                .padding(innerPadding)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    PastelSunflowerLoader(mensaje = "Recuperando telemetría del cultivo...")
                }
            } else if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "No hay registros de telemetría disponibles.",
                        color = PastelTextMuted
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tarjeta destacada para acceder a la bitácora de auditoría si está habilitada
                    if (onIrAAuditoria != null) {
                        item {
                            Card(
                                onClick = onIrAAuditoria,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
                                border = BorderStroke(1.dp, PastelBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .background(Color(0xFFE0F2FE), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.Security,
                                                contentDescription = null,
                                                tint = Color(0xFF0284C7),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(
                                                text = "Bitácora de Auditoría",
                                                fontWeight = FontWeight.Bold,
                                                color = PastelTextPrimary,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = "Registro de seguridad, accesos y eventos",
                                                fontSize = 11.sp,
                                                color = PastelTextSecondary
                                            )
                                        }
                                    }
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                        contentDescription = "Abrir Auditoría",
                                        tint = PastelTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    items(logs) { log ->
                        TarjetaLogTelemetria(log)
                    }

                    item {
                        Spacer(modifier = Modifier.height(48.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaLogTelemetria(log: TelemetryLog) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = SolidColor(PastelBorder)
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(PastelGreenPrimary)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.horaFormateada,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PastelTextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${log.fechaFormateada.substringBefore(" ")})",
                        style = MaterialTheme.typography.bodySmall,
                        color = PastelTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = log.rutinaActiva,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B5E20),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Cuadrícula tabular de 3 columnas proporcionales para alineación perfecta
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Temperatura
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Thermostat,
                        contentDescription = "Temp",
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%.1f", log.temperatura)} °C",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PastelTextPrimary,
                        maxLines = 1
                    )
                }

                // Humedad Aire
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.WaterDrop,
                        contentDescription = "Hum",
                        tint = PastelSkyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${String.format("%.1f", log.humedadAmbiente)} %",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PastelTextPrimary,
                        maxLines = 1
                    )
                }

                // Humedad Suelo (Icono Botánico Grass)
                Row(
                    modifier = Modifier.weight(1.1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Grass,
                        contentDescription = "Suelo",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Suelo: ${log.humedadSueloRaw}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = PastelTextPrimary,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ventana: ${log.anguloVentana}° • Extractor: ${log.porcentajeVentilador}% (PWM ${log.velocidadVentiladorPwm})",
                    style = MaterialTheme.typography.bodySmall,
                    color = PastelTextSecondary
                )

                if (log.modoManualActivo) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFFFE0B2)
                    ) {
                        Text(
                            text = "OVERRIDE",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
