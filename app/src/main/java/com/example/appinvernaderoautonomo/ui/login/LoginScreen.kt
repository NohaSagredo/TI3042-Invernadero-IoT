package com.example.appinvernaderoautonomo.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Eco
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockClock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appinvernaderoautonomo.data.AuthResult
import com.example.appinvernaderoautonomo.data.GreenhouseRepository
import com.example.appinvernaderoautonomo.model.User
import com.example.appinvernaderoautonomo.security.FailSafeLockoutManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LoginScreen(
    repository: GreenhouseRepository,
    onLoginSuccess: (User) -> Unit,
    modifier: Modifier = Modifier
) {
    var username by remember { mutableStateOf("admin") }
    var password by remember { mutableStateOf("admin123") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    var isLockedOut by remember { mutableStateOf(FailSafeLockoutManager.isLockedOut()) }
    var remainingSeconds by remember { mutableIntStateOf(FailSafeLockoutManager.getRemainingSeconds()) }

    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Temporizador de cuenta regresiva para el bloqueo Fail-Safe
    LaunchedEffect(isLockedOut) {
        while (isLockedOut) {
            remainingSeconds = FailSafeLockoutManager.getRemainingSeconds()
            if (remainingSeconds <= 0) {
                isLockedOut = false
                errorMessage = null
                break
            }
            delay(1000L)
        }
    }

    fun ejecutarLogin() {
        if (isLockedOut) return
        if (username.isBlank() || password.isBlank()) {
            errorMessage = "Complete usuario y contraseña."
            return
        }
        focusManager.clearFocus()
        isLoading = true
        errorMessage = null

        coroutineScope.launch {
            val result = repository.authenticate(username, password)
            isLoading = false
            when (result) {
                is AuthResult.Success -> {
                    onLoginSuccess(result.user)
                }
                is AuthResult.Failure -> {
                    errorMessage = result.message
                    if (result.isLockedOut) {
                        isLockedOut = true
                        remainingSeconds = result.remainingSeconds
                    }
                }
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAF9))
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Emblema e Identidad
            Surface(
                shape = CircleShape,
                color = Color(0xFFE8F5E9),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Eco,
                        contentDescription = "Eco",
                        tint = Color(0xFF1B5E20),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Invernadero Inteligente IoT",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1B5E20),
                textAlign = TextAlign.Center
            )

            Text(
                text = "HMI de Supervisión y Control OT • TI3042",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF556052),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Banner Fail-Safe de Bloqueo
            AnimatedVisibility(visible = isLockedOut) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LockClock,
                            contentDescription = "Bloqueado",
                            tint = Color(0xFFC62828),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Defensa Fail-Safe Activada",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB71C1C),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Acceso bloqueado por tasa de fallos. Desbloqueo en $remainingSeconds s.",
                                color = Color(0xFFC62828),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            // Formulario de Ingreso
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text(
                        text = "Autenticación de Acceso",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F2923)
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = {
                            username = it
                            errorMessage = null
                        },
                        label = { Text("Usuario del Sistema") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = "Usuario",
                                tint = Color(0xFF2E7D32)
                            )
                        },
                        singleLine = true,
                        enabled = !isLockedOut && !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32)
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = {
                            password = it
                            errorMessage = null
                        },
                        label = { Text("Contraseña (PBKDF2)") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Rounded.Lock,
                                contentDescription = "Contraseña",
                                tint = Color(0xFF2E7D32)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                                    contentDescription = "Ver contraseña"
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        singleLine = true,
                        enabled = !isLockedOut && !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF2E7D32),
                            focusedLabelColor = Color(0xFF2E7D32)
                        ),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { ejecutarLogin() })
                    )

                    AnimatedVisibility(visible = errorMessage != null && !isLockedOut) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFFF3E0),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.WarningAmber,
                                    contentDescription = "Aviso",
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorMessage ?: "",
                                    color = Color(0xFFBF360C),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { ejecutarLogin() },
                        enabled = !isLockedOut && !isLoading,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (isLockedOut) "Bloqueado ($remainingSeconds s)" else "Iniciar Sesión Segura",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Selector rápido de roles para demostración en clase
            Text(
                text = "Credenciales para Evaluación (Demo en Clase):",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6B7280),
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ChipCredencial(
                    titulo = "Admin",
                    subtitulo = "admin / admin123",
                    colorFondo = Color(0xFFE8F5E9),
                    colorTexto = Color(0xFF1B5E20),
                    onClick = {
                        username = "admin"
                        password = "admin123"
                        errorMessage = null
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))

                ChipCredencial(
                    titulo = "Operador",
                    subtitulo = "operador / operador123",
                    colorFondo = Color(0xFFF1F8E9),
                    colorTexto = Color(0xFF33691E),
                    onClick = {
                        username = "operador"
                        password = "operador123"
                        errorMessage = null
                    }
                )

                Spacer(modifier = Modifier.width(6.dp))

                ChipCredencial(
                    titulo = "Auditor",
                    subtitulo = "auditor / auditor123",
                    colorFondo = Color(0xFFE1F5FE),
                    colorTexto = Color(0xFF01579B),
                    onClick = {
                        username = "auditor"
                        password = "auditor123"
                        errorMessage = null
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Etiqueta de Ciberseguridad ISO / ISA
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.Shield,
                    contentDescription = "ISO 27400",
                    tint = Color(0xFF78909C),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Protegido por ISO 27400 & ISA/IEC 62443 • Hash PBKDF2 (10k)",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF78909C)
                )
            }
        }
    }
}

@Composable
private fun ChipCredencial(
    titulo: String,
    subtitulo: String,
    colorFondo: Color,
    colorTexto: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = colorFondo
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
            Text(text = titulo, fontWeight = FontWeight.Bold, color = colorTexto, fontSize = 11.sp)
            Text(text = subtitulo, color = colorTexto.copy(alpha = 0.8f), fontSize = 10.sp)
        }
    }
}
