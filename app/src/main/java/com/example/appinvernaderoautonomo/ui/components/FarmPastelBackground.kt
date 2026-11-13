package com.example.appinvernaderoautonomo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fondo vectorial animado de temática rural bucólica en paleta pastel:
 * - Cielo celeste suave con sol radiante matutino.
 * - Nubes esponjosas flotando suavemente.
 * - Colinas onduladas en verde menta y salvia.
 * - Granja tradicional rústica con silo y molino de viento giratorio.
 * - Campo de girasoles con tallos y pétalos dorados meciéndose con la brisa del viento.
 */
@Composable
fun FarmPastelBackground(
    modifier: Modifier = Modifier,
    mostrarCapaFrosted: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "FarmPastelAnimations")

    // Animación de nubes a la deriva (capa lenta y capa media)
    val cloudProgress1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 32_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudDrift1"
    )

    val cloudProgress2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 48_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "CloudDrift2"
    )

    // Pulsación suave de la luz solar
    val sunPulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SunPulsing"
    )

    // Rotación continua de las aspas del molino
    val windmillAngle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 14_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WindmillRotation"
    )

    // Balanceo natural de los girasoles por el viento
    val sunflowerWindSway by transition.animateFloat(
        initialValue = -5.5f,
        targetValue = 5.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SunflowerWindSway"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // 1. CIELO CELESTE PASTEL CON GRADIENTE DE HORIZONTE
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFBAE6FD), // Celeste pastel cielo alto
                    Color(0xFFD0F0FD), // Celeste claro medio
                    Color(0xFFE0F2FE), // Celeste brisa
                    Color(0xFFFEF3C7)  // Horizonte cálido de amanecer
                ),
                startY = 0f,
                endY = h * 0.58f
            ),
            size = Size(w, h)
        )

        // 2. SOL PASTEL RADIANTE CON RESPLANDOR
        val sunCenter = Offset(w * 0.82f, h * 0.11f)
        val sunBaseRadius = w * 0.085f * sunPulse

        // Halo difuso
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color(0x80FDE047),
                    Color(0x30FEF08A),
                    Color.Transparent
                ),
                center = sunCenter,
                radius = sunBaseRadius * 2.3f
            ),
            radius = sunBaseRadius * 2.3f,
            center = sunCenter
        )

        // Disco solar central
        drawCircle(
            color = Color(0xFFFEF08A),
            radius = sunBaseRadius,
            center = sunCenter
        )
        drawCircle(
            color = Color(0xFFFDE047),
            radius = sunBaseRadius,
            center = sunCenter,
            style = Stroke(width = 2.5f)
        )

        // 3. NUBES FLOTANTES PASTEL
        drawCloud(
            x = ((cloudProgress1 * (w + 240f)) - 120f),
            y = h * 0.08f,
            scale = 0.95f
        )
        drawCloud(
            x = (((cloudProgress2 * (w + 280f)) + w * 0.45f) % (w + 280f) - 140f),
            y = h * 0.17f,
            scale = 0.75f
        )
        drawCloud(
            x = (((cloudProgress1 * 0.7f * (w + 260f)) + w * 0.78f) % (w + 260f) - 130f),
            y = h * 0.23f,
            scale = 0.85f
        )

        // 4. COLINAS DISTANTES (VERDE SALVIA Y MENTA PASTEL)
        val pathColinaLejana = Path().apply {
            moveTo(0f, h * 0.38f)
            cubicTo(
                w * 0.25f, h * 0.35f,
                w * 0.60f, h * 0.42f,
                w, h * 0.37f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = pathColinaLejana, color = Color(0xFFC7E9C0)) // Verde salvia distante

        // Fila de arbolitos distantes en la colina
        drawDistantTrees(w = w, h = h)

        // 5. COLINA MEDIA CON LA GRANJA Y EL MOLINO
        val pathColinaMedia = Path().apply {
            moveTo(0f, h * 0.45f)
            cubicTo(
                w * 0.30f, h * 0.42f,
                w * 0.68f, h * 0.48f,
                w, h * 0.44f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = pathColinaMedia, color = Color(0xFFA1D99B)) // Verde pradera pastel

        // DIBUJO DE LA GRANJA RÚSTICA (Barn + Silo)
        drawGranjaBarn(
            x = w * 0.16f,
            y = h * 0.41f,
            scale = w / 420f
        )

        // DIBUJO DEL MOLINO DE VIENTO (Windmill giratorio)
        drawMolinoViento(
            x = w * 0.82f,
            y = h * 0.435f,
            angle = windmillAngle,
            scale = w / 420f
        )

        // 6. COLINA PRINCIPAL DE PRIMER PLANO (PASTEL PRADERA VERDE)
        val pathColinaFrontal = Path().apply {
            moveTo(0f, h * 0.58f)
            cubicTo(
                w * 0.35f, h * 0.54f,
                w * 0.70f, h * 0.62f,
                w, h * 0.57f
            )
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(path = pathColinaFrontal, color = Color(0xFF74C69D)) // Verde pasto menta

        // 7. CAMPO DE GIRASOLES ANIMADOS MECEndOSE AL VIENTO
        drawCampoGirasoles(
            w = w,
            h = h,
            windSway = sunflowerWindSway
        )

        // 8. CAPA DE CRISTAL ESMERILADO PASTEL (FROSTED SCRIM)
        if (mostrarCapaFrosted) {
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xD0F0F9FF), // Tinte celeste pastel arriba (semitransparente)
                        Color(0xE8F7FBF8), // Tinte menta suave en contenido central
                        Color(0xEEF1F8F3)  // Tinte verde pradera suave al fondo
                    ),
                    startY = 0f,
                    endY = h
                ),
                size = Size(w, h)
            )
        }
    }
}

