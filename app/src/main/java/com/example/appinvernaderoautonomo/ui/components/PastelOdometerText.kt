package com.example.appinvernaderoautonomo.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

/**
 * Muestra texto numérico con efecto de odómetro (desplazamiento vertical suave de cifras).
 * Aplica la característica tipográfica 'tnum' (números tabulares de ancho fijo)
 * para evitar que los contenedores oscilen o vibren cuando los números cambian.
 */
@Composable
fun PastelOdometerText(
    value: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF142C1D),
    fontSize: TextUnit = 22.sp,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (char in value) {
            if (char.isDigit()) {
                AnimatedContent(
                    targetState = char,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInVertically { height -> height } togetherWith
                                    slideOutVertically { height -> -height }
                        } else {
                            slideInVertically { height -> -height } togetherWith
                                    slideOutVertically { height -> height }
                        }
                    },
                    label = "OdometerDigit_$char"
                ) { digit ->
                    Text(
                        text = digit.toString(),
                        fontSize = fontSize,
                        fontWeight = fontWeight,
                        color = color,
                        style = TextStyle(
                            fontFeatureSettings = "tnum"
                        )
                    )
                }
            } else {
                Text(
                    text = char.toString(),
                    fontSize = fontSize,
                    fontWeight = fontWeight,
                    color = color,
                    style = TextStyle(
                        fontFeatureSettings = "tnum"
                    )
                )
            }
        }
    }
}
