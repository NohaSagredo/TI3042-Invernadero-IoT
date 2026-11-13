package com.example.appinvernaderoautonomo.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Fondo vectorial animado de temática rural bucólica en paleta pastel:
 * - Cielo dinámico con sol radiante, rayos matutinos y aves sobrevolando.
 * - Nubes esponjosas flotando con sombras y volumen.
 * - Colinas onduladas en verde menta y salvia con arboledas distantes.
 * - Granja tradicional rústica con valla de madera, silo con veleta y molino de viento giratorio.
 * - Campo de girasoles con doble corona de pétalos dorados y semillas texturadas meciéndose al viento.
 * - Capa frosted esmerilada luminosa para contrastar perfectamente las tarjetas sin opacar el arte SVG.
 */
@Composable
fun FarmPastelBackground(
    modifier: Modifier = Modifier,
    mostrarCapaFrosted: Boolean = true
) {
    val horaActual = androidx.compose.runtime.remember {
        java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    }
    val esAtardecer = horaActual in 18..20
    val esNoche = horaActual >= 21 || horaActual < 7

    val transition = rememberInfiniteTransition(label = "FarmPastelAnimations")

    // Animación de nubes a la deriva (capa lenta y capa media)
    val cloudProgress1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 34_000, easing = LinearEasing),
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

    // Pulsación suave de la luz solar o lunar
    val sunPulse by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = FastOutSlowInEasing),
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

        // 1. CIELO DINÁMICO ADAPTATIVO A LA HORA DEL DÍA
        val coloresCielo = when {
            esNoche -> listOf(
                Color(0xFF1E293B),
                Color(0xFF334155),
                Color(0xFF475569),
                Color(0xFF1B3D2B)
            )
            esAtardecer -> listOf(
                Color(0xFFF472B6),
                Color(0xFFFB7185),
                Color(0xFFFED7AA),
                Color(0xFFFEF08A)
            )
            else -> listOf(
                Color(0xFFBAE6FD),
                Color(0xFFD0F0FD),
                Color(0xFFE0F2FE),
                Color(0xFFFEF3C7)
            )
        }

        drawRect(
            brush = Brush.verticalGradient(
                colors = coloresCielo,
                startY = 0f,
                endY = h * 0.58f
            ),
            size = Size(w, h)
        )

        // 2. ASTRO CELESTE (SOL DE DÍA/ATARDECER O LUNA CRECIENTE DE NOCHE)
        val astroCenter = Offset(w * 0.82f, h * (if (esAtardecer) 0.17f else 0.11f))
        val astroBaseRadius = w * 0.082f * sunPulse

        if (esNoche) {
            // Luna creciente apacible pastel
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x50FEF9C3), Color.Transparent),
                    center = astroCenter,
                    radius = astroBaseRadius * 2f
                ),
                radius = astroBaseRadius * 2f,
                center = astroCenter
            )
            drawCircle(
                color = Color(0xFFFEF9C3),
                radius = astroBaseRadius * 0.9f,
                center = astroCenter
            )
            drawCircle(
                color = Color(0xFF1E293B),
                radius = astroBaseRadius * 0.78f,
                center = Offset(astroCenter.x - (astroBaseRadius * 0.35f), astroCenter.y - (astroBaseRadius * 0.15f))
            )
        } else {
            // Haces de luz solar radiante
            val rayCount = 8
            val rayColor = if (esAtardecer) Color(0x22FDBA74) else Color(0x28FFFBEB)
            for (i in 0 until rayCount) {
                val rayAngle = (i * (360f / rayCount)) + (cloudProgress1 * 360f * 0.08f)
                val rad = rayAngle * (PI / 180f).toFloat()
                val rayEnd = Offset(
                    astroCenter.x + cos(rad) * (astroBaseRadius * 4.2f),
                    astroCenter.y + sin(rad) * (astroBaseRadius * 4.2f)
                )
                drawLine(
                    color = rayColor,
                    start = astroCenter,
                    end = rayEnd,
                    strokeWidth = 2.8f
                )
            }

            // Sol pastel radiante
            val haloColor = if (esAtardecer) Color(0x60FB7185) else Color(0x80FDE047)
            val coreColor = if (esAtardecer) Color(0xFFFDBA74) else Color(0xFFFEF08A)

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(haloColor, Color(0x25FEF08A), Color.Transparent),
                    center = astroCenter,
                    radius = astroBaseRadius * 2.3f
                ),
                radius = astroBaseRadius * 2.3f,
                center = astroCenter
            )
            drawCircle(
                color = coreColor,
                radius = astroBaseRadius,
                center = astroCenter
            )
        }

        // 3. AVES CAMPESTRES EN EL CIELO DISTANTE
        if (!esNoche) {
            val birdOffset = (cloudProgress1 * 120f)
            drawBird(w * 0.30f + birdOffset, h * 0.13f, 1.0f)
            drawBird(w * 0.36f + birdOffset * 1.15f, h * 0.11f, 0.75f)
            drawBird(w * 0.25f + birdOffset * 0.9f, h * 0.16f, 0.60f)
        }

        // 4. NUBES FLOTANTES PASTEL
        val cloudTint = if (esAtardecer) Color(0xFFFEE2E2) else if (esNoche) Color(0x9094A3B8) else Color(0xF0FFFFFF)
        drawCloud(
            x = ((cloudProgress1 * (w + 240f)) - 120f),
            y = h * 0.08f,
            scale = 0.95f,
            colorTint = cloudTint
        )
        drawCloud(
            x = (((cloudProgress2 * (w + 280f)) + w * 0.45f) % (w + 280f) - 140f),
            y = h * 0.17f,
            scale = 0.75f,
            colorTint = cloudTint
        )
        drawCloud(
            x = (((cloudProgress1 * 0.7f * (w + 260f)) + w * 0.78f) % (w + 260f) - 130f),
            y = h * 0.23f,
            scale = 0.85f,
            colorTint = cloudTint
        )

        // 5. COLINAS DISTANTES (SALVIA / TONALIDAD DEL MOMENTO)
        val colorColinaLejana = when {
            esNoche -> Color(0xFF2A4837)
            esAtardecer -> Color(0xFFB5D8B5)
            else -> Color(0xFFC7E9C0)
        }
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
        drawPath(path = pathColinaLejana, color = colorColinaLejana)
        drawDistantTrees(w = w, h = h)

        // 6. COLINA MEDIA CON LA GRANJA, VALLA Y EL MOLINO
        val colorColinaMedia = when {
            esNoche -> Color(0xFF203B2B)
            esAtardecer -> Color(0xFF8FB88E)
            else -> Color(0xFFA1D99B)
        }
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
        drawPath(path = pathColinaMedia, color = colorColinaMedia)

        // Valla campestre de madera en la ladera
        drawWoodenFence(
            x = w * 0.32f,
            y = h * 0.44f,
            count = 5,
            scale = w / 420f
        )

        // Granja tradicional (Barn + Silo)
        drawGranjaBarn(
            x = w * 0.16f,
            y = h * 0.41f,
            scale = w / 420f
        )

        // Molino de viento de campo con aspas giratorias
        drawMolinoViento(
            x = w * 0.82f,
            y = h * 0.435f,
            angle = windmillAngle,
            scale = w / 420f
        )

        // 7. COLINA PRINCIPAL DE PRIMER PLANO (PRADERA VERDE)
        val colorColinaFrontal = when {
            esNoche -> Color(0xFF1A3323)
            esAtardecer -> Color(0xFF6A996D)
            else -> Color(0xFF74C69D)
        }
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
        drawPath(path = pathColinaFrontal, color = colorColinaFrontal)

        // 8. CAMPO DE GIRASOLES ANIMADOS MECEndOSE AL VIENTO
        drawCampoGirasoles(
            w = w,
            h = h,
            windSway = sunflowerWindSway
        )

        // 9. CAPA DE CRISTAL ESMERILADO PASTEL LUMINOSA (FROSTED SCRIM)
        // Opacidad suave (20%-35%) para que el paisaje vectorial resplandezca sin saturar el texto
        if (mostrarCapaFrosted) {
            val scrimColors = when {
                esNoche -> listOf(
                    Color(0x351E293B),
                    Color(0x5016221A),
                    Color(0x68121E15)
                )
                esAtardecer -> listOf(
                    Color(0x24FFF1F2),
                    Color(0x38FFF7ED),
                    Color(0x50F8FAF5)
                )
                else -> listOf(
                    Color(0x1CBAE6FD),
                    Color(0x30F7FBF8),
                    Color(0x42F1F8F3)
                )
            }
            drawRect(
                brush = Brush.verticalGradient(
                    colors = scrimColors,
                    startY = 0f,
                    endY = h
                ),
                size = Size(w, h)
            )
        }
    }
}

