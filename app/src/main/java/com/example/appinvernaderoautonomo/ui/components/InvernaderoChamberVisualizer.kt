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
import kotlin.math.cos
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
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Color(0xFF4CAF50), shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Cámara de Policarbonato",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B4D28),
                        letterSpacing = 0.3.sp,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Text(
                        text = "VENTILACIÓN CRUZADA",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        softWrap = false,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Canvas de la caja rectangular de policarbonato con extremos funcionales
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Color(0xFFF7FBF8), shape = RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFDCE9E0), RoundedCornerShape(12.dp))
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // Geometría segura y proporcional de la caja para evitar cortes en bordes
                    val boxMarginLeft = 56.dp.toPx()
                    val boxMarginRight = 48.dp.toPx()
                    val boxMarginTop = h * 0.16f
                    val boxMarginBottom = h * 0.88f
                    val boxWidth = w - boxMarginLeft - boxMarginRight
                    val boxHeight = boxMarginBottom - boxMarginTop
                    val cornerRadius = 8.dp.toPx()

                    val boxTopLeft = Offset(boxMarginLeft, boxMarginTop)
                    val boxSize = Size(boxWidth, boxHeight)

                    // 1. Fondo interior transparente translúcido del policarbonato
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x28BAE6FD),
                                Color(0x35E0F2FE),
                                Color(0x40D8F3DC)
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
                        val ribX = boxMarginLeft + (i * ribSpacing)
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
                        moveTo(boxMarginLeft + 2f, soilTop)
                        cubicTo(
                            boxMarginLeft + boxWidth * 0.35f, soilTop - 3f,
                            boxMarginLeft + boxWidth * 0.65f, soilTop + 2f,
                            boxMarginLeft + boxWidth - 2f, soilTop
                        )
                        lineTo(boxMarginLeft + boxWidth - 2f, boxMarginBottom - 2f)
                        lineTo(boxMarginLeft + 2f, boxMarginBottom - 2f)
                        close()
                    }
                    val esSueloHumedo = humedadSueloRaw <= 850
                    val soilGradient = if (esSueloHumedo) {
                        listOf(Color(0xFF3E2723), Color(0xFF271A14))
                    } else {
                        listOf(Color(0xFF795548), Color(0xFF5D4037))
                    }
                    drawPath(
                        path = soilPath,
                        brush = Brush.verticalGradient(
                            colors = soilGradient,
                            startY = soilTop,
                            endY = boxMarginBottom
                        )
                    )

                    // Textura de perlita, mantillo y partículas orgánicas en el suelo
                    val soilParticleCount = 8
                    for (sp in 0 until soilParticleCount) {
                        val pX = boxMarginLeft + 14f + (sp * (boxWidth - 28f) / soilParticleCount)
                        val pY = soilTop + 8f + ((sp % 3) * 6f)
                        drawCircle(Color(0x70FEF9C3), radius = 1.3f, center = Offset(pX, pY))
                        if (esSueloHumedo && sp % 2 == 0) {
                            drawCircle(Color(0x6038BDF8), radius = 1.6f, center = Offset(pX + 3f, pY - 2f))
                        }
                    }

                    // 3. Sensor FC-28 incrustado en el suelo con electrodos de oro ENIG y módulo LM393
                    val fc28X = boxMarginLeft + boxWidth * 0.26f
                    val fc28Top = soilTop - 18f
                    val fc28ProngLen = 18f

                    // Halo sutil de detección de humedad en el suelo
                    val haloColorSuelo = if (esSueloHumedo) Color(0x3538BDF8) else Color(0x20F59E0B)
                    drawCircle(haloColorSuelo, radius = 16f, center = Offset(fc28X, soilTop + 12f))

                    // Placa base FR-4 del sensor (Verde oscuro)
                    drawRoundRect(
                        color = Color(0xFF14532D),
                        topLeft = Offset(fc28X - 6f, fc28Top),
                        size = Size(12f, 15f),
                        cornerRadius = CornerRadius(1.5f, 1.5f)
                    )
                    // Conector header negro
                    drawRect(color = Color(0xFF0F172A), topLeft = Offset(fc28X - 3f, fc28Top), size = Size(6f, 3f))

                    // Pistas de cobre doradas en PCB
                    drawLine(Color(0xFFF59E0B), start = Offset(fc28X - 2.5f, fc28Top + 3f), end = Offset(fc28X - 2.5f, fc28Top + 14f), strokeWidth = 1.2f)
                    drawLine(Color(0xFFF59E0B), start = Offset(fc28X + 2.5f, fc28Top + 3f), end = Offset(fc28X + 2.5f, fc28Top + 14f), strokeWidth = 1.2f)

                    // Horquillas metálicas (electrodos bifurcados bañados en oro ENIG)
                    val prongW = 2.4f
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFFDE047)), startY = fc28Top + 15f, endY = fc28Top + 15f + fc28ProngLen),
                        topLeft = Offset(fc28X - 3.8f, fc28Top + 15f),
                        size = Size(prongW, fc28ProngLen),
                        cornerRadius = CornerRadius(1f, 1f)
                    )
                    drawRoundRect(
                        brush = Brush.verticalGradient(listOf(Color(0xFFF59E0B), Color(0xFFFDE047)), startY = fc28Top + 15f, endY = fc28Top + 15f + fc28ProngLen),
                        topLeft = Offset(fc28X + 1.4f, fc28Top + 15f),
                        size = Size(prongW, fc28ProngLen),
                        cornerRadius = CornerRadius(1f, 1f)
                    )

                    // Campo de detección entre horquillas
                    if (esSueloHumedo) {
                        drawLine(Color(0x8038BDF8), start = Offset(fc28X - 1.4f, fc28Top + 20f), end = Offset(fc28X + 1.4f, fc28Top + 20f), strokeWidth = 1f)
                        drawLine(Color(0x8038BDF8), start = Offset(fc28X - 1.4f, fc28Top + 25f), end = Offset(fc28X + 1.4f, fc28Top + 25f), strokeWidth = 1f)
                    }

                    // Mini módulo comparador LM393 montado al lado
                    val modX = fc28X + 10f
                    val modY = fc28Top + 2f
                    drawRoundRect(color = Color(0xFF1D4ED8), topLeft = Offset(modX, modY), size = Size(11f, 14f), cornerRadius = CornerRadius(1.5f, 1.5f))
                    // Chip LM393 y Trimpot
                    drawRect(color = Color(0xFF0F172A), topLeft = Offset(modX + 2f, modY + 3f), size = Size(4f, 5f))
                    drawRect(color = Color(0xFF2563EB), topLeft = Offset(modX + 6.5f, modY + 3f), size = Size(3.5f, 3.5f))
                    drawCircle(Color(0xFFF59E0B), radius = 1.2f, center = Offset(modX + 8.2f, modY + 4.7f))
                    // LEDs de diagnóstico (PWR rojo y DO verde)
                    drawCircle(color = Color(0xFFEF4444), radius = 1.2f, center = Offset(modX + 3f, modY + 11f))
                    val doColor = if (esSueloHumedo) Color(0xFF22C55E) else Color(0xFF475569)
                    drawCircle(color = doColor, radius = 1.2f, center = Offset(modX + 8f, modY + 11f))

                    // 4. Plantas interiores floreciendo en el suelo
                    val plantPositions = listOf(0.48f, 0.70f)
                    for ((idx, posRel) in plantPositions.withIndex() ) {
                        val plantBaseX = boxMarginLeft + (boxWidth * posRel)
                        val plantBaseY = soilTop
                        val swayFactor = if (estaVentiladorGirando || anguloAnimado > 15f) plantSway else (plantSway * 0.25f)
                        val currentPlantSway = (swayFactor * (if (idx == 0) 1f else -0.8f))

                        val stemHeight = boxHeight * 0.42f
                        val stemTopX = plantBaseX + currentPlantSway
                        val stemTopY = plantBaseY - stemHeight

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

                        // Hojas con nervadura y rocío
                        val leafY1 = plantBaseY - stemHeight * 0.40f
                        drawCircle(Color(0xFF52B788), radius = 6.2f, center = Offset(plantBaseX - 7f, leafY1))
                        drawLine(Color(0xFF2D6A4F), start = Offset(plantBaseX - 3f, leafY1), end = Offset(plantBaseX - 10f, leafY1), strokeWidth = 1f)
                        drawCircle(Color(0x90FFFFFF), radius = 1.2f, center = Offset(plantBaseX - 6f, leafY1 - 2f)) // Rocío

                        val leafY2 = plantBaseY - stemHeight * 0.65f
                        drawCircle(Color(0xFF40916C), radius = 6.2f, center = Offset(plantBaseX + 7f, leafY2))
                        drawLine(Color(0xFF1B4D28), start = Offset(plantBaseX + 3f, leafY2), end = Offset(plantBaseX + 10f, leafY2), strokeWidth = 1f)

                        drawCircle(Color(0xFF74C69D), radius = 5.2f, center = Offset(stemTopX, stemTopY))
                    }

                    // 5. Sensor DHT22 suspendido en el aire interior (Carcasa blanca ventilada)
                    val dhtX = boxMarginLeft + boxWidth * 0.50f
                    val dhtY = boxMarginTop + 14f
                    // Cable de suspensión con pasacables
                    drawLine(
                        color = Color(0xFF334155),
                        start = Offset(dhtX, boxMarginTop),
                        end = Offset(dhtX, dhtY),
                        strokeWidth = 1.8f
                    )
                    drawCircle(color = Color(0xFF475569), radius = 2.2f, center = Offset(dhtX, dhtY))

                    // Cuerpo del DHT22
                    drawRoundRect(
                        color = Color(0xFFF8FAFC),
                        topLeft = Offset(dhtX - 7f, dhtY + 2f),
                        size = Size(14f, 18f),
                        cornerRadius = CornerRadius(2.5f, 2.5f)
                    )
                    drawRoundRect(
                        color = Color(0xFF94A3B8),
                        topLeft = Offset(dhtX - 7f, dhtY + 2f),
                        size = Size(14f, 18f),
                        cornerRadius = CornerRadius(2.5f, 2.5f),
                        style = Stroke(width = 1f)
                    )
                    // Rejillas de toma de aire del DHT22 (Ranuras horizontales)
                    val dhtGridY = dhtY + 5f
                    for (g in 0..3) {
                        drawLine(Color(0xFFCBD5E1), start = Offset(dhtX - 4.5f, dhtGridY + (g * 3f)), end = Offset(dhtX + 4.5f, dhtGridY + (g * 3f)), strokeWidth = 1.2f)
                    }

                    // 6. EXTREMO IZQUIERDO: Compuerta batiente de policarbonato alveolar (Servo SG90)
                    val hatchHingeX = boxMarginLeft
                    val hatchHingeY = boxMarginTop + (boxHeight * 0.18f)
                    val hatchLength = 36.dp.toPx()
                    val panelThick = 6.5f

                    // Vano de ventilación en la pared izquierda
                    drawLine(
                        color = Color(0x5038BDF8),
                        start = Offset(boxMarginLeft, hatchHingeY),
                        end = Offset(boxMarginLeft, hatchHingeY + hatchLength),
                        strokeWidth = 2.5f,
                        cap = StrokeCap.Round
                    )

                    // Servomotor SG90 fijado al marco interior
                    val sg90X = boxMarginLeft + 6f
                    val sg90Y = hatchHingeY + 12f
                    drawRoundRect(
                        color = Color(0xFF0284C7),
                        topLeft = Offset(sg90X, sg90Y),
                        size = Size(10f, 15f),
                        cornerRadius = CornerRadius(1.5f, 1.5f)
                    )
                    drawCircle(Color(0xFFF59E0B), radius = 2.2f, center = Offset(sg90X + 3.5f, sg90Y + 3f))

                    // Brazo oscilante de nylon del servo
                    val sg90ArmAngle = -(anguloAnimado * 0.85f)
                    val sg90ArmLen = 8f
                    val sg90ArmEndX = (sg90X + 3.5f) - sg90ArmLen * sin(sg90ArmAngle * PI.toFloat() / 180f)
                    val sg90ArmEndY = (sg90Y + 3f) + sg90ArmLen * cos(sg90ArmAngle * PI.toFloat() / 180f)
                    drawLine(Color(0xFFF8FAFC), start = Offset(sg90X + 3.5f, sg90Y + 3f), end = Offset(sg90ArmEndX, sg90ArmEndY), strokeWidth = 2.2f, cap = StrokeCap.Round)

                    // Panel alveolar abriéndose hacia afuera con flautas internas y varilla de empuje
                    rotate(degrees = -anguloAnimado, pivot = Offset(hatchHingeX, hatchHingeY)) {
                        val panelEndY = hatchHingeY + hatchLength

                        // Vidrio alveolar celular
                        val pathHatch = Path().apply {
                            moveTo(hatchHingeX - panelThick, hatchHingeY)
                            lineTo(hatchHingeX, hatchHingeY)
                            lineTo(hatchHingeX, panelEndY)
                            lineTo(hatchHingeX - panelThick, panelEndY)
                            close()
                        }
                        drawPath(path = pathHatch, color = Color(0x70BAE6FD))

                        // Canales / flautas celulares
                        val numFlutes = 4
                        for (fl in 1..numFlutes) {
                            val fluteY = hatchHingeY + (fl * hatchLength / (numFlutes + 1))
                            drawLine(Color(0x600284C7), start = Offset(hatchHingeX - panelThick + 1f, fluteY), end = Offset(hatchHingeX - 1f, fluteY), strokeWidth = 1f)
                        }

                        // Marco perimetral verde menta pastel
                        drawPath(path = pathHatch, color = Color(0xFF2E7D32), style = Stroke(width = 1.5f))

                        // Punto de anclaje de rótula y varilla de acero
                        val anclajeY = hatchHingeY + hatchLength * 0.45f
                        drawCircle(Color(0xFF334155), radius = 1.8f, center = Offset(hatchHingeX, anclajeY))
                        drawLine(Color(0xFF64748B), start = Offset(sg90ArmEndX, sg90ArmEndY), end = Offset(hatchHingeX, anclajeY), strokeWidth = 1.5f, cap = StrokeCap.Round)
                    }
                    // Eje y perno de la bisagra principal
                    drawCircle(Color(0xFF1E293B), radius = 4f, center = Offset(hatchHingeX, hatchHingeY))
                    drawCircle(Color(0xFFE2E8F0), radius = 1.8f, center = Offset(hatchHingeX, hatchHingeY))

                    // 7. EXTREMO DERECHO OPUESTO: Ventilador Extractor 12V con MOSFET (Chasis cuadrado, 7 aspas y tobera)
                    val fanCenterX = boxMarginLeft + boxWidth
                    val fanCenterY = boxMarginTop + (boxHeight * 0.40f)
                    val fanRadius = 18.dp.toPx()
                    val fanFrameSize = fanRadius * 2.1f

                    // Chasis cuadrado industrial moldeado
                    drawRoundRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(fanCenterX - (fanFrameSize / 2f), fanCenterY - (fanFrameSize / 2f)),
                        size = Size(fanFrameSize, fanFrameSize),
                        cornerRadius = CornerRadius(4f, 4f)
                    )

                    // 4 tornillos esquineros
                    val fScOffset = fanFrameSize * 0.40f
                    val fanScrews = listOf(
                        Offset(fanCenterX - fScOffset, fanCenterY - fScOffset),
                        Offset(fanCenterX + fScOffset, fanCenterY - fScOffset),
                        Offset(fanCenterX - fScOffset, fanCenterY + fScOffset),
                        Offset(fanCenterX + fScOffset, fanCenterY + fScOffset)
                    )
                    for (fsp in fanScrews) {
                        drawCircle(Color(0xFF0F172A), radius = 1.8f, center = fsp)
                        drawCircle(Color(0xFF94A3B8), radius = 1.2f, center = fsp)
                    }

                    // LED de estado del ventilador
                    val fLedColor = if (estaVentiladorGirando) Color(0xFF10B981) else Color(0xFF64748B)
                    drawCircle(color = fLedColor, radius = 1.5f, center = Offset(fanCenterX + fScOffset, fanCenterY - fScOffset + 6f))

                    // Tobera cilíndrica interna
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFF1E293B), Color(0xFF0F172A)), center = Offset(fanCenterX, fanCenterY), radius = fanRadius),
                        radius = fanRadius,
                        center = Offset(fanCenterX, fanCenterY)
                    )
                    drawCircle(
                        color = Color(0xFF475569),
                        radius = fanRadius,
                        center = Offset(fanCenterX, fanCenterY),
                        style = Stroke(width = 1.5f)
                    )

                    // Turbina de 7 aspas aerodinámicas alabeadas
                    val currentFanRot = if (estaVentiladorGirando) fanRotation else 22f
                    rotate(degrees = currentFanRot, pivot = Offset(fanCenterX, fanCenterY)) {
                        val bladeCount = 7
                        val bladeLen = fanRadius * 0.78f
                        val bladeColor = if (estaVentiladorGirando) Color(0xFF0284C7) else Color(0xFF64748B)
                        val bladeHl = if (estaVentiladorGirando) Color(0xFF7DD3FC) else Color(0xFF94A3B8)
                        for (b in 0 until bladeCount) {
                            rotate(degrees = b * (360f / bladeCount), pivot = Offset(fanCenterX, fanCenterY)) {
                                val bPath = Path().apply {
                                    moveTo(fanCenterX - 1.5f, fanCenterY - fanRadius * 0.28f)
                                    quadraticTo(fanCenterX + bladeLen * 0.45f, fanCenterY - bladeLen * 0.65f, fanCenterX + 1.2f, fanCenterY - bladeLen)
                                    quadraticTo(fanCenterX - bladeLen * 0.15f, fanCenterY - bladeLen * 0.45f, fanCenterX + 1.5f, fanCenterY - fanRadius * 0.28f)
                                    close()
                                }
                                drawPath(path = bPath, color = bladeColor)
                                drawPath(path = bPath, color = bladeHl, style = Stroke(width = 0.8f))
                            }
                        }
                    }

                    // Rejilla de protección concéntrica
                    drawCircle(Color(0x30CBD5E1), radius = fanRadius * 0.65f, center = Offset(fanCenterX, fanCenterY), style = Stroke(width = 0.8f))
                    // Buje central con tapa metálica y bobinas
                    drawCircle(Color(0xFF0F172A), radius = fanRadius * 0.30f, center = Offset(fanCenterX, fanCenterY))
                    drawCircle(Color(0xFFD97706), radius = fanRadius * 0.22f, center = Offset(fanCenterX, fanCenterY), style = Stroke(width = 1f))
                    drawCircle(
                        brush = Brush.radialGradient(listOf(Color(0xFFE2E8F0), Color(0xFF64748B)), center = Offset(fanCenterX - 1f, fanCenterY - 1f), radius = fanRadius * 0.15f),
                        radius = fanRadius * 0.15f,
                        center = Offset(fanCenterX, fanCenterY)
                    )

                    // Pluma de eyección de aire al exterior cuando el ventilador gira
                    if (estaVentiladorGirando) {
                        val plumeAlpha = (velocidadFanPwm / 255f).coerceIn(0.2f, 0.9f)
                        val plumeParticles = 3
                        for (pp in 0 until plumeParticles) {
                            val pProg = (airflowPhase + (pp * 0.33f)) % 1f
                            val pX = fanCenterX + fanRadius + (pProg * 22f)
                            val pY = fanCenterY - 6f + (pp * 6f) + sin((airflowPhase + pp) * PI.toFloat() * 2f) * 3f
                            drawCircle(Color(0x7038BDF8).copy(alpha = (1f - pProg) * plumeAlpha), radius = 1.8f + (pProg * 1.5f), center = Offset(pX, pY))
                        }
                    }

                    // 8. FLUJO DE AIRE CRUZADO (Entra por compuerta izq -> barre plantas -> sale por extractor der)
                    val hayFlujoActivo = anguloAnimado > 10f || estaVentiladorGirando
                    if (hayFlujoActivo) {
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), airflowPhase * 18f)
                        val flujoColor = Color(0x9538BDF8)

                        val yOffsets = listOf(0.32f, 0.45f, 0.58f)
                        for (yRel in yOffsets) {
                            val streamY = boxMarginTop + (boxHeight * yRel)
                            val pathAir = Path().apply {
                                moveTo(boxMarginLeft - 22f, streamY - 3f)
                                cubicTo(
                                    boxMarginLeft + boxWidth * 0.30f, streamY - 6f,
                                    boxMarginLeft + boxWidth * 0.70f, streamY + 5f,
                                    boxMarginLeft + boxWidth + 24f, streamY
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
                        style = Stroke(width = 3.2f)
                    )
                    drawLine(
                        color = Color(0x99FFFFFF),
                        start = Offset(boxMarginLeft + 12f, boxMarginTop + 2.5f),
                        end = Offset(boxMarginLeft + boxWidth - 12f, boxMarginTop + 2.5f),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )

                    // Reflejos diagonales especulares de la lámina frontal de policarbonato
                    drawLine(
                        color = Color(0x22FFFFFF),
                        start = Offset(boxMarginLeft + boxWidth * 0.15f, boxMarginTop + 6f),
                        end = Offset(boxMarginLeft + boxWidth * 0.35f, boxMarginBottom - 6f),
                        strokeWidth = 5f,
                        cap = StrokeCap.Round
                    )
                    drawLine(
                        color = Color(0x15FFFFFF),
                        start = Offset(boxMarginLeft + boxWidth * 0.42f, boxMarginTop + 6f),
                        end = Offset(boxMarginLeft + boxWidth * 0.62f, boxMarginBottom - 6f),
                        strokeWidth = 3f,
                        cap = StrokeCap.Round
                    )

                    // Pernos de montaje en las 4 esquinas del marco de aluminio
                    val rivetRadius = 2.2f
                    val rivetColor = Color(0xFF94A3B8)
                    drawCircle(rivetColor, radius = rivetRadius, center = Offset(boxMarginLeft + 5f, boxMarginTop + 5f))
                    drawCircle(rivetColor, radius = rivetRadius, center = Offset(boxMarginLeft + boxWidth - 5f, boxMarginTop + 5f))
                    drawCircle(rivetColor, radius = rivetRadius, center = Offset(boxMarginLeft + 5f, boxMarginBottom - 5f))
                    drawCircle(rivetColor, radius = rivetRadius, center = Offset(boxMarginLeft + boxWidth - 5f, boxMarginBottom - 5f))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Leyendas organizadas en 3 tarjetas tipo chip equilibradas y sin desbordes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F8F4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4E8DC))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EXT. IZQ",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            maxLines = 1
                        )
                        Text(
                            text = "Compuerta: $anguloCompuerta°",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B4D28),
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1.2f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF1F8F4),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4E8DC))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "INTERIOR",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            maxLines = 1
                        )
                        Text(
                            text = "Cultivo • FC-28 • DHT22",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1B4D28),
                            maxLines = 1
                        )
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFF0F9FF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBAE6FD))
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "EXT. DER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B),
                            maxLines = 1
                        )
                        val fanPct = ((velocidadFanPwm / 255f) * 100).toInt()
                        Text(
                            text = "Extractor: $fanPct%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}
