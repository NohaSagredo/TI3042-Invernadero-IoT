package com.example.appinvernaderoautonomo.ui.thresholds

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Thermostat
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appinvernaderoautonomo.model.PlantProfile

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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = "Calibrar Umbrales Críticos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B5E20)
                )
                Text(
                    text = profile.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF556052)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Temperatura Min
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Temp. Mínima de Resguardo",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${String.format("%.1f", tempMin)} °C",
                            fontWeight = FontWeight.Bold,
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
                }

                // Temperatura Max
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Temp. Máxima (Disparo Extractor)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${String.format("%.1f", tempMax)} °C",
                            fontWeight = FontWeight.Bold,
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

                // Humedad Min
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Humedad Relativa Mínima",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${humMin.toInt()} %",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF388E3C)
                        )
                    }
                    Slider(
                        value = humMin,
                        onValueChange = { humMin = it.coerceAtMost(humMax - 5f) },
                        valueRange = 30f..80f,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFF388E3C),
                            activeTrackColor = Color(0xFF388E3C)
                        )
                    )
                }

                // Humedad Max
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Humedad Relativa Máxima",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "${humMax.toInt()} %",
                            fontWeight = FontWeight.Bold,
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
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = profile.copy(
                        tempMin = tempMin,
                        tempMax = tempMax,
                        humidityMin = humMin,
                        humidityMax = humMax
                    )
                    onConfirm(updated)
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20))
            ) {
                Icon(imageVector = Icons.Rounded.Check, contentDescription = "Guardar")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Guardar y Sincronizar")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Cancelar")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Cancelar")
            }
        }
    )
}
