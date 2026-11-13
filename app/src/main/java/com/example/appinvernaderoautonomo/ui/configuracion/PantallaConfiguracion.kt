package com.example.appinvernaderoautonomo.ui.configuracion

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appinvernaderoautonomo.ui.theme.PastelBorder
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenDark
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenLight
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenPrimary
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenSoft
import com.example.appinvernaderoautonomo.ui.theme.PastelSkyPrimary
import com.example.appinvernaderoautonomo.ui.theme.PastelTextMuted
import com.example.appinvernaderoautonomo.ui.theme.PastelTextPrimary
import com.example.appinvernaderoautonomo.ui.theme.PastelTextSecondary
import kotlin.math.roundToInt

@Composable
fun PantallaConfiguracion(
    onVolverAlPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados requeridos por la guía y adaptados al Invernadero
    var volumen by rememberSaveable { mutableFloatStateOf(75f) }
    var notificacionesActivas by rememberSaveable { mutableStateOf(true) }
    var respaldoAutomatico by rememberSaveable { mutableStateOf(true) }

    val volumenEntero = volumen.roundToInt()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x22F7FBF8))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barra de navegación superior
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVolverAlPerfil,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xF0FFFFFF), shape = CircleShape)
                    .border(1.dp, PastelBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver",
                    tint = PastelGreenDark
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Ajustes del Invernadero",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PastelGreenDark
                )
                Text(
                    text = "Preferencias operativas y notificaciones",
                    fontSize = 11.sp,
                    color = PastelTextSecondary
                )
            }
        }

        // 1. Sección de Control de Volumen / Alerta Sonora (Slider de 0 a 100)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(PastelBorder)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Nivel de Alerta Sonora",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = PastelTextPrimary
                        )
                        Text(
                            text = "Intensidad del aviso ante anomalías térmicas",
                            fontSize = 11.sp,
                            color = PastelTextSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PastelGreenSoft
                    ) {
                        Text(
                            text = "$volumenEntero / 100",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = PastelGreenDark,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeDown,
                        contentDescription = "Volumen bajo",
                        tint = PastelTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Slider(
                        value = volumen,
                        onValueChange = { volumen = it },
                        valueRange = 0f..100f,
                        colors = SliderDefaults.colors(
                            thumbColor = PastelGreenPrimary,
                            activeTrackColor = PastelGreenPrimary,
                            inactiveTrackColor = PastelGreenLight
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 10.dp)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                        contentDescription = "Volumen alto",
                        tint = PastelGreenDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // 2. Sección de Checkbox para Activar Notificaciones (Requisito Guía)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(PastelBorder)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { notificacionesActivas = !notificacionesActivas }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                color = if (notificacionesActivas) PastelGreenSoft else Color(0xFFFFEBEE),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (notificacionesActivas) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsOff,
                            contentDescription = null,
                            tint = if (notificacionesActivas) PastelGreenDark else Color(0xFFC62828),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Activar notificaciones push",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PastelTextPrimary
                        )
                        Text(
                            text = if (notificacionesActivas) "Alertas inmediatas de sequía y calor" else "Notificaciones silenciadas",
                            style = MaterialTheme.typography.bodySmall,
                            color = PastelTextSecondary
                        )
                    }
                }

                Checkbox(
                    checked = notificacionesActivas,
                    onCheckedChange = { notificacionesActivas = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PastelGreenPrimary,
                        checkmarkColor = Color.White
                    )
                )
            }
        }

        // Opción adicional agronómica: Respaldo continuo en base de datos SQLite
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(PastelBorder)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { respaldoAutomatico = !respaldoAutomatico }
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color(0xFFE0F2FE), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Storage,
                            contentDescription = null,
                            tint = PastelSkyPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Muestreo cada 5 minutos",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = PastelTextPrimary
                        )
                        Text(
                            text = "Registro histórico continuo en SQLite",
                            fontSize = 11.sp,
                            color = PastelTextSecondary
                        )
                    }
                }

                Checkbox(
                    checked = respaldoAutomatico,
                    onCheckedChange = { respaldoAutomatico = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PastelGreenPrimary,
                        checkmarkColor = Color.White
                    )
                )
            }
        }

        // 3. Tarjeta de resumen con Text que muestra el valor actual del slider y el estado del checkbox (Requisito Guía)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF2F8F4)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(PastelBorder)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = PastelGreenPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Resumen de Ajustes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PastelGreenDark
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "• Nivel de alerta sonora: $volumenEntero% (Avisos acústicos locales)",
                    style = MaterialTheme.typography.bodySmall,
                    color = PastelTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Estado de notificaciones push: ${if (notificacionesActivas) "Activadas (Canal prioritario)" else "Desactivadas"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PastelTextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "• Respaldo de telemetría: ${if (respaldoAutomatico) "Activo (Muestreo cada 5 min)" else "Pausado"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = PastelTextPrimary
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. Botón "Volver al Perfil" (Requisito Guía)
        Button(
            onClick = onVolverAlPerfil,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Volver al Perfil",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
    }
}
