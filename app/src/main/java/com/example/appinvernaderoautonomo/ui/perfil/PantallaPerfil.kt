package com.example.appinvernaderoautonomo.ui.perfil

import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appinvernaderoautonomo.model.Role
import com.example.appinvernaderoautonomo.model.User
import com.example.appinvernaderoautonomo.ui.theme.PastelBorder
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenDark
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenLight
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenPrimary
import com.example.appinvernaderoautonomo.ui.theme.PastelGreenSoft
import com.example.appinvernaderoautonomo.ui.theme.PastelTextMuted
import com.example.appinvernaderoautonomo.ui.theme.PastelTextPrimary
import com.example.appinvernaderoautonomo.ui.theme.PastelTextSecondary

@Composable
fun PantallaPerfil(
    currentUser: User,
    onIrAConfiguracion: () -> Unit,
    onVolver: () -> Unit,
    onCerrarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    var nombreUsuario by rememberSaveable { mutableStateOf(currentUser.fullName) }
    var interesCultivo by rememberSaveable { mutableStateOf(true) }
    var interesVentilacion by rememberSaveable { mutableStateOf(true) }
    var interesSeguridad by rememberSaveable { mutableStateOf(currentUser.role == Role.ADMIN) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0x22F7FBF8))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Barra superior de navegación interna
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onVolver,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xF0FFFFFF), shape = CircleShape)
                    .border(1.dp, PastelBorder, CircleShape)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = "Volver al Dashboard",
                    tint = PastelGreenDark
                )
            }

            Text(
                text = "Perfil del Operador",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PastelGreenDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            IconButton(
                onClick = onCerrarSesion,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFFFFEBEE), shape = CircleShape)
                    .border(1.dp, Color(0xFFFFCDD2), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Rounded.PowerSettingsNew,
                    contentDescription = "Cerrar Sesión",
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 1. Imagen circular con foto/avatar del operador
        Box(contentAlignment = Alignment.BottomEnd) {
            Surface(
                shape = CircleShape,
                color = Color(0xF5FFFFFF),
                border = CardDefaults.outlinedCardBorder().copy(
                    brush = androidx.compose.ui.graphics.SolidColor(PastelGreenPrimary)
                ),
                modifier = Modifier.size(112.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (currentUser.role.isReadOnly()) Icons.Rounded.Visibility else Icons.Rounded.AccountCircle,
                        contentDescription = "Avatar de Usuario",
                        tint = if (currentUser.role.isReadOnly()) Color(0xFF00796B) else PastelGreenPrimary,
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            Surface(
                shape = CircleShape,
                color = if (currentUser.role.isReadOnly()) Color(0xFF00796B) else PastelGreenPrimary,
                modifier = Modifier.size(30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (currentUser.role.isReadOnly()) Icons.Rounded.Info else Icons.Rounded.Eco,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Ficha de Identidad y Rol RBAC
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = nombreUsuario,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PastelTextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(currentUser.role.badgeColorHex)
            ) {
                Text(
                    text = currentUser.role.displayTitle.uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // 2. Campo editable con el nombre de usuario
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(PastelBorder)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Datos de Identificación",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PastelGreenDark
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = nombreUsuario,
                    onValueChange = { if (!currentUser.role.isReadOnly()) nombreUsuario = it },
                    readOnly = currentUser.role.isReadOnly(),
                    label = { Text(if (currentUser.role.isReadOnly()) "Terminal de Visualización" else "Nombre del Operador") },
                    leadingIcon = {
                        Icon(
                            imageVector = if (currentUser.role.isReadOnly()) Icons.Rounded.Visibility else Icons.Rounded.Person,
                            contentDescription = null,
                            tint = if (currentUser.role.isReadOnly()) Color(0xFF00796B) else PastelGreenPrimary
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PastelGreenPrimary,
                        focusedLabelColor = PastelGreenDark
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Usuario en sistema: @${currentUser.username}",
                    fontSize = 11.sp,
                    color = PastelTextMuted
                )
            }
        }

        // 3. Tarjeta con Row de 3 áreas de responsabilidad/interés mediante Checkbox (Requisito Guía Adaptado)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xF7FFFFFF)),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(PastelBorder)
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Áreas de Supervisión Asignadas",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = PastelGreenDark
                )
                Text(
                    text = "Módulos activos bajo el rol de este operador",
                    fontSize = 11.sp,
                    color = PastelTextSecondary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    InteresCheckboxFila(
                        titulo = "Cultivo y Riego (DHT22 y FC-28)",
                        seleccionado = interesCultivo,
                        habilitado = !currentUser.role.isReadOnly(),
                        onCheckedChange = { interesCultivo = it }
                    )
                    InteresCheckboxFila(
                        titulo = "Ventilación (Servo 90° y Extractor 12V)",
                        seleccionado = interesVentilacion,
                        habilitado = !currentUser.role.isReadOnly(),
                        onCheckedChange = { interesVentilacion = it }
                    )
                    InteresCheckboxFila(
                        titulo = "Seguridad Operativa y Auditoría",
                        seleccionado = interesSeguridad,
                        habilitado = !currentUser.role.isReadOnly(),
                        onCheckedChange = { interesSeguridad = it }
                    )
                }
            }
        }

        // 4. Botón para navegar a la pantalla de configuración del invernadero (o ficha informativa si es Visor)
        if (currentUser.role.canModifySettings()) {
            Button(
                onClick = onIrAConfiguracion,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PastelGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ajustes del Invernadero",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE0F2F1),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB2DFDB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Info,
                        contentDescription = null,
                        tint = Color(0xFF00695C),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Terminal en Terreno: Dispositivo de visualización pasiva para la cámara de cultivo. Parámetros y actuadores protegidos.",
                        fontSize = 11.5.sp,
                        color = Color(0xFF004D40),
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Botón Volver al Dashboard
        OutlinedButton(
            onClick = onVolver,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
        ) {
            Text(
                text = "Volver al Dashboard",
                fontWeight = FontWeight.SemiBold,
                color = PastelGreenDark
            )
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
private fun InteresCheckboxFila(
    titulo: String,
    seleccionado: Boolean,
    habilitado: Boolean = true,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = habilitado) { onCheckedChange(!seleccionado) }
            .padding(vertical = 4.dp)
    ) {
        Checkbox(
            checked = seleccionado,
            onCheckedChange = onCheckedChange,
            enabled = habilitado,
            colors = CheckboxDefaults.colors(
                checkedColor = PastelGreenPrimary,
                checkmarkColor = Color.White
            )
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = PastelTextPrimary
        )
    }
}
