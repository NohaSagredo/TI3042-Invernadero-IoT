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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Visualizador vectorial de alta fidelidad de la compuerta batiente del invernadero.
 * Incorpora:
 * - Panel alveolar de policarbonato con flautas/nervaduras interiores y perfil de aluminio.
 * - Servomotor analógico SG90 (carcasa azul translúcida, brazo de nylon y varilla roscada articulada).
 * - Arco de trayectoria angular (0° a 90°) con marcas de referencia mecánicas.
 * - Brisa dinámica con líneas de corriente que ingresan al recinto al abrirse.
 */
@Composable
fun CompuertaVentanaVisualizer(
    angulo: Int,
    modifier: Modifier = Modifier
) {
    val anguloAnimado by animateFloatAsState(
        targetValue = angulo.coerceIn(0, 90).toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "CompuertaAnguloAnim"
    )

    val transition = rememberInfiniteTransition(label = "FlujoAireBrisa")
    val brisaPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
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
        Canvas(modifier = Modifier.size(width = 120.dp, height = 82.dp)) {
            val w = size.width
            val h = size.height

            val paredX = w * 0.44f
            val baseCajaY = h * 0.90f
            val techoCajaY = h * 0.14f

            // 1. Estructura de aluminio extruido del marco del invernadero
            val frameColor = Color(0xFF475569)
            val subFrameColor = Color(0xFF94A3B8)

            // Viga horizontal superior
            drawLine(
                color = frameColor,
                start = Offset(paredX, techoCajaY),
                end = Offset(w * 0.98f, techoCajaY),
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
            // Viga horizontal inferior de fijación
            drawLine(
                color = subFrameColor,
                start = Offset(paredX, baseCajaY),
                end = Offset(w * 0.98f, baseCajaY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            // Pilar vertical inferior (debajo del vano de la compuerta)
            drawLine(
                color = frameColor,
                start = Offset(paredX, h * 0.68f),
                end = Offset(paredX, baseCajaY),
                strokeWidth = 4.5f,
                cap = StrokeCap.Round
            )
            // Junta de sellado / goma estanca en el vano
            drawLine(
                color = Color(0xFF334155),
                start = Offset(paredX - 2f, techoCajaY + 6f),
                end = Offset(paredX - 2f, h * 0.68f),
                strokeWidth = 2f,
                cap = StrokeCap.Square
            )

            // 2. Arco de trayectoria angular (0° a 90°)
            val bisagraX = paredX
            val bisagraY = techoCajaY + 4f
            val radioArco = h * 0.50f
            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)

            // Arco guía con marcas cada 45°
            drawArc(
                color = Color(0x6094A3B8),
                startAngle = 90f,
                sweepAngle = -90f,
                useCenter = false,
                topLeft = Offset(bisagraX - radioArco, bisagraY - radioArco),
                size = Size(radioArco * 2f, radioArco * 2f),
                style = Stroke(width = 1.2f, pathEffect = dashEffect)
            )

            // 3. Servomotor SG90 montado en el interior de la pared
            val servoX = paredX + 16f
            val servoY = bisagraY + 22f
            val servoWidth = 14f
            val servoHeight = 22f

            // Carcasa azul translúcida característica del SG90
            drawRoundRect(
                color = Color(0xFF0284C7),
                topLeft = Offset(servoX, servoY),
                size = Size(servoWidth, servoHeight),
                cornerRadius = CornerRadius(2f, 2f)
            )
            // Pestañas de montaje con orificio
            drawRect(
                color = Color(0xFF0369A1),
                topLeft = Offset(servoX - 3f, servoY + 3f),
                size = Size(3f, servoHeight - 6f)
            )
            // Engranaje de salida superior (piñón de latón dorado)
            drawCircle(
                color = Color(0xFFF59E0B),
                radius = 3.5f,
                center = Offset(servoX + 5f, servoY + 4f)
            )

            // Brazo oscilante de nylon blanco del servo (gira con el ángulo)
            val servoArmAngle = -(anguloAnimado * 0.9f)
            val armLength = 11f
            val armEndX = (servoX + 5f) - armLength * sin(servoArmAngle * PI.toFloat() / 180f)
            val armEndY = (servoY + 4f) + armLength * cos(servoArmAngle * PI.toFloat() / 180f)

            drawLine(
                color = Color(0xFFF8FAFC),
                start = Offset(servoX + 5f, servoY + 4f),
                end = Offset(armEndX, armEndY),
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
            drawCircle(Color(0xFFE2E8F0), radius = 1.8f, center = Offset(armEndX, armEndY))

            // 4. Panel batiente de Policarbonato Alveolar (Doble pared celular)
            val largoCompuerta = radioArco
            val espesorCompuerta = 8f

            rotate(degrees = -anguloAnimado, pivot = Offset(bisagraX, bisagraY)) {
                val panelEndY = bisagraY + largoCompuerta

                // Lámina de policarbonato translúcido (celeste agua)
                val pathPanel = Path().apply {
                    moveTo(bisagraX - espesorCompuerta, bisagraY)
                    lineTo(bisagraX, bisagraY)
                    lineTo(bisagraX, panelEndY)
                    lineTo(bisagraX - espesorCompuerta, panelEndY)
                    close()
                }
                drawPath(
                    path = pathPanel,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color(0x80BAE6FD), Color(0x35E0F2FE)),
                        startX = bisagraX - espesorCompuerta,
                        endX = bisagraX
                    )
                )

                // Flautas / canales celulares internos del policarbonato alveolar
                val numFlautas = 4
                for (f in 1..numFlautas) {
                    val flautaY = bisagraY + (f * largoCompuerta / (numFlautas + 1))
                    drawLine(
                        color = Color(0x600284C7),
                        start = Offset(bisagraX - espesorCompuerta + 1.5f, flautaY),
                        end = Offset(bisagraX - 1.5f, flautaY),
                        strokeWidth = 1f
                    )
                }

                // Perfil perimetral de aluminio extruido anodizado (Verde oliva pastel)
                drawPath(
                    path = pathPanel,
                    color = Color(0xFF2E7D32),
                    style = Stroke(width = 1.8f)
                )

                // Línea de brillo especular
                drawLine(
                    color = Color(0xB0FFFFFF),
                    start = Offset(bisagraX - espesorCompuerta + 1f, bisagraY + 2f),
                    end = Offset(bisagraX - espesorCompuerta + 1f, panelEndY - 2f),
                    strokeWidth = 1f
                )

                // Rótula de anclaje de la varilla en la compuerta
                val anclajeCompuertaY = bisagraY + largoCompuerta * 0.45f
                drawCircle(
                    color = Color(0xFF334155),
                    radius = 2.2f,
                    center = Offset(bisagraX, anclajeCompuertaY)
                )

                // Varilla mecánica articulada (acero inoxidable) entre servo y compuerta
                drawLine(
                    color = Color(0xFF64748B),
                    start = Offset(armEndX, armEndY),
                    end = Offset(bisagraX, anclajeCompuertaY),
                    strokeWidth = 1.8f,
                    cap = StrokeCap.Round
                )
            }

            // Soporte y perno pivotante de la bisagra principal
            drawCircle(color = Color(0xFF1E293B), radius = 4.5f, center = Offset(bisagraX, bisagraY))
            drawCircle(color = Color(0xFFE2E8F0), radius = 2f, center = Offset(bisagraX, bisagraY))

            // 5. Corrientes dinámicas de brisa aerodinámica entrando al invernadero
            if (anguloAnimado > 8f) {
                val airColor = Color(0x9538BDF8)
                val breezeLines = 3
                for (i in 0 until breezeLines) {
                    val phase = (brisaPhase + (i * 0.32f)) % 1f
                    val yLevel = bisagraY + 12f + (i * 13f)
                    val curveStartX = paredX - 32f + (phase * 20f)
                    val curveEndX = paredX + 28f + (phase * 16f)

                    val airPath = Path().apply {
                        moveTo(curveStartX, yLevel - 4f)
                        quadraticTo(paredX - 6f, yLevel, curveEndX, yLevel + 3f)
                    }
                    drawPath(
                        path = airPath,
                        color = airColor.copy(alpha = (1f - phase) * 0.75f),
                        style = Stroke(width = 1.8f, cap = StrokeCap.Round)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Badge textual con grado de apertura y estado funcional
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
                text = when {
                    angulo == 0 -> "Cerrada Hermética"
                    angulo <= 45 -> "Brisa Atenuada"
                    else -> "Ventilación Cruzada"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF52796F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Visualizador vectorial de alta fidelidad del extractor forzado 12V MOSFET.
 * Incorpora:
 * - Chasis cuadrado industrial con 4 orificios esquineros y tornillos metálicos.
 * - Tobera aerodinámica con bisel concéntrico.
 * - Turbina de 7 aspas alabeadas con perfil aerodinámico de alta extracción.
 * - Eje central con disipador, embellecedor metálico y pistas de bobinado de cobre.
 * - Rejilla concéntrica protectora de alambre con tirantes radiales.
 * - Partículas de vórtice de flujo centrífugo y LED de estado en esquina.
 */
@Composable
fun VentiladorMosfetVisualizer(
    pwm: Int,
    modifier: Modifier = Modifier
) {
    val porcentajePwm = (pwm.coerceIn(0, 255) / 255f)
    val estaGirando = pwm > 5

    val duracionGiro = if (estaGirando) {
        (2000 - (porcentajePwm * 1720)).toInt().coerceIn(240, 2200)
    } else {
        12_000
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

    val vortexPhase by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = duracionGiro.coerceIn(400, 1500), easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "FanVortexPhase"
    )

    Row(
        modifier = modifier
            .background(Color(0xFFF1F8F4), shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFD4E8DC), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.size(76.dp)) {
            val w = size.width
            val h = size.height
            val center = Offset(w / 2f, h / 2f)
            val outerRadius = (w / 2f) - 4f
            val hubRadius = outerRadius * 0.30f

            // 1. Chasis cuadrado industrial moldeado (Grafito mate con esquinas redondeadas)
            val frameMargin = 2f
            val frameRect = Size(w - frameMargin * 2f, h - frameMargin * 2f)
            drawRoundRect(
                color = Color(0xFF334155),
                topLeft = Offset(frameMargin, frameMargin),
                size = frameRect,
                cornerRadius = CornerRadius(8f, 8f)
            )

            // 4 Tornillos metálicos esquineros de fijación M3
            val screwOffset = 7f
            val screwPositions = listOf(
                Offset(screwOffset, screwOffset),
                Offset(w - screwOffset, screwOffset),
                Offset(screwOffset, h - screwOffset),
                Offset(w - screwOffset, h - screwOffset)
            )
            for (sp in screwPositions) {
                // Pozo oscuro del tornillo
                drawCircle(color = Color(0xFF0F172A), radius = 3.2f, center = sp)
                // Cabeza de tornillo plateada con ranura
                drawCircle(color = Color(0xFF94A3B8), radius = 2.2f, center = sp)
                drawLine(Color(0xFF475569), start = Offset(sp.x - 1.2f, sp.y), end = Offset(sp.x + 1.2f, sp.y), strokeWidth = 0.8f)
            }

            // LED de estado del actuador en esquina superior derecha
            val ledPos = Offset(w - screwOffset, screwOffset + 9f)
            val ledColor = if (estaGirando) Color(0xFF10B981) else Color(0xFF64748B)
            if (estaGirando) {
                drawCircle(color = Color(0x4010B981), radius = 4f, center = ledPos)
            }
            drawCircle(color = ledColor, radius = 2f, center = ledPos)

            // 2. Tobera / Aro cilíndrico de admisión (Conducto circular con bisel)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A)),
                    center = center,
                    radius = outerRadius
                ),
                radius = outerRadius,
                center = center
            )
            drawCircle(
                color = Color(0xFF475569),
                radius = outerRadius,
                center = center,
                style = Stroke(width = 1.5f)
            )

            // 3. Turbina de 7 aspas aerodinámicas (Sickle blades de alta presión)
            val rotacionActual = if (estaGirando) anguloAspas else 18f
            rotate(degrees = rotacionActual, pivot = center) {
                val bladeCount = 7
                val bladeLength = outerRadius * 0.80f
                val bladeBaseWidth = hubRadius * 0.70f
                val bladeColor = if (estaGirando) Color(0xFF0284C7) else Color(0xFF64748B)
                val bladeHighlight = if (estaGirando) Color(0xFF7DD3FC) else Color(0xFF94A3B8)

                for (b in 0 until bladeCount) {
                    rotate(degrees = b * (360f / bladeCount), pivot = center) {
                        val pathAspa = Path().apply {
                            moveTo(center.x - bladeBaseWidth * 0.5f, center.y - hubRadius)
                            // Borde de ataque curvado con concavidad aerodinámica
                            cubicTo(
                                center.x - bladeLength * 0.25f, center.y - hubRadius * 1.5f,
                                center.x + bladeLength * 0.55f, center.y - bladeLength * 0.75f,
                                center.x + bladeLength * 0.15f, center.y - bladeLength
                            )
                            // Borde de salida y retorno al buje
                            quadraticTo(
                                center.x + bladeBaseWidth * 0.8f, center.y - bladeLength * 0.50f,
                                center.x + bladeBaseWidth * 0.5f, center.y - hubRadius
                            )
                            close()
                        }
                        drawPath(path = pathAspa, color = bladeColor)
                        drawPath(path = pathAspa, color = bladeHighlight, style = Stroke(width = 1f))
                    }
                }
            }

            // 4. Rejilla protectora metálica concéntrica con tirantes de soporte
            val grillColor = Color(0x35CBD5E1)
            drawCircle(color = grillColor, radius = outerRadius * 0.72f, center = center, style = Stroke(width = 1f))
            drawCircle(color = grillColor, radius = outerRadius * 0.48f, center = center, style = Stroke(width = 1f))
            // 4 tirantes de soporte de la rejilla
            drawLine(grillColor, start = Offset(center.x - outerRadius, center.y), end = Offset(center.x + outerRadius, center.y), strokeWidth = 1f)
            drawLine(grillColor, start = Offset(center.x, center.y - outerRadius), end = Offset(center.x, center.y + outerRadius), strokeWidth = 1f)

            // 5. Buje / Rotor central del motor brushless 12V
            drawCircle(color = Color(0xFF0F172A), radius = hubRadius, center = center)
            // Detalles de bobinas de cobre en el estator
            drawCircle(color = Color(0xFFD97706), radius = hubRadius * 0.75f, center = center, style = Stroke(width = 1.2f))
            // Tapa metálica central cepillada
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFE2E8F0), Color(0xFF64748B)),
                    center = center - Offset(1.5f, 1.5f),
                    radius = hubRadius * 0.50f
                ),
                radius = hubRadius * 0.50f,
                center = center
            )

            // 6. Vórtice dinámico de partículas de extracción
            if (estaGirando) {
                val particleCount = 4
                for (p in 0 until particleCount) {
                    val pPhase = (vortexPhase + (p * 0.25f)) % 1f
                    val pAngle = (rotacionActual + (p * 90f) + (pPhase * 60f)) * (PI.toFloat() / 180f)
                    val pDist = hubRadius + (pPhase * (outerRadius - hubRadius))
                    val px = center.x + pDist * cos(pAngle)
                    val py = center.y + pDist * sin(pAngle)

                    drawCircle(
                        color = Color(0x80BAE6FD).copy(alpha = (1f - pPhase) * porcentajePwm),
                        radius = 1.5f + (pPhase * 1.5f),
                        center = Offset(px, py)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Badge textual con porcentaje, potencia PWM y estado térmico
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
                    else -> "Extracción Continua"
                },
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF52796F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Visualizador vectorial de alta fidelidad del sensor de humedad de suelo FC-28.
 * Incorpora:
 * - Sonda de horquilla FR-4 con electrodos sumergidos en oro ENIG (oro químico).
 * - Estrato de sustrato/tierra con textura orgánica viva, perlita y partículas de humedad activa.
 * - Módulo comparador acondicionador LM393 con potenciómetro multivuelta, LED de encendido (PWR) y LED de detección (DO).
 * - Campo capacitivo/resistivo de detección entre los electrodos que se ilumina con la humedad.
 */
@Composable
fun SensorHumedadSueloVisualizer(
    raw: Int,
    estaEnRango: Boolean,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "SoilMoistureTransition")
    val waterPulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SoilWaterPulse"
    )

    // Cuanto menor es el raw, más húmedo está el sustrato (inversamente proporcional)
    val porcentajeHumedad = ((1023 - raw.coerceIn(0, 1023)) / 1023f)
    val esHumedo = raw <= 850

    Row(
        modifier = modifier
            .background(Color(0xFFF1F8F4), shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFD4E8DC), RoundedCornerShape(12.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Canvas(modifier = Modifier.size(width = 135.dp, height = 78.dp)) {
            val w = size.width
            val h = size.height

            // 1. Estrato de suelo/sustrato en corte transversal (Lado izquierdo)
            val soilWidth = w * 0.54f
            val soilTop = h * 0.26f
            val soilHeight = h - soilTop

            // Colores del suelo según hidratación (Tierra húmeda y rica vs Tierra seca y agrietada)
            val soilGradient = if (esHumedo) {
                listOf(Color(0xFF3E2723), Color(0xFF271A14))
            } else {
                listOf(Color(0xFF795548), Color(0xFF5D4037))
            }

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = soilGradient,
                    startY = soilTop,
                    endY = h
                ),
                topLeft = Offset(2f, soilTop),
                size = Size(soilWidth, soilHeight - 2f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Línea de corte de la superficie vegetal (mantillo orgánico)
            drawLine(
                color = if (esHumedo) Color(0xFF2E7D32) else Color(0xFF8D6E63),
                start = Offset(2f, soilTop),
                end = Offset(soilWidth + 2f, soilTop),
                strokeWidth = 2.5f,
                cap = StrokeCap.Round
            )

            // Perlita mineral y gotitas de humedad dispersas en el suelo
            val grainPositions = listOf(
                Offset(soilWidth * 0.20f, soilTop + 14f),
                Offset(soilWidth * 0.48f, soilTop + 24f),
                Offset(soilWidth * 0.78f, soilTop + 12f),
                Offset(soilWidth * 0.32f, soilTop + 38f),
                Offset(soilWidth * 0.65f, soilTop + 42f)
            )
            for ((idx, gp) in grainPositions.withIndex()) {
                // Perlita blanca
                drawCircle(color = Color(0x70FEF9C3), radius = 1.3f, center = gp)
                // Microgotas activas de agua si el suelo está húmedo
                if (esHumedo) {
                    val dropPulse = (waterPulse + (idx * 0.2f)) % 1f
                    drawCircle(
                        color = Color(0x9038BDF8).copy(alpha = 0.4f + dropPulse * 0.5f),
                        radius = 1.8f + (dropPulse * 0.8f),
                        center = gp + Offset(5f, -3f)
                    )
                }
            }

            // 2. Sonda FC-28 incrustada en el suelo
            val probeCenterX = soilWidth * 0.50f
            val probeWidth = 20f
            val probeHeadTop = 3f
            val probeHeadHeight = soilTop + 4f
            val prongLength = soilHeight * 0.68f

            // Cuello / Placa base FR-4 de la sonda (Verde oscuro / Negro FR-4)
            drawRoundRect(
                color = Color(0xFF14532D),
                topLeft = Offset(probeCenterX - (probeWidth / 2f), probeHeadTop),
                size = Size(probeWidth, probeHeadHeight - probeHeadTop),
                cornerRadius = CornerRadius(2f, 2f)
            )

            // Conector header de 2 pines en la parte superior
            drawRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(probeCenterX - 4f, probeHeadTop),
                size = Size(8f, 4f)
            )

            // Pistas impresas y serigrafía FC-28
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(probeCenterX - 3f, probeHeadTop + 4f),
                end = Offset(probeCenterX - 3f, probeHeadHeight),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color(0xFFF59E0B),
                start = Offset(probeCenterX + 3f, probeHeadTop + 4f),
                end = Offset(probeCenterX + 3f, probeHeadHeight),
                strokeWidth = 1.5f
            )

            // Electrodos bifurcados (Horquilla dorada ENIG sumergida en el sustrato)
            val prongWidth = 3.2f
            val prongGap = 5.5f
            val prong1X = probeCenterX - (prongGap / 2f) - prongWidth
            val prong2X = probeCenterX + (prongGap / 2f)

            // Horquilla izquierda
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFFDE047)),
                    startY = probeHeadHeight,
                    endY = probeHeadHeight + prongLength
                ),
                topLeft = Offset(prong1X, probeHeadHeight),
                size = Size(prongWidth, prongLength),
                cornerRadius = CornerRadius(1.2f, 1.2f)
            )
            // Horquilla derecha
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFF59E0B), Color(0xFFFDE047)),
                    startY = probeHeadHeight,
                    endY = probeHeadHeight + prongLength
                ),
                topLeft = Offset(prong2X, probeHeadHeight),
                size = Size(prongWidth, prongLength),
                cornerRadius = CornerRadius(1.2f, 1.2f)
            )

            // Líneas de campo de detección resistivo/capacitivo entre electrodos
            if (esHumedo) {
                val fieldColor = Color(0x8038BDF8)
                val fieldSteps = 3
                for (s in 1..fieldSteps) {
                    val fieldY = probeHeadHeight + (s * (prongLength / (fieldSteps + 1)))
                    drawLine(
                        color = fieldColor,
                        start = Offset(prong1X + prongWidth + 1f, fieldY),
                        end = Offset(prong2X - 1f, fieldY),
                        strokeWidth = 1.2f,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Cables jumper puente que viajan desde la sonda hacia el módulo comparador
            val cableStartX = probeCenterX + 4f
            val cableStartY = probeHeadTop + 2f
            val moduleX = w * 0.65f
            val moduleY = h * 0.18f

            // Cable VCC (Rojo)
            val cableVccPath = Path().apply {
                moveTo(cableStartX, cableStartY)
                quadraticTo((cableStartX + moduleX) / 2f, cableStartY - 4f, moduleX, moduleY + 6f)
            }
            drawPath(path = cableVccPath, color = Color(0xFFEF4444), style = Stroke(width = 1.5f))

            // Cable GND (Negro / Azul)
            val cableGndPath = Path().apply {
                moveTo(cableStartX - 2f, cableStartY + 2f)
                quadraticTo((cableStartX + moduleX) / 2f, cableStartY + 8f, moduleX, moduleY + 12f)
            }
            drawPath(path = cableGndPath, color = Color(0xFF1E293B), style = Stroke(width = 1.5f))

            // 4. Módulo acondicionador de señal LM393 (PCB azul a la derecha)
            val moduleWidth = w * 0.30f
            val moduleHeight = h * 0.65f

            // Placa PCB azul clásica de módulos Arduino/ESP32
            drawRoundRect(
                color = Color(0xFF1D4ED8),
                topLeft = Offset(moduleX, moduleY),
                size = Size(moduleWidth, moduleHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Agujero de montaje mecánico M2
            drawCircle(color = Color(0xFF0F172A), radius = 2.5f, center = Offset(moduleX + moduleWidth - 5f, moduleY + 5f))

            // Chip IC LM393 (SOIC-8 en el centro de la placa)
            val icX = moduleX + 6f
            val icY = moduleY + 16f
            drawRoundRect(
                color = Color(0xFF0F172A),
                topLeft = Offset(icX, icY),
                size = Size(10f, 16f),
                cornerRadius = CornerRadius(1.5f, 1.5f)
            )
            // Punto indicador de pin 1 del integrado
            drawCircle(color = Color(0xFFE2E8F0), radius = 0.8f, center = Offset(icX + 2f, icY + 3f))

            // Trimpot potenciómetro de ajuste de umbral (Carcasa azul con tornillo de latón)
            val potX = moduleX + 18f
            val potY = moduleY + 16f
            drawRect(color = Color(0xFF2563EB), topLeft = Offset(potX, potY), size = Size(11f, 11f))
            drawCircle(color = Color(0xFFF59E0B), radius = 3.2f, center = Offset(potX + 5.5f, potY + 5.5f))
            drawLine(Color(0xFF78350F), start = Offset(potX + 3f, potY + 5.5f), end = Offset(potX + 8f, potY + 5.5f), strokeWidth = 0.8f)

            // LEDs indicadores de diagnóstico del módulo
            // LED 1: Alimentación PWR (Rojo encendido continuo)
            val ledPwrY = moduleY + moduleHeight - 8f
            drawCircle(color = Color(0xFFEF4444), radius = 2f, center = Offset(moduleX + 6f, ledPwrY))
            drawCircle(color = Color(0x50EF4444), radius = 3.5f, center = Offset(moduleX + 6f, ledPwrY))

            // LED 2: Salida Digital DO (Verde encendido cuando está en rango óptimo)
            val ledDoColor = if (estaEnRango) Color(0xFF22C55E) else Color(0xFF475569)
            drawCircle(color = ledDoColor, radius = 2f, center = Offset(moduleX + 16f, ledPwrY))
            if (estaEnRango) {
                drawCircle(color = Color(0x5022C55E), radius = 3.5f, center = Offset(moduleX + 16f, ledPwrY))
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Badge textual con porcentaje de hidratación y diagnóstico
        Column(horizontalAlignment = Alignment.End) {
            val porcentajeInt = (porcentajeHumedad * 100).toInt()
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (estaEnRango) Color(0xFFD8F3DC) else Color(0xFFFFF3E0)
            ) {
                Text(
                    text = "${porcentajeInt}%",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (estaEnRango) Color(0xFF1B4D28) else Color(0xFFE65100),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (estaEnRango) "Sustrato Óptimo" else if (esHumedo) "Saturación Hídrica" else "Sustrato Seco",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF52796F),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