/**
 * Dibuja una nube esponjosa compuesta por múltiples arcos suaves pastel.
 */
private fun DrawScope.drawCloud(x: Float, y: Float, scale: Float) {
    val cloudColor = Color(0xE8FFFFFF)
    val shadowColor = Color(0x30BAE6FD)
    val r = 24f * scale

    // Sombra sutil de la nube
    drawCircle(shadowColor, radius = r * 1.05f, center = Offset(x, y + 4f))
    drawCircle(shadowColor, radius = r * 1.35f, center = Offset(x + 22f * scale, y - 6f))
    drawCircle(shadowColor, radius = r * 1.15f, center = Offset(x + 48f * scale, y + 2f))

    // Cuerpo blanco de la nube
    drawCircle(cloudColor, radius = r, center = Offset(x, y))
    drawCircle(cloudColor, radius = r * 1.3f, center = Offset(x + 22f * scale, y - 8f))
    drawCircle(cloudColor, radius = r * 1.1f, center = Offset(x + 48f * scale, y))
    drawCircle(cloudColor, radius = r * 0.85f, center = Offset(x + 66f * scale, y + 4f))
}

/**
 * Dibuja copas de arbolitos pequeños en la cresta de la colina distante.
 */
private fun DrawScope.drawDistantTrees(w: Float, h: Float) {
    val treeColor = Color(0xFF81C784)
    val treePositions = listOf(
        0.05f, 0.08f, 0.12f, 0.42f, 0.45f, 0.48f, 0.52f, 0.65f, 0.68f, 0.92f, 0.95f
    )
    for (pos in treePositions) {
        val tx = w * pos
        val ty = h * (0.36f + sin(pos * 6f) * 0.02f)
        drawCircle(
            color = treeColor,
            radius = 6.5f,
            center = Offset(tx, ty)
        )
    }
}

/**
 * Dibuja la granja tradicional (granero rústico en rojo pastel coral y silo cilíndrico).
 */
private fun DrawScope.drawGranjaBarn(x: Float, y: Float, scale: Float) {
    val s = scale.coerceIn(0.7f, 1.3f)
    val barnWidth = 52f * s
    val barnHeight = 36f * s

    // Silo al costado del granero
    val siloWidth = 16f * s
    val siloHeight = 44f * s
    val siloX = x - siloWidth - 4f
    val siloY = y - (siloHeight - barnHeight)

    // Cuerpo del silo (gris azulado pastel)
    drawRect(
        color = Color(0xFFCBD5E1),
        topLeft = Offset(siloX, siloY + 8f * s),
        size = Size(siloWidth, siloHeight - 8f * s)
    )
    // Cúpula del silo
    drawArc(
        color = Color(0xFF94A3B8),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(siloX, siloY),
        size = Size(siloWidth, 16f * s)
    )

    // Cuerpo principal del granero (terracota pastel / coral suave)
    drawRect(
        color = Color(0xFFFCA5A5),
        topLeft = Offset(x, y),
        size = Size(barnWidth, barnHeight)
    )

    // Techo a dos aguas del granero (crema pastel cálido)
    val pathTecho = Path().apply {
        moveTo(x - 5f * s, y)
        lineTo(x + barnWidth / 2f, y - 18f * s)
        lineTo(x + barnWidth + 5f * s, y)
        close()
    }
    drawPath(path = pathTecho, color = Color(0xFFFEE2E2))
    drawPath(path = pathTecho, color = Color(0xFFF87171), style = Stroke(width = 1.5f * s))

    // Puerta del granero con cruz tradicional blanca
    val doorW = 16f * s
    val doorH = 22f * s
    val doorX = x + (barnWidth - doorW) / 2f
    val doorY = y + barnHeight - doorH

    drawRect(
        color = Color(0xFFEF4444),
        topLeft = Offset(doorX, doorY),
        size = Size(doorW, doorH)
    )
    drawLine(
        color = Color.White,
        start = Offset(doorX, doorY),
        end = Offset(doorX + doorW, doorY + doorH),
        strokeWidth = 1.8f * s
    )
    drawLine(
        color = Color.White,
        start = Offset(doorX + doorW, doorY),
        end = Offset(doorX, doorY + doorH),
        strokeWidth = 1.8f * s
    )
}

