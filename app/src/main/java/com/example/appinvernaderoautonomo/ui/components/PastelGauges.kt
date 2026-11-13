package com.example.appinvernaderoautonomo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Medidor tacométrico semicircular (180°) en paleta pastel.
 * Visualiza la relación entre el valor medido y el rango óptimo del cultivo.
 */
@Composable
fun PastelArcGauge(
    valor: Float,
    minimo: Float,
    maximo: Float,
    unidad: String,
    estaEnRango: Boolean,
    colorArco: Color,
    modifier: Modifier = Modifier
) {
    val rangoTotal = (maximo - minimo).coerceAtLeast(1f)
    val fraccion = ((valor - minimo) / rangoTotal).coerceIn(0f, 1f)

    // Animación elástica del arco de llenado
    val fraccionAnimada by animateFloatAsState(
        targetValue = fraccion,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "GaugeArcFraction"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val w = size.width
                val h = size.height

                // Geometría semicircular pura con márgenes de seguridad para no cortar bordes ni puntero
                val radius = minOf((w / 2f) - strokeWidth - 8.dp.toPx(), h - strokeWidth - 8.dp.toPx())
                val centerX = w / 2f
                val centerY = h - 8.dp.toPx()

                val topLeft = Offset(centerX - radius, centerY - radius)
                val arcSize = Size(radius * 2, radius * 2)

                // 1. Arco base de fondo (Gris menta pastel suave)
                drawArc(
                    color = Color(0xFFE2EFE6),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 2. Arco activo coloreado (Verde / Celeste / Coral pastel)
                val sweep = 180f * fraccionAnimada
                drawArc(
                    color = colorArco,
                    startAngle = 180f,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // 3. Puntero alineado con precisión trigonométrica en el extremo del arco activo
                val rad = (180f + sweep) * (PI / 180f).toFloat()
                val needleX = centerX + cos(rad) * radius
                val needleY = centerY + sin(rad) * radius

                drawCircle(
                    color = Color.White,
                    radius = strokeWidth * 0.44f,
                    center = Offset(needleX, needleY)
                )
                drawCircle(
                    color = colorArco,
                    radius = strokeWidth * 0.26f,
                    center = Offset(needleX, needleY)
                )
            }

            // Valor numérico centrado limpiamente en el área hueca del arco
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                PastelOdometerText(
                    value = String.format("%.1f", valor).replace(',', '.'),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF142C1D)
                )
                Text(
                    text = unidad,
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF52796F)
                )
            }
        }
    }
}
