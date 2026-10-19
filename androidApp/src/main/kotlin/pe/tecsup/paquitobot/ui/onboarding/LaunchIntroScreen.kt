package pe.tecsup.paquitobot.ui.onboarding

import android.graphics.Paint
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.theme.*
import kotlin.math.*

/** Entrada de marca con una escultura de vidrio tridimensional dibujada en Canvas.
 * No usa videos, red ni imágenes nuevas; conserva el flujo de sesión y onboarding.
 */
@Composable
fun LaunchIntroScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val preview = LocalInspectionMode.current
    val progress = remember { Animatable(if (preview) 0.76f else 0f) }
    val finished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        if (!preview) {
            progress.animateTo(1f, tween(2850, easing = LinearEasing))
            finished()
        }
    }
    LaunchIntroArtwork(progress.value, modifier, onSkip = { finished() })
}

@Composable
private fun LaunchIntroArtwork(progress: Float, modifier: Modifier = Modifier, onSkip: () -> Unit = {}) {
    fun phase(start: Float, end: Float) = FastOutSlowInEasing.transform(((progress - start) / (end - start)).coerceIn(0f, 1f))
    val entrance = phase(0f, 0.43f)
    val lettering = phase(0.42f, 0.76f)
    val renderer = remember { GlassSculpture() }
    BoxWithConstraints(modifier.fillMaxSize().background(PaquitoColors.Background)) {
        val compact = maxHeight < 480.dp
        val sculptureSize = minOf(maxWidth * 0.92f, maxHeight * if (compact) 0.45f else 0.43f, 380.dp)
        Canvas(Modifier.fillMaxSize()) {
            val light = androidx.compose.ui.geometry.Offset(size.width * 0.67f, size.height * 0.29f)
            drawCircle(Brush.radialGradient(listOf(Color(0xFFF0FBFF), Color.White.copy(alpha = 0f)),
                center = light, radius = size.width * 0.9f), radius = size.width * 0.9f, center = light)
        }
        Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(sculptureSize), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().graphicsLayer {
                    alpha = entrance
                    scaleX = 0.64f + entrance * 0.36f; scaleY = scaleX
                    translationY = (1f - entrance) * 45.dp.toPx() - sin(progress * PI).toFloat() * 8.dp.toPx()
                }) {
                    val shadowCenter = androidx.compose.ui.geometry.Offset(center.x, size.height * 0.85f)
                    drawOval(Brush.radialGradient(listOf(PaquitoColors.StateInfoStrong.copy(alpha = 0.10f), Color.Transparent),
                        center = shadowCenter, radius = size.width * 0.40f),
                        topLeft = androidx.compose.ui.geometry.Offset(size.width * 0.12f, size.height * 0.76f),
                        size = androidx.compose.ui.geometry.Size(size.width * 0.76f, size.height * 0.18f))
                    drawIntoCanvas { canvas -> renderer.draw(canvas.nativeCanvas, size.width, size.height, progress, entrance) }
                }
            }
            Spacer(Modifier.height(if (compact) 2.dp else 26.dp))
            Column(Modifier.graphicsLayer { alpha = lettering; translationY = (1f - lettering) * 24.dp.toPx() },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Paquito", style = PaquitoTypography.DisplayLarge.copy(fontSize = if (compact) 32.sp else 46.sp,
                        letterSpacing = (-2).sp, fontWeight = FontWeight.SemiBold), color = PaquitoColors.TextOnPrimaryDim)
                    Text("Bot", style = PaquitoTypography.DisplayLarge.copy(fontSize = if (compact) 32.sp else 46.sp,
                        letterSpacing = (-2).sp, fontWeight = FontWeight.Light), color = PaquitoColors.StateInfoStrong)
                }
                Text("Tu mundo académico, en orden.", style = PaquitoTypography.BodyMedium,
                    color = PaquitoColors.TextSecondary, textAlign = TextAlign.Center)
            }
        }
        TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 8.dp)) {
            Text("Omitir", style = PaquitoTypography.BodySmall, color = PaquitoColors.TextSecondary)
        }
    }
}

