package com.example.appinvernaderoautonomo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visualizador dinámico en Canvas de la compuerta cenital batiente del invernadero.
 * Refleja en tiempo real la apertura angular del servomotor (0° a 90°).
 */
@Composable
fun CompuertaVentanaVisualizer(
    angulo: Int,
    modifier: Modifier = Modifier
) {
    // Animación suave del ángulo con amortiguación elástica de resorte
    val anguloAnimado by animateFloatAsState(
        targetValue = angulo.coerceIn(0, 90).toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CompuertaAnguloAnim"
    )

    // Animación de partículas de brisa/aire si la compuerta está abierta
    val transition = rememberInfiniteTransition(label = "FlujoAireBrisa")
    val brisaPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "BrisaPhase"
    )

    Row(
        modifier = modifier
            .background(Color(0xFFF1F8F4), shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFD4E8DC), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.size(width = 110.dp, height = 75.dp)) {
            val w = size.width
            val h = size.height

            // 1. Marco lateral de la caja rectangular de policarbonato
            val marcoColor = Color(0xFF64748B)
            val vigaColor = Color(0xFFCBD5E1)
            val paredX = w * 0.42f
            val baseCajaY = h * 0.88f
            val techoCajaY = h * 0.16f

            // Pared vertical fija inferior de policarbonato
            drawLine(
                color = marcoColor,
                start = Offset(paredX, h * 0.62f),
                end = Offset(paredX, baseCajaY),
                strokeWidth = 4.5f,
                cap = StrokeCap.Round
            )
            // Techo horizontal superior de la caja
            drawLine(
                color = marcoColor,
                start = Offset(paredX, techoCajaY),
                end = Offset(w * 0.95f, techoCajaY),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )
            // Base horizontal de la caja
            drawLine(
                color = vigaColor,
                start = Offset(paredX, baseCajaY),
                end = Offset(w * 0.95f, baseCajaY),
                strokeWidth = 3.5f,
                cap = StrokeCap.Round
            )

            // Eje de la bisagra superior de la compuerta
            val bisagraX = paredX
            val bisagraY = techoCajaY + 4f
            drawCircle(
                color = Color(0xFF1E293B),
                radius = 5.5f,
                center = Offset(bisagraX, bisagraY)
            )

            // 2. Compuerta Batiente de Policarbonato (Abre hacia afuera / hacia la izquierda)
            val largoCompuerta = h * 0.48f

            rotate(degrees = -anguloAnimado, pivot = Offset(bisagraX, bisagraY)) {
                val panelEndY = bisagraY + largoCompuerta

                // Cristal translúcido de la lámina de policarbonato
                val pathVidrio = Path().apply {
                    moveTo(bisagraX - 6f, bisagraY)
                    lineTo(bisagraX + 2f, bisagraY)
                    lineTo(bisagraX + 2f, panelEndY)
                    lineTo(bisagraX - 6f, panelEndY)
                    close()
                }
                drawPath(path = pathVidrio, color = Color(0x60BAE6FD))

                // Marco exterior de la compuerta batiente (Verde menta pastel)
                drawLine(
                    color = Color(0xFF4CAF50),
                    start = Offset(bisagraX, bisagraY),
                    end = Offset(bisagraX, panelEndY),
                    strokeWidth = 5.5f,
                    cap = StrokeCap.Round
                )

                // Brazo/Varilla de empuje del servomotor SG90
                drawLine(
                    color = Color(0xFF2E7D32),
                    start = Offset(bisagraX, bisagraY + largoCompuerta * 0.55f),
                    end = Offset(bisagraX + 16f, bisagraY + largoCompuerta * 0.55f),
                    strokeWidth = 2.5f,
                    cap = StrokeCap.Round
                )
            }

            // 3. Flechas animadas de flujo de entrada cuando la compuerta está abierta
            if (anguloAnimado > 10f) {
                val airColor = Color(0x9538BDF8)
                val arrowCount = 3
                for (i in 0 until arrowCount) {
                    val progress = (brisaPhase + (i * 0.33f)) % 1f
                    val startX = paredX - 28f + (progress * 45f)
                    val currentY = techoCajaY + 16f + (i * 12f)

                    drawCircle(
                        color = airColor,
                        radius = 2.5f * (1f - progress * 0.3f),
                        center = Offset(startX, currentY)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Badge textual con grado de inclinación
        Column(horizontalAlignment = Alignment.End) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (angulo > 10) Color(0xFFD8F3DC) else Color(0xFFE2E8F0)
            ) {
                Text(
                    text = "${angulo}°",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (angulo > 10) Color(0xFF1B4D28) else Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (angulo == 0) "Compuerta Cerrada" else "Apertura Ventilada",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF52796F)
            )
        }
    }
}

