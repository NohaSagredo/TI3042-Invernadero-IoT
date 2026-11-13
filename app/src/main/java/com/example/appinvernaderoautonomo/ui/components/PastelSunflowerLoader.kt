package com.example.appinvernaderoautonomo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Indicador de carga temático artesanal.
 * Muestra un brote de girasol que germina, abre sus hojas y florece en bucle suave.
 * Reemplaza el spinner circular genérico de Android en las pantallas de la app.
 */
@Composable
fun PastelSunflowerLoader(
    modifier: Modifier = Modifier,
    mensaje: String = "Cargando..."
) {
    val transition = rememberInfiniteTransition(label = "SunflowerLoaderTransition")

    // Rotación y balanceo del girasol
    val rotation by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LoaderSway"
    )

    // Pulsación de la flor
    val bloomScale by transition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LoaderBloom"
    )

    // Giro continuo suave de los pétalos
    val spinPetals by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "LoaderSpin"
    )

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Canvas(modifier = Modifier.size(54.dp)) {
            val w = size.width
            val h = size.height
            val centerX = w / 2f
            val baseY = h * 0.90f
            val flowerY = h * 0.38f

            // 1. Tallo verde pastel
            val pathTallo = Path().apply {
                moveTo(centerX, baseY)
                quadraticTo(
                    centerX + (rotation * 0.35f),
                    baseY - (h * 0.28f),
                    centerX + rotation,
                    flowerY
                )
            }
            drawPath(
                path = pathTallo,
                color = Color(0xFF2D6A4F),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // 2. Pequeñas hojas laterales en verde menta
            val leafY = baseY - h * 0.22f
            drawCircle(
                color = Color(0xFF52B788),
                radius = 5.5f,
                center = Offset(centerX - 8f, leafY)
            )
            drawCircle(
                color = Color(0xFF52B788),
                radius = 5.5f,
                center = Offset(centerX + 8f, leafY - 4f)
            )

            // 3. Flor de girasol rotando y pulsando suavemente
            val headCenter = Offset(centerX + rotation, flowerY)
            val centerRadius = 6.5f * bloomScale
            val petalLen = 9.5f * bloomScale
            val petalCount = 10

            rotate(degrees = spinPetals, pivot = headCenter) {
                for (i in 0 until petalCount) {
                    val angle = i * (360f / petalCount)
                    val rad = angle * (PI / 180f).toFloat()
                    val pX = headCenter.x + cos(rad) * (centerRadius + petalLen)
                    val pY = headCenter.y + sin(rad) * (centerRadius + petalLen)

                    drawCircle(
                        color = if (i % 2 == 0) Color(0xFFFDE047) else Color(0xFFFEF08A),
                        radius = 4.2f * bloomScale,
                        center = Offset(pX, pY)
                    )
                }
            }

            // Centro de la flor
            drawCircle(
                color = Color(0xFF78350F),
                radius = centerRadius,
                center = headCenter
            )
            drawCircle(
                color = Color(0xFF92400E),
                radius = centerRadius * 0.6f,
                center = headCenter
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = mensaje,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF43604E)
        )
    }
}