/** Malla toroidal: profundidad real, perspectiva y reflejos según la normal de cada cara.
 * Se dibuja en una sola llamada nativa con colores interpolados y triángulos ordenados por profundidad.
 */
private class GlassSculpture {
    private val segments = 80
    private val sides = 18
    private val count = segments * sides
    private val positions = FloatArray(count * 2)
    private val colors = IntArray(count)
    private val depths = FloatArray(count)
    private val indices = ShortArray(count * 6)
    private val faces = Array(count) { it }
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    fun draw(canvas: android.graphics.Canvas, width: Float, height: Float, progress: Float, entrance: Float) {
        val tilt = 1.18f - entrance * 0.60f
        val yaw = -0.64f + entrance * 0.24f + progress * 0.30f
        val roll = -0.48f + entrance * 0.18f
        val cx = cos(tilt); val sx = sin(tilt)
        val cy = cos(yaw); val sy = sin(yaw)
        val cz = cos(roll); val sz = sin(roll)
        val scale = width * 0.405f
        for (i in 0 until segments) {
            val u = (i * 2.0 * PI / segments).toFloat()
            val cu = cos(u); val su = sin(u)
            val major = 0.69f + 0.035f * sin(u * 2f + progress * 1.4f)
            for (j in 0 until sides) {
                val v = (j * 2.0 * PI / sides).toFloat()
                val cv = cos(v); val sv = sin(v)
                val radius = 0.225f + 0.018f * cos(u - progress)
                val x = (major + radius * cv) * cu
                val y = (major + radius * cv) * su
                val z = radius * sv
                val y1 = y * cx - z * sx; val z1 = y * sx + z * cx
                val x2 = x * cy + z1 * sy; val z2 = -x * sy + z1 * cy
                val index = i * sides + j
                val perspective = 3.5f / (3.5f - z2)
                positions[index * 2] = width * 0.5f + (x2 * cz - y1 * sz) * scale * perspective
                positions[index * 2 + 1] = height * 0.45f + (x2 * sz + y1 * cz) * scale * perspective
                depths[index] = z2
                val nx = cu * cv; val ny = su * cv; val nz = sv
                val ny1 = ny * cx - nz * sx; val nz1 = ny * sx + nz * cx
                val nx2 = nx * cy + nz1 * sy; val nz2 = -nx * sy + nz1 * cy
                val nx3 = nx2 * cz - ny1 * sz
                val ny3 = nx2 * sz + ny1 * cz
                val light = (nx3 * -0.35f + ny3 * -0.55f + nz2 * 0.76f).coerceAtLeast(0f)
                val specular = light.pow(22f)
                val edge = (1f - abs(nz2)).pow(2f)
                val band = (0.5f + 0.5f * sin(nx3 * 5.0f + ny3 * 3f + progress * 2.3f)).pow(3f)
                val tint = (0.12f + edge * 0.48f + band * 0.27f).coerceIn(0f, 1f)
                fun channel(clear: Float, blue: Float) = ((clear * (1f - tint) + blue * tint) * (1f - specular) + 255f * specular).toInt().coerceIn(0, 255)
                colors[index] = android.graphics.Color.rgb(channel(248f, 18f), channel(253f, 174f), channel(255f, 218f))
            }
        }
        fun next(index: Int) = ((index / sides + 1) % segments) * sides + index % sides
        faces.sortBy { index -> (depths[index] + depths[next(index)]) * 0.5f }
        var output = 0
        for (a in faces) {
            val b = next(a)
            val d = a / sides * sides + (a % sides + 1) % sides
            val c = next(d)
            indices[output++] = a.toShort(); indices[output++] = b.toShort(); indices[output++] = c.toShort()
            indices[output++] = a.toShort(); indices[output++] = c.toShort(); indices[output++] = d.toShort()
        }
        canvas.drawVertices(android.graphics.Canvas.VertexMode.TRIANGLES, positions.size, positions, 0,
            null, 0, colors, 0, indices, 0, indices.size, paint)
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Apertura · Cristal")
@Composable
private fun LaunchIntroPreview() { PaquitoTheme { LaunchIntroArtwork(0.76f) } }
