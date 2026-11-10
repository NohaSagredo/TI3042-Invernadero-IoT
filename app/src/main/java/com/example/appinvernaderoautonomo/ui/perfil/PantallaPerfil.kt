package com.example.appinvernaderoautonomo.ui.perfil

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.appinvernaderoautonomo.ui.theme.AppInvernaderoAutonomoTheme

@Composable
fun PantallaPerfil(
    onIrAConfiguracion: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estados de la pantalla de perfil
    var nombreUsuario by rememberSaveable { mutableStateOf("Noha Sagredo") }
    var interesTecnologia by rememberSaveable { mutableStateOf(true) }
    var interesMusica by rememberSaveable { mutableStateOf(true) }
    var interesDeportes by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Perfil de Usuario",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        // 1. Imagen circular con foto de perfil (placeholder vector)
        Image(
            imageVector = Icons.Rounded.AccountCircle,
            contentDescription = "Foto de perfil",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
        )

        // 2. Campo editable con el nombre de usuario
        OutlinedTextField(
            value = nombreUsuario,
            onValueChange = { nombreUsuario = it },
            label = { Text("Nombre de usuario") },
            leadingIcon = {
                Icon(imageVector = Icons.Rounded.Person, contentDescription = null)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Tarjeta con Row de 3 intereses seleccionables mediante Checkbox
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Áreas de Interés",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    InteresCheckboxItem(
                        titulo = "Tecnología",
                        seleccionado = interesTecnologia,
                        onCheckedChange = { interesTecnologia = it }
                    )
                    InteresCheckboxItem(
                        titulo = "Música",
                        seleccionado = interesMusica,
                        onCheckedChange = { interesMusica = it }
                    )
                    InteresCheckboxItem(
                        titulo = "Deportes",
                        seleccionado = interesDeportes,
                        onCheckedChange = { interesDeportes = it }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // 4. Botón para navegar a la pantalla de configuración
        Button(
            onClick = onIrAConfiguracion,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = "Ir a Configuración",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun InteresCheckboxItem(
    titulo: String,
    seleccionado: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable { onCheckedChange(!seleccionado) }
    ) {
        Checkbox(
            checked = seleccionado,
            onCheckedChange = onCheckedChange
        )
        Text(
            text = titulo,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PantallaPerfilPreview() {
    AppInvernaderoAutonomoTheme {
        Surface {
            PantallaPerfil(onIrAConfiguracion = {})
        }
    }
}
