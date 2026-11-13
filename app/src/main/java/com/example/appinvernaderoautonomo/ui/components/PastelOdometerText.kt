package com.example.appinvernaderoautonomo.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Muestra texto numérico con efecto de odómetro suave (deslizamiento vertical con desvanecimiento).
 * Utiliza números tabulares 'tnum' para asegurar un ancho monospaciado en cifras
 * sin deformar la tipografía ni alterar el flujo del contenedor.
 */
@Composable
fun PastelOdometerText(
    value: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF142C1D),
    fontSize: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    AnimatedContent(
        targetState = value,
        transitionSpec = {
            (slideInVertically { height -> height / 3 } + fadeIn()) togetherWith
                    (slideOutVertically { height -> -height / 3 } + fadeOut())
        },
        label = "PastelOdometerAnimation",
        modifier = modifier
    ) { targetValue ->
        Text(
            text = targetValue,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            style = TextStyle(
                fontFeatureSettings = "tnum"
            )
        )
    }
}