/**
 * Dibuja un ave campestre en vuelo distante (silueta en V con curvatura orgánica).
 */
private fun DrawScope.drawBird(x: Float, y: Float, scale: Float) {
    val birdColor = Color(0x65475569)
    val span = 9f * scale
    val path = Path().apply {
        moveTo(x - span, y + span * 0.4f)
        quadraticTo(x - span * 0.5f, y - span * 0.55f, x, y)
        quadraticTo(x + span * 0.5f, y - span * 0.55f, x + span, y + span * 0.4f)
    }
    drawPath(path = path, color = birdColor, style = Stroke(width = 1.4f * scale, cap = StrokeCap.Round))
}

/**
 * Dibuja una valla campestre de madera pastel con postes y rieles.
 */
private fun DrawScope.drawWoodenFence(x: Float, y: Float, count: Int, scale: Float) {
    val s = scale.coerceIn(0.7f, 1.2f)
    val postDist = 13f * s
    val postH = 15f * s
    val postW = 3f * s

    // Rieles horizontales
    drawLine(
        color = Color(0xFFCBD5E1),
        start = Offset(x, y - postH * 0.75f),
        end = Offset(x + count * postDist, y - postH * 0.75f),
        strokeWidth = 1.4f * s
    )
    drawLine(
        color = Color(0xFFCBD5E1),
        start = Offset(x, y - postH * 0.35f),
        end = Offset(x + count * postDist, y - postH * 0.35f),
        strokeWidth = 1.4f * s
    )

    // Postes verticales de estacas con punta
    for (i in 0..count) {
        val px = x + i * postDist
        val pathPoste = Path().apply {
            moveTo(px - postW / 2f, y)
            lineTo(px - postW / 2f, y - postH)
            lineTo(px, y - postH - 2.5f * s)
            lineTo(px + postW / 2f, y - postH)
            lineTo(px + postW / 2f, y)
            close()
        }
        drawPath(path = pathPoste, color = Color(0xFFF1F5F9))
        drawPath(path = pathPoste, color = Color(0xFF94A3B8), style = Stroke(width = 0.8f * s))
    }
}

