package com.example.appinvernaderoautonomo.ui.thresholds

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appinvernaderoautonomo.model.PlantProfile

private val ColorVerdePrimario = Color(0xFF1E7E34)
private val ColorVerdeOscuro = Color(0xFF145A27)

@Composable
fun ThresholdDialog(
    profile: PlantProfile,
    onDismiss: () -> Unit,
    onConfirm: (PlantProfile) -> Unit
) {
    var tempMin by remember { mutableFloatStateOf(profile.tempMin) }
    var tempMax by remember { mutableFloatStateOf(profile.tempMax) }
    var humMin by remember { mutableFloatStateOf(profile.humidityMin) }
    var humMax by remember { mutableFloatStateOf(profile.humidityMax) }
    var soilMin by remember { mutableIntStateOf(profile.soilRawMin) }
    var soilMax by remember { mutableIntStateOf(profile.soilRawMax) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFE8F5E9), shape = RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = "Calibrar",
                        tint = ColorVerdePrimario,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Calibración de Umbrales",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C2833)
                    )
                    Text(
                        text = "Perfil: ${profile.name}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ColorVerdeOscuro
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Ajuste los límites operacionales para la conmutación automática de actuadores en el invernadero:",
                    fontSize = 11.sp,
                    color = Color(0xFF5D6D7E)
                )

                // 1. SECCIÓN TEMPERATURA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FBF8)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC8E6C9))
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "RANGO DE TEMPERATURA (°C)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Temp Min
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Mínima (Cierre de compuerta)", fontSize = 11.sp, color = Color(0xFF2C3E50))
                            Text(
                                "${String.format("%.1f", tempMin)} °C",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF1976D2)
                            )
                        }
                        Slider(
                            value = tempMin,
                            onValueChange = { tempMin = it.coerceAtMost(tempMax - 1f) },
                            valueRange = 10f..30f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF1976D2),
                                activeTrackColor = Color(0xFF1976D2)
                            )
                        )

                        // Temp Max
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Máxima (Extractor y apertura)", fontSize = 11.sp, color = Color(0xFF2C3E50))
                            Text(
                                "${String.format("%.1f", tempMax)} °C",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFFD32F2F)
                            )
                        }
                        Slider(
                            value = tempMax,
                            onValueChange = { tempMax = it.coerceAtLeast(tempMin + 1f) },
                            valueRange = 15f..40f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFD32F2F),
                                activeTrackColor = Color(0xFFD32F2F)
                            )
                        )
                    }
                }

                // 2. SECCIÓN HUMEDAD RELATIVA
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FBF8)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC8E6C9))
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "RANGO DE HUMEDAD AMBIENTE (%)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00796B),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Hum Min
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Humedad Mínima", fontSize = 11.sp, color = Color(0xFF2C3E50))
                            Text(
                                "${humMin.toInt()} %",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF00796B)
                            )
                        }
                        Slider(
                            value = humMin,
                            onValueChange = { humMin = it.coerceAtMost(humMax - 5f) },
                            valueRange = 30f..80f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00796B),
                                activeTrackColor = Color(0xFF00796B)
                            )
                        )

                        // Hum Max
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Humedad Máxima", fontSize = 11.sp, color = Color(0xFF2C3E50))
                            Text(
                                "${humMax.toInt()} %",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF00796B)
                            )
                        }
                        Slider(
                            value = humMax,
                            onValueChange = { humMax = it.coerceAtLeast(humMin + 5f) },
                            valueRange = 50f..95f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF00796B),
                                activeTrackColor = Color(0xFF00796B)
                            )
                        )
                    }
                }

                // 3. SECCIÓN HUMEDAD DE SUELO (FC-28)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7FBF8)),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFC8E6C9))
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "RANGO DE SUSTRATO FC-28 (ADC)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037),
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ADC Óptimo (Húmedo)", fontSize = 11.sp, color = Color(0xFF2C3E50))
                            Text(
                                "ADC $soilMin",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF5D4037)
                            )
                        }
                        Slider(
                            value = soilMin.toFloat(),
                            onValueChange = { soilMin = it.toInt().coerceAtMost(soilMax - 50) },
                            valueRange = 200f..800f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF5D4037),
                                activeTrackColor = Color(0xFF5D4037)
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("ADC Límite Sequedad", fontSize = 11.sp, color = Color(0xFF2C3E50))
                            Text(
                                "ADC $soilMax",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF8D6E63)
                            )
                        }
                        Slider(
                            value = soilMax.toFloat(),
                            onValueChange = { soilMax = it.toInt().coerceAtLeast(soilMin + 50) },
                            valueRange = 400f..1000f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF8D6E63),
                                activeTrackColor = Color(0xFF8D6E63)
                            )
                        )
                    }
                }
            }
        },
        // Botones de acción del diálogo: Idéntico tamaño, altura 46dp y texto ordenado sin cortes
        confirmButton = {
            Button(
                onClick = {
                    val updated = profile.copy(
                        tempMin = tempMin,
                        tempMax = tempMax,
                        humidityMin = humMin,
                        humidityMax = humMax,
                        soilRawMin = soilMin,
                        soilRawMax = soilMax
                    )
                    onConfirm(updated)
                },
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorVerdePrimario)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Guardar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Guardar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFB0BEC5))
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Cancelar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Cancelar",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = Color(0xFF455A64)
                )
            }
        }
    )
}
