package com.example.appinvernaderoautonomo.ui.audit

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.LockOpen
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appinvernaderoautonomo.data.GreenhouseRepository
import com.example.appinvernaderoautonomo.model.AuditEventType
import com.example.appinvernaderoautonomo.model.AuditLog
import kotlinx.coroutines.launch

enum class FiltroAuditoria(val titulo: String) {
    TODOS("Todos"),
    SEGURIDAD("Seguridad & Login"),
    RUTINAS("Rutinas"),
    OVERRIDE("Overrides & Ajustes")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditoriaScreen(
    repository: GreenhouseRepository,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    var logs by remember { mutableStateOf<List<AuditLog>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var filtroSeleccionado by remember { mutableStateOf(FiltroAuditoria.TODOS) }
    val scope = rememberCoroutineScope()

    fun cargarLogs() {
        scope.launch {
            isLoading = true
            logs = repository.getAuditLogs(60)
            isLoading = false
        }
    }

    LaunchedEffect(Unit) {
        cargarLogs()
    }

    val logsFiltrados = remember(logs, filtroSeleccionado) {
        when (filtroSeleccionado) {
            FiltroAuditoria.TODOS -> logs
            FiltroAuditoria.SEGURIDAD -> logs.filter {
                it.eventType in listOf(
                    AuditEventType.LOGIN_SUCCESS,
                    AuditEventType.LOGIN_FAILED,
                    AuditEventType.LOCKOUT_ACTIVATED,
                    AuditEventType.SYSTEM_RESET
                )
            }
            FiltroAuditoria.RUTINAS -> logs.filter { it.eventType == AuditEventType.ROUTINE_CHANGED }
            FiltroAuditoria.OVERRIDE -> logs.filter {
                it.eventType in listOf(
                    AuditEventType.THRESHOLD_MODIFIED,
                    AuditEventType.MANUAL_OVERRIDE,
                    AuditEventType.FAILSAFE_TRIGGERED
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Bitácora de Auditoría",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Trazabilidad de Ciberseguridad ISO 27400",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB3E5FC)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onVolver) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            repository.unlockUser("admin", "Admin (Auditoría)")
                            repository.unlockUser("operador", "Admin (Auditoría)")
                            cargarLogs()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.LockOpen,
                            contentDescription = "Desbloquear Cuentas Fail-Safe",
                            tint = Color.White
                        )
                    }
                    IconButton(onClick = { cargarLogs() }) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Recargar",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0277BD),
                    titleContentColor = Color.White
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8FAF9))
                .padding(innerPadding)
        ) {
            // Barra horizontal de chips de filtrado
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FiltroAuditoria.entries.forEach { filtro ->
                    val seleccionado = filtro == filtroSeleccionado
                    Surface(
                        onClick = { filtroSeleccionado = filtro },
                        shape = RoundedCornerShape(8.dp),
                        color = if (seleccionado) Color(0xFF0277BD) else Color(0xFFE1F5FE)
                    ) {
                        Text(
                            text = filtro.titulo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (seleccionado) Color.White else Color(0xFF0277BD),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (isLoading) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF0277BD))
                    }
                } else if (logsFiltrados.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No se registran eventos con este filtro.",
                            color = Color(0xFF757575)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(logsFiltrados) { log ->
                            TarjetaLogAuditoria(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaLogAuditoria(log: AuditLog) {
    val esAlerta = log.eventType.esAlertaCritica

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                        imageVector = when (log.eventType) {
                            AuditEventType.LOCKOUT_ACTIVATED -> Icons.Rounded.LockClock
                            AuditEventType.LOGIN_FAILED, AuditEventType.FAILSAFE_TRIGGERED -> Icons.Rounded.Warning
                            AuditEventType.THRESHOLD_MODIFIED -> Icons.Rounded.Tune
                            else -> Icons.Rounded.Security
                        },
                        contentDescription = "Tipo",
                        tint = if (esAlerta) Color(0xFFC62828) else Color(0xFF0277BD),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = log.eventType.titulo,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (esAlerta) Color(0xFFB71C1C) else Color(0xFF1F2923)
                    )
                }

                Text(
                    text = log.horaFormateada,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF757575),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = log.details,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF424242)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Actor: ${log.username} (${log.userRole})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF616161)
                )

                Text(
                    text = log.fechaFormateada.substringBefore(" "),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
    }
}