/**
 * Visualizador dinámico en Canvas del ventilador extractor 12V MOSFET.
 * Hace girar las aspas vectoriales en proporción exacta a la velocidad PWM recibida.
 */
@Composable
fun VentiladorMosfetVisualizer(
    pwm: Int,
    modifier: Modifier = Modifier
) {
    val porcentajePwm = (pwm.coerceIn(0, 255) / 255f)
    val estaGirando = pwm > 5

    // Duración de giro inversamente proporcional a la potencia PWM
    val duracionGiro = if (estaGirando) {
        (2200 - (porcentajePwm * 1850)).toInt().coerceIn(280, 2400)
    } else {
        10_000
    }

    val transition = rememberInfiniteTransition(label = "AspasVentiladorRotation")
    val anguloAspas by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duracionGiro, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FanRotationAngle"
    )

    Row(
        modifier = modifier
            .background(Color(0xFFF1F8F4), shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFD4E8DC), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.size(72.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = (size.width / 2f) - 4f
            val hubRadius = outerRadius * 0.28f

            // 1. Carcasa exterior del ventilador (Gris azulado pastel con bisel)
            drawCircle(
                color = Color(0xFFE2E8F0),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF94A3B8),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 2.5f)
            )

            // Rejilla de protección concéntrica
            drawCircle(
                color = Color(0x40CBD5E1),
                radius = outerRadius * 0.65f,
                center = center,
                style = Stroke(width = 1.2f)
            )

            // 2. Aspas de extracción (Giran según el ángulo animado o quedan estáticas)
            val rotacionActual = if (estaGirando) anguloAspas else 15f
            rotate(degrees = rotacionActual, pivot = center) {
                val bladeCount = 4
                val bladeLength = outerRadius * 0.72f
                val bladeColor = if (estaGirando) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                val bladeHighlight = if (estaGirando) Color(0xFFBAE6FD) else Color(0xFFCBD5E1)

                for (b in 0 until bladeCount) {
                    rotate(degrees = b * (360f / bladeCount), pivot = center) {
                        val pathAspa = Path().apply {
                            moveTo(center.x - hubRadius * 0.45f, center.y - hubRadius)
                            quadraticTo(
                                center.x + bladeLength * 0.55f,
                                center.y - bladeLength * 0.75f,
                                center.x,
                                center.y - bladeLength
                            )
                            quadraticTo(
                                center.x - bladeLength * 0.35f,
                                center.y - bladeLength * 0.45f,
                                center.x + hubRadius * 0.45f,
                                center.y - hubRadius
                            )
                            close()
                        }
                        drawPath(path = pathAspa, color = bladeColor)
                        drawPath(path = pathAspa, color = bladeHighlight, style = Stroke(width = 1.2f))
                    }
                }
            }

            // 3. Rotor central del motor DC 12V
            drawCircle(
                color = Color(0xFF475569),
                radius = hubRadius,
                center = center
            )
            drawCircle(
                color = if (estaGirando) Color(0xFFFDE047) else Color(0xFF64748B),
                radius = hubRadius * 0.45f,
                center = center
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Badge textual con porcentaje y velocidad
        Column(horizontalAlignment = Alignment.End) {
            val porcentajeInt = (porcentajePwm * 100).toInt()
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (estaGirando) Color(0xFFE0F2FE) else Color(0xFFE2E8F0)
            ) {
                Text(
                    text = "${porcentajeInt}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (estaGirando) Color(0xFF0369A1) else Color(0xFF475569),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = when {
                    pwm == 0 -> "Extractor Apagado"
                    pwm >= 200 -> "Flujo Forzado Máx"
                    else -> "Disipación Térmica"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF52796F)
            )
        }
    }
}