/**
 * Dibuja una nube esponjosa compuesta por múltiples arcos suaves pastel.
 */
private fun DrawScope.drawCloud(x: Float, y: Float, scale: Float, colorTint: Color = Color(0xE8FFFFFF)) {
    val cloudColor = colorTint
    val shadowColor = Color(0x25BAE6FD)
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
 * Dibuja la granja tradicional (granero rústico en coral suave y silo con veleta).
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

    // Cuerpo del silo (gris azulado pastel con bandas de refuerzo)
    drawRect(
        color = Color(0xFFCBD5E1),
        topLeft = Offset(siloX, siloY + 8f * s),
        size = Size(siloWidth, siloHeight - 8f * s)
    )
    drawLine(Color(0xFF94A3B8), start = Offset(siloX, siloY + 18f * s), end = Offset(siloX + siloWidth, siloY + 18f * s), strokeWidth = 1f * s)
    drawLine(Color(0xFF94A3B8), start = Offset(siloX, siloY + 30f * s), end = Offset(siloX + siloWidth, siloY + 30f * s), strokeWidth = 1f * s)

    // Cúpula del silo
    drawArc(
        color = Color(0xFF94A3B8),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = true,
        topLeft = Offset(siloX, siloY),
        size = Size(siloWidth, 16f * s)
    )

    // Veleta en la cúpula del silo
    drawLine(Color(0xFF475569), start = Offset(siloX + siloWidth / 2f, siloY), end = Offset(siloX + siloWidth / 2f, siloY - 7f * s), strokeWidth = 1.4f * s)
    drawLine(Color(0xFF475569), start = Offset(siloX + siloWidth / 2f - 4f * s, siloY - 5f * s), end = Offset(siloX + siloWidth / 2f + 4f * s, siloY - 5f * s), strokeWidth = 1.2f * s)

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

    // Ventana en el ático del granero
    val winSize = 8f * s
    val winX = x + (barnWidth - winSize) / 2f
    val winY = y - 11f * s
    drawRect(Color(0xFFFEF08A), topLeft = Offset(winX, winY), size = Size(winSize, winSize))
    drawRect(Color(0xFFB91C1C), topLeft = Offset(winX, winY), size = Size(winSize, winSize), style = Stroke(1.2f * s))
    drawLine(Color(0xFFB91C1C), start = Offset(winX + winSize / 2f, winY), end = Offset(winX + winSize / 2f, winY + winSize), strokeWidth = 1f * s)
    drawLine(Color(0xFFB91C1C), start = Offset(winX, winY + winSize / 2f), end = Offset(winX + winSize, winY + winSize / 2f), strokeWidth = 1f * s)

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
 * Dibuja un molino de viento de campo con celosía y aspas giratorias.
 */
private fun DrawScope.drawMolinoViento(x: Float, y: Float, angle: Float, scale: Float) {
    val s = scale.coerceIn(0.7f, 1.3f)
    val towerW = 18f * s
    val towerH = 46f * s

    // Torre trapezoidal del molino
    val pathTorre = Path().apply {
        moveTo(x - towerW * 0.35f, y - towerH)
        lineTo(x + towerW * 0.35f, y - towerH)
        lineTo(x + towerW * 0.5f, y)
        lineTo(x - towerW * 0.5f, y)
        close()
    }
    drawPath(path = pathTorre, color = Color(0xFFF1F5F9))
    drawPath(path = pathTorre, color = Color(0xFF94A3B8), style = Stroke(width = 1.2f * s))

    // Tirantes de madera en X de la torre
    drawLine(Color(0xFFCBD5E1), start = Offset(x - towerW * 0.38f, y - towerH * 0.75f), end = Offset(x + towerW * 0.42f, y - towerH * 0.25f), strokeWidth = 1f * s)
    drawLine(Color(0xFFCBD5E1), start = Offset(x + towerW * 0.38f, y - towerH * 0.75f), end = Offset(x - towerW * 0.42f, y - towerH * 0.25f), strokeWidth = 1f * s)

    // Cúpula superior
    val hubCenter = Offset(x, y - towerH)
    drawCircle(
        color = Color(0xFF64748B),
        radius = 5.5f * s,
        center = hubCenter
    )

    // 4 Aspas del molino rotando continuamente con celosía dorada
    rotate(degrees = angle, pivot = hubCenter) {
        val bladeLength = 32f * s
        for (i in 0 until 4) {
            rotate(degrees = i * 90f, pivot = hubCenter) {
                drawLine(
                    color = Color(0xFF475569),
                    start = hubCenter,
                    end = Offset(hubCenter.x, hubCenter.y - bladeLength),
                    strokeWidth = 1.8f * s
                )
                drawRect(
                    color = Color(0xDDFEF08A),
                    topLeft = Offset(hubCenter.x + 1.5f * s, hubCenter.y - bladeLength),
                    size = Size(7.5f * s, bladeLength * 0.75f)
                )
            }
        }
    }
}

/**
 * Dibuja un campo abundante de girasoles con doble corona de pétalos dorados
 * y disco central texturado meciéndose suavemente con la brisa.
 */
private fun DrawScope.drawCampoGirasoles(w: Float, h: Float, windSway: Float) {
    data class SunflowerConfig(
        val relX: Float,
        val relY: Float,
        val scale: Float,
        val phase: Float
    )

    val girasoles = listOf(
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

        SunflowerConfig(0.08f, 0.76f, 0.82f, 1.1f),
        SunflowerConfig(0.19f, 0.79f, 0.88f, 2.6f),
        SunflowerConfig(0.28f, 0.75f, 0.78f, 0.3f),
        SunflowerConfig(0.39f, 0.80f, 0.92f, 1.8f),
        SunflowerConfig(0.49f, 0.76f, 0.85f, 3.0f),
        SunflowerConfig(0.60f, 0.81f, 0.90f, 0.7f),
        SunflowerConfig(0.70f, 0.77f, 0.84f, 2.2f),
        SunflowerConfig(0.81f, 0.82f, 0.95f, 1.5f),
        SunflowerConfig(0.91f, 0.78f, 0.80f, 3.4f),

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

        val currentSway = sin(windSway * 0.1f + item.phase) * (8f * scale)
        val headX = posX + currentSway
        val headY = baseY - stemHeight

        // 1. Tallo verde curvo
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

        // 2. Hojas laterales verde menta
        val leafY = baseY - stemHeight * 0.45f
        val leafX = posX + (currentSway * 0.35f)
        val pathHoja = Path().apply {
            moveTo(leafX, leafY)
            quadraticTo(leafX - 16f * scale, leafY - 10f * scale, leafX - 22f * scale, leafY - 4f * scale)
            quadraticTo(leafX - 12f * scale, leafY + 4f * scale, leafX, leafY)
            close()
        }
        drawPath(path = pathHoja, color = Color(0xFF52B788))

        // 3. Cabeza del girasol
        val headCenter = Offset(headX, headY)
        val centerRadius = 8.5f * scale
        val petalLength = 16f * scale
        val petalWidth = 6.5f * scale
        val petalCount = 14

        // Capa exterior de pétalos (amarillo limón pastel)
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
            drawPath(path = pathPetalo, color = if (p % 2 == 0) Color(0xFFFDE047) else Color(0xFFFEF08A))
        }

        // Capa interior de pétalos secundarios (dorado ámbar)
        for (p in 0 until (petalCount / 2)) {
            val angle = (p * (360f / (petalCount / 2f))) + 12f + (currentSway * 2.5f)
            val rad = angle * (PI / 180f).toFloat()
            val pTipX = headX + cos(rad) * (centerRadius + petalLength * 0.65f)
            val pTipY = headY + sin(rad) * (centerRadius + petalLength * 0.65f)
            drawCircle(Color(0xFFF59E0B), radius = 2.5f * scale, center = Offset(pTipX, pTipY))
        }

        // Disco central de semillas (marrón tostado y textura de micro-semillas)
        drawCircle(
            color = Color(0xFF78350F),
            radius = centerRadius,
            center = headCenter
        )
        drawCircle(
            color = Color(0xFF92400E),
            radius = centerRadius * 0.72f,
            center = headCenter
        )
        drawCircle(
            color = Color(0xFFB45309),
            radius = centerRadius * 0.40f,
            center = headCenter
        )
        // Punteado concéntrico de semillas
        for (s in 0 until 6) {
            val a = s * 60f * (PI / 180f).toFloat()
            drawCircle(
                color = Color(0xFFFDE68A),
                radius = 1f * scale,
                center = Offset(headCenter.x + cos(a) * (centerRadius * 0.55f), headCenter.y + sin(a) * (centerRadius * 0.55f))
            )
        }
    }
}
