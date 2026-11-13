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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.sin

/**
 * Maqueta gráfica interactiva de la cámara rectangular de policarbonato transparente:
 * - Extremo izquierdo: Compuerta batiente de ventilación (Servo SG90).
 * - Extremo derecho: Ventilador extractor 12V (Driver MOSFET PWM).
 * - Interior: Cultivo de plantas, sustrato con sensor FC-28 insertado en el suelo,
 *   y sensor DHT22 midiendo el microclima ambiental.
 * - Flujo de ventilación cruzada en tiempo real cuando la compuerta y/o ventilador están activos.
 */
@Composable
fun InvernaderoChamberVisualizer(
    anguloCompuerta: Int,
    velocidadFanPwm: Int,
    humedadSueloRaw: Int,
    temperatura: Float,
    modifier: Modifier = Modifier
) {
    val anguloAnimado by animateFloatAsState(
        targetValue = anguloCompuerta.coerceIn(0, 90).toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ChamberHatchAngle"
    )

    val estaVentiladorGirando = velocidadFanPwm > 5
    val duracionGiro = if (estaVentiladorGirando) {
        (2200 - ((velocidadFanPwm / 255f) * 1850)).toInt().coerceIn(260, 2400)
    } else {
        12_000
    }

    val transition = rememberInfiniteTransition(label = "ChamberAirflowTransition")
    val fanRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duracionGiro, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ChamberFanRotation"
    )

    val airflowPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CrossAirflowPhase"
    )

    // Balanceo suave de las plantas al circular el aire
    val plantSway by transition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "InternalPlantSway"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xF4FFFFFF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD8E8DE))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Encabezado descriptivo de la maqueta física
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF4CAF50), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MAQUETA DIGITAL: CÁMARA DE POLICARBONATO",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4D28),
                        letterSpacing = 0.5.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "VENTILACIÓN CRUZADA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas de la caja rectangular de policarbonato con extremos funcionales
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(175.dp)
                    .background(Color(0xFFF7FBF8), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFDCE9E0), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Geometría de la caja rectangular de policarbonato
                    val boxMarginX = w * 0.11f
                    val boxMarginTop = h * 0.16f
                    val boxMarginBottom = h * 0.88f
                    val boxWidth = w * 0.78f
                    val boxHeight = boxMarginBottom - boxMarginTop
                    val cornerRadius = 8.dp.toPx()

                    val boxTopLeft = Offset(boxMarginX, boxMarginTop)
                    val boxSize = Size(boxWidth, boxHeight)

                    // 1. Fondo interior transparente translúcido del policarbonato
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x28BAE6FD), // Reflejo celeste superior
                                Color(0x35E0F2FE),
                                Color(0x40D8F3DC)  // Menta translúcido
                            ),
                            startY = boxMarginTop,
                            endY = boxMarginBottom
                        ),
                        topLeft = boxTopLeft,
                        size = boxSize,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                    )

                    // Nervaduras verticales alveolares típicas del policarbonato alveolar
                    val ribCount = 14
                    val ribSpacing = boxWidth / (ribCount + 1)
                    for (i in 1..ribCount) {
                        val ribX = boxMarginX + (i * ribSpacing)
                        drawLine(
                            color = Color(0x20FFFFFF),
                            start = Offset(ribX, boxMarginTop + 4f),
                            end = Offset(ribX, boxMarginBottom - 4f),
                            strokeWidth = 1.2f
                        )
                    }

                    // 2. Capa de Sustrato / Suelo en la base interior
                    val soilHeight = boxHeight * 0.24f
                    val soilTop = boxMarginBottom - soilHeight
                    val soilPath = Path().apply {
                        moveTo(boxMarginX + 2f, soilTop)
                        cubicTo(
                            boxMarginX + boxWidth * 0.35f, soilTop - 3f,
                            boxMarginX + boxWidth * 0.65f, soilTop + 2f,
                            boxMarginX + boxWidth - 2f, soilTop
                        )
                        lineTo(boxMarginX + boxWidth - 2f, boxMarginBottom - 2f)
                        lineTo(boxMarginX + 2f, boxMarginBottom - 2f)
                        close()
                    }
                    drawPath(
                        path = soilPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF92400E), Color(0xFF78350F)),
                            startY = soilTop,
                            endY = boxMarginBottom
                        )
                    )

                    // 3. Sensor FC-28 incrustado en el suelo
                    val fc28X = boxMarginX + boxWidth * 0.28f
                    val fc28Top = soilTop - 18f
                    // Placa del sensor con terminales
                    drawRect(
                        color = Color(0xFF1E3A8A), // PCB azul
                        topLeft = Offset(fc28X - 5f, fc28Top),
                        size = Size(10f, 14f)
                    )
                    // Horquilla metálica que penetra en la tierra
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(fc28X - 2.5f, fc28Top + 14f),
                        end = Offset(fc28X - 2.5f, fc28Top + 30f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color(0xFFCBD5E1),
                        start = Offset(fc28X + 2.5f, fc28Top + 14f),
                        end = Offset(fc28X + 2.5f, fc28Top + 30f),
                        strokeWidth = 2f
                    )
                    // LED de estado del FC-28
                    val fc28LedColor = if (humedadSueloRaw <= 850) Color(0xFF4CAF50) else Color(0xFFEF4444)
                    drawCircle(
                        color = fc28LedColor,
                        radius = 2f,
                        center = Offset(fc28X, fc28Top + 5f)
                    )

                    // 4. Plantas interiores floreciendo en el suelo
                    val plantPositions = listOf(0.48f, 0.68f)
                    for ((idx, posRel) in plantPositions.withIndex() ) {
                        val plantBaseX = boxMarginX + (boxWidth * posRel)
                        val plantBaseY = soilTop
                        val swayFactor = if (estaVentiladorGirando || anguloAnimado > 15f) plantSway else (plantSway * 0.25f)
                        val currentPlantSway = (swayFactor * (if (idx == 0) 1f else -0.8f))

                        val stemHeight = boxHeight * 0.42f
                        val stemTopX = plantBaseX + currentPlantSway
                        val stemTopY = plantBaseY - stemHeight

                        // Tallo principal
                        val pathTallo = Path().apply {
                            moveTo(plantBaseX, plantBaseY)
                            quadraticTo(
                                plantBaseX + (currentPlantSway * 0.5f),
                                plantBaseY - (stemHeight * 0.5f),
                                stemTopX,
                                stemTopY
                            )
                        }
                        drawPath(
                            path = pathTallo,
                            color = Color(0xFF2D6A4F),
                            style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                        )

                        // Hojas en verde menta
                        val leafY1 = plantBaseY - stemHeight * 0.40f
                        drawCircle(Color(0xFF52B788), radius = 6.5f, center = Offset(plantBaseX - 8f, leafY1))
                        val leafY2 = plantBaseY - stemHeight * 0.65f
                        drawCircle(Color(0xFF40916C), radius = 6.5f, center = Offset(plantBaseX + 8f, leafY2))
                        // Brote superior
                        drawCircle(Color(0xFF74C69D), radius = 5.5f, center = Offset(stemTopX, stemTopY))
                    }

                    // 5. Sensor DHT22 suspendido en el aire interior
                    val dhtX = boxMarginX + boxWidth * 0.48f
                    val dhtY = boxMarginTop + 14f
                    // Cable desde el techo
                    drawLine(
                        color = Color(0xFF475569),
                        start = Offset(dhtX, boxMarginTop),
                        end = Offset(dhtX, dhtY),
                        strokeWidth = 1.5f
                    )
                    // Módulo blanco ranurado del DHT22
                    drawRoundRect(
                        color = Color.White,
                        topLeft = Offset(dhtX - 6f, dhtY),
                        size = Size(12f, 15f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        color = Color(0xFF94A3B8),
                        topLeft = Offset(dhtX - 6f, dhtY),
                        size = Size(12f, 15f),
                        cornerRadius = CornerRadius(2f, 2f),
                        style = Stroke(width = 1f)
                    )

                    // 6. EXTREMO IZQUIERDO: Compuerta batiente de policarbonato (Servo SG90)
                    val hatchHingeX = boxMarginX
                    val hatchHingeY = boxMarginTop + (boxHeight * 0.18f)
                    val hatchLength = boxHeight * 0.46f

                    // Hueco/Vano de ventilación en la pared izquierda
                    drawLine(
                        color = Color(0x4038BDF8),
                        start = Offset(boxMarginX, hatchHingeY),
                        end = Offset(boxMarginX, hatchHingeY + hatchLength),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )

                    // Panel batiente abriéndose hacia afuera (hacia la izquierda)
                    // 0° = vertical cerrada; hasta 90° = abierta horizontal
                    rotate(degrees = -anguloAnimado, pivot = Offset(hatchHingeX, hatchHingeY)) {
                        // Lámina de policarbonato de la compuerta
                        drawLine(
                            color = Color(0xFF4CAF50),
                            start = Offset(hatchHingeX, hatchHingeY),
                            end = Offset(hatchHingeX, hatchHingeY + hatchLength),
                            strokeWidth = 5.5f,
                            cap = StrokeCap.Round
                        )
                        // Borde vidriado
                        drawLine(
                            color = Color(0xFFD8F3DC),
                            start = Offset(hatchHingeX - 2f, hatchHingeY + 2f),
                            end = Offset(hatchHingeX - 2f, hatchHingeY + hatchLength - 2f),
                            strokeWidth = 2.5f
                        )
                    }
                    // Bisagra del servo
                    drawCircle(Color(0xFF1E293B), radius = 4f, center = Offset(hatchHingeX, hatchHingeY))

                    // 7. EXTREMO DERECHO OPUESTO: Ventilador Extractor 12V con MOSFET
                    val fanCenterX = boxMarginX + boxWidth
                    val fanCenterY = boxMarginTop + (boxHeight * 0.40f)
                    val fanRadius = boxHeight * 0.22f

                    // Carcasa cilíndrica del extractor incrustada en la pared opuesta
                    drawCircle(
                        color = Color(0xFFE2E8F0),
                        radius = fanRadius,
                        center = Offset(fanCenterX, fanCenterY)
                    )
                    drawCircle(
                        color = Color(0xFF64748B),
                        radius = fanRadius,
                        center = Offset(fanCenterX, fanCenterY),
                        style = Stroke(width = 2.5f)
                    )

                    // Aspas giratorias del ventilador
                    val currentFanRot = if (estaVentiladorGirando) fanRotation else 25f
                    rotate(degrees = currentFanRot, pivot = Offset(fanCenterX, fanCenterY)) {
                        val bladeCount = 4
                        val bladeLen = fanRadius * 0.78f
                        val bladeColor = if (estaVentiladorGirando) Color(0xFF38BDF8) else Color(0xFF94A3B8)
                        for (b in 0 until bladeCount) {
                            rotate(degrees = b * 90f, pivot = Offset(fanCenterX, fanCenterY)) {
                                drawLine(
                                    color = bladeColor,
                                    start = Offset(fanCenterX, fanCenterY),
                                    end = Offset(fanCenterX, fanCenterY - bladeLen),
                                    strokeWidth = 3.5f,
                                    cap = StrokeCap.Round
                                )
                            }
                        }
                    }
                    // Núcleo del motor DC
                    drawCircle(Color(0xFF334155), radius = fanRadius * 0.32f, center = Offset(fanCenterX, fanCenterY))
                    drawCircle(
                        color = if (estaVentiladorGirando) Color(0xFFFDE047) else Color(0xFF64748B),
                        radius = fanRadius * 0.16f,
                        center = Offset(fanCenterX, fanCenterY)
                    )

                    // 8. FLUJO DE AIRE CRUZADO (Entra por compuerta izq -> barre plantas -> sale por ventilador der)
                    val hayFlujoActivo = anguloAnimado > 10f || estaVentiladorGirando
                    if (hayFlujoActivo) {
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), airflowPhase * 22f)
                        val flujoColor = Color(0x9538BDF8)

                        // 3 líneas de corriente cruzada
                        val yOffsets = listOf(0.32f, 0.44f, 0.56f)
                        for (yRel in yOffsets) {
                            val streamY = boxMarginTop + (boxHeight * yRel)
                            val pathAir = Path().apply {
                                moveTo(boxMarginX - 16f, streamY - 4f)
                                cubicTo(
                                    boxMarginX + boxWidth * 0.30f, streamY - 8f,
                                    boxMarginX + boxWidth * 0.70f, streamY + 6f,
                                    boxMarginX + boxWidth + 24f, streamY
                                )
                            }
                            drawPath(
                                path = pathAir,
                                color = flujoColor,
                                style = Stroke(width = 2.2f, pathEffect = dashEffect, cap = StrokeCap.Round)
                            )
                        }
                    }

                    // 9. Marco estructural exterior de aluminio / soportes esquineros de la caja
                    drawRoundRect(
                        color = Color(0xFF64748B),
                        topLeft = boxTopLeft,
                        size = boxSize,
                        cornerRadius = CornerRadius(cornerRadius, cornerRadius),
                        style = Stroke(width = 3.5f)
                    )
                    // Reflejo biselado de cristal en el borde superior
                    drawLine(
                        color = Color(0x99FFFFFF),
                        start = Offset(boxMarginX + 12f, boxMarginTop + 2.5f),
                        end = Offset(boxMarginX + boxWidth - 12f, boxMarginTop + 2.5f),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Leyendas explicativas de los extremos y elementos interiores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text(
                        text = "EXTREMO IZQUIERDO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Compuerta Batiente ($anguloCompuerta°)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B4D28)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "INTERIOR",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Cultivo + FC-28 + DHT22",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1B4D28)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EXTREMO DERECHO",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    val fanPct = ((velocidadFanPwm / 255f) * 100).toInt()
                    Text(
                        text = "Ventilador ($fanPct%)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0369A1)
                    )
                }
            }
        }
    }
}