/**
 * Dibuja un molino de viento de campo con aspas giratorias.
 */
private fun DrawScope.drawMolinoViento(x: Float, y: Float, angle: Float, scale: Float) {
    val s = scale.coerceIn(0.7f, 1.3f)
    val towerW = 18f * s
    val towerH = 46f * s

    // Torre trapezoidal del molino (madera clara pastel)
    val pathTorre = Path().apply {
        moveTo(x - towerW * 0.35f, y - towerH)
        lineTo(x + towerW * 0.35f, y - towerH)
        lineTo(x + towerW * 0.5f, y)
        lineTo(x - towerW * 0.5f, y)
        close()
    }
    drawPath(path = pathTorre, color = Color(0xFFF1F5F9))
    drawPath(path = pathTorre, color = Color(0xFF94A3B8), style = Stroke(width = 1.2f * s))

    // Cúpula superior
    val hubCenter = Offset(x, y - towerH)
    drawCircle(
        color = Color(0xFF64748B),
        radius = 5.5f * s,
        center = hubCenter
    )

    // 4 Aspas del molino rotando continuamente
    rotate(degrees = angle, pivot = hubCenter) {
        val bladeLength = 32f * s
        for (i in 0 until 4) {
            rotate(degrees = i * 90f, pivot = hubCenter) {
                // Brazo principal del aspa
                drawLine(
                    color = Color(0xFF475569),
                    start = hubCenter,
                    end = Offset(hubCenter.x, hubCenter.y - bladeLength),
                    strokeWidth = 1.8f * s
                )
                // Celosía del aspa (tela vela pastel)
                drawRect(
                    color = Color(0xCCFEF08A),
                    topLeft = Offset(hubCenter.x + 1.5f * s, hubCenter.y - bladeLength),
                    size = Size(7.5f * s, bladeLength * 0.75f)
                )
            }
        }
    }
}

/**
 * Dibuja un campo abundante de girasoles con tallos curvos, hojas y pétalos dorados
 * meciéndose armónicamente con la brisa del viento.
 */
