package com.example.appinvernaderoautonomo.ui.dashboard

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.NetworkPing
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import kotlinx.coroutines.launch

private val ColorVerdePrimario = Color(0xFF1E7E34)

@Composable
fun ConfiguracionIpDialog(
    ipActual: String,
    onDismiss: () -> Unit,
    onGuardar: (String) -> Unit,
    onTestPing: suspend (String) -> Boolean
) {
    var ipInput by remember { mutableStateOf(ipActual) }
    var pingState by remember { mutableStateOf<Boolean?>(null) }
    var isPinging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

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
                        imageVector = Icons.Rounded.Router,
                        contentDescription = "Router",
                        tint = ColorVerdePrimario,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Enlace Wi-Fi ESP32",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1C2833)
                    )
                    Text(
                        text = "Configuración de Red Local / SoftAP",
                        fontSize = 12.sp,
                        color = Color(0xFF5D6D7E)
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "Defina la dirección IP asignada al microcontrolador ESP32 (por defecto 192.168.4.1 en modo SoftAP o 192.168.1.150 en LAN):",
                    fontSize = 11.sp,
                    color = Color(0xFF5D6D7E)
                )

                OutlinedTextField(
                    value = ipInput,
                    onValueChange = {
                        ipInput = it
                        pingState = null
                    },
                    label = { Text("Dirección IPv4") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                isPinging = true
                                pingState = onTestPing(ipInput.trim())
                                isPinging = false
                            }
                        },
                        enabled = !isPinging && ipInput.isNotBlank(),
                        modifier = Modifier.height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (isPinging) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = ColorVerdePrimario
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Probando...", fontSize = 12.sp)
                        } else {
                            Icon(
                                imageVector = Icons.Rounded.NetworkPing,
                                contentDescription = "Ping",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Probar Ping", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    if (pingState != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (pingState == true) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (pingState == true) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,
                                    contentDescription = "Estado",
                                    tint = if (pingState == true) Color(0xFF2E7D32) else Color(0xFFC62828),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (pingState == true) "En Línea" else "Inalcanzable",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (pingState == true) Color(0xFF2E7D32) else Color(0xFFC62828)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGuardar(ipInput.trim()) },
                enabled = ipInput.isNotBlank(),
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
                Text("Guardar IP", fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                Text("Cerrar", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF455A64))
            }
        }
    )
}