private fun DrawScope.drawCampoGirasoles(w: Float, h: Float, windSway: Float) {
    data class SunflowerConfig(
        val relX: Float,
        val relY: Float,
        val scale: Float,
        val phase: Float
    )

    // Distribución armónica de girasoles a lo largo del campo frontal
    val girasoles = listOf(
        // Hilera posterior (más pequeños, mayor densidad)
        SunflowerConfig(0.04f, 0.65f, 0.55f, 0.2f),
        SunflowerConfig(0.12f, 0.67f, 0.60f, 0.8f),
        SunflowerConfig(0.22f, 0.64f, 0.52f, 1.4f),
        SunflowerConfig(0.31f, 0.68f, 0.65f, 2.1f),
        SunflowerConfig(0.42f, 0.65f, 0.58f, 2.7f),
        SunflowerConfig(0.53f, 0.67f, 0.62f, 0.5f),
        SunflowerConfig(0.64f, 0.64f, 0.55f, 1.9f),
        SunflowerConfig(0.74f, 0.68f, 0.68f, 3.2f),
        SunflowerConfig(0.85f, 0.66f, 0.56f, 0.9f),
        SunflowerConfig(0.95f, 0.65f, 0.60f, 2.4f),

        // Hilera media (tamaño estándar)
        SunflowerConfig(0.08f, 0.76f, 0.82f, 1.1f),
        SunflowerConfig(0.19f, 0.79f, 0.88f, 2.6f),
        SunflowerConfig(0.28f, 0.75f, 0.78f, 0.3f),
        SunflowerConfig(0.39f, 0.80f, 0.92f, 1.8f),
        SunflowerConfig(0.49f, 0.76f, 0.85f, 3.0f),
        SunflowerConfig(0.60f, 0.81f, 0.90f, 0.7f),
        SunflowerConfig(0.70f, 0.77f, 0.84f, 2.2f),
        SunflowerConfig(0.81f, 0.82f, 0.95f, 1.5f),
        SunflowerConfig(0.91f, 0.78f, 0.80f, 3.4f),

        // Hilera frontal destacada (girasoles grandes y expresivos)
        SunflowerConfig(0.03f, 0.89f, 1.15f, 0.6f),
        SunflowerConfig(0.15f, 0.92f, 1.25f, 2.0f),
        SunflowerConfig(0.26f, 0.90f, 1.10f, 1.3f),
        SunflowerConfig(0.37f, 0.94f, 1.30f, 3.1f),
        SunflowerConfig(0.52f, 0.91f, 1.20f, 0.4f),
        SunflowerConfig(0.66f, 0.93f, 1.28f, 2.5f),
        SunflowerConfig(0.79f, 0.90f, 1.15f, 1.7f),
        SunflowerConfig(0.94f, 0.93f, 1.35f, 3.3f)
    )

    for (item in girasoles) {
        val posX = w * item.relX
        val baseY = h * item.relY
        val scale = item.scale
        val stemHeight = 55f * scale

        // Desplazamiento orgánico por viento
        val currentSway = sin(windSway * 0.1f + item.phase) * (8f * scale)
        val headX = posX + currentSway
        val headY = baseY - stemHeight

        // 1. Tallo curvado verde pastel
        val pathTallo = Path().apply {
            moveTo(posX, baseY)
            quadraticTo(
                posX + (currentSway * 0.4f),
                baseY - (stemHeight * 0.5f),
                headX,
                headY
            )
        }
        drawPath(
            path = pathTallo,
            color = Color(0xFF2D6A4F),
            style = Stroke(width = 3.2f * scale)
        )

        // 2. Hojas laterales verde salvia
        val leafY = baseY - stemHeight * 0.45f
        val leafX = posX + (currentSway * 0.35f)
        val pathHoja = Path().apply {
            moveTo(leafX, leafY)
            quadraticTo(leafX - 16f * scale, leafY - 10f * scale, leafX - 22f * scale, leafY - 4f * scale)
            quadraticTo(leafX - 12f * scale, leafY + 4f * scale, leafX, leafY)
            close()
        }
        drawPath(path = pathHoja, color = Color(0xFF52B788))

        // 3. Cabeza del Girasol (Corona de pétalos amarillos y disco marrón)
        val headCenter = Offset(headX, headY)
        val centerRadius = 8.5f * scale
        val petalLength = 16f * scale
        val petalWidth = 6.5f * scale
        val petalCount = 14

        // Corona de pétalos dorados en abanico circular
        for (p in 0 until petalCount) {
            val petalAngle = (p * (360f / petalCount)) + (currentSway * 2.5f)
            val rad = petalAngle * (PI / 180f).toFloat()
            val petalTipX = headX + cos(rad) * (centerRadius + petalLength)
            val petalTipY = headY + sin(rad) * (centerRadius + petalLength)

            val petalBaseLeftX = headX + cos(rad - 0.22f) * centerRadius
            val petalBaseLeftY = headY + sin(rad - 0.22f) * centerRadius
            val petalBaseRightX = headX + cos(rad + 0.22f) * centerRadius
            val petalBaseRightY = headY + sin(rad + 0.22f) * centerRadius

            val pathPetalo = Path().apply {
                moveTo(petalBaseLeftX, petalBaseLeftY)
                quadraticTo(
                    headX + cos(rad) * (centerRadius + petalLength * 0.55f) - sin(rad) * (petalWidth * 0.6f),
                    headY + sin(rad) * (centerRadius + petalLength * 0.55f) + cos(rad) * (petalWidth * 0.6f),
                    petalTipX,
                    petalTipY
                )
                quadraticTo(
                    headX + cos(rad) * (centerRadius + petalLength * 0.55f) + sin(rad) * (petalWidth * 0.6f),
                    headY + sin(rad) * (centerRadius + petalLength * 0.55f) - cos(rad) * (petalWidth * 0.6f),
                    petalBaseRightX,
                    petalBaseRightY
                )
                close()
            }

            // Alternancia sutil de tonos amarillos pastel para profundidad
            val colorPetalo = if (p % 2 == 0) Color(0xFFFDE047) else Color(0xFFFEF08A)
            drawPath(path = pathPetalo, color = colorPetalo)
        }

        // Disco central de semillas (marrón avellana pastel y texturado)
        drawCircle(
            color = Color(0xFF78350F), // Marrón tierra profundo
            radius = centerRadius,
            center = headCenter
        )
        drawCircle(
            color = Color(0xFF92400E), // Borde marrón tostado
            radius = centerRadius * 0.75f,
            center = headCenter
        )
        drawCircle(
            color = Color(0xFFB45309), // Centro avellana
            radius = centerRadius * 0.40f,
            center = headCenter
        )
    }
}
