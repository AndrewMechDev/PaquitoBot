package pe.tecsup.paquitobot.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Entrada decorativa de marca; no sustituye Welcome ni modifica la configuración guardada.
 * Se dibuja con recursos existentes y Compose, sin video ni dependencias de animación.
 */
@Composable
fun LaunchIntroScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val preview = LocalInspectionMode.current
    val progress = remember { Animatable(if (preview) 0.86f else 0f) }
    val finished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        if (!preview) {
            // Animatable respeta la escala de animación del sistema, incluido movimiento desactivado.
            progress.animateTo(1f, tween(2200, easing = FastOutSlowInEasing))
            finished()
        }
    }
    LaunchIntroArtwork(progress.value, modifier, onSkip = { finished() })
}

@Composable
private fun LaunchIntroArtwork(progress: Float, modifier: Modifier = Modifier, onSkip: () -> Unit = {}) {
    fun phase(start: Float, end: Float) = ((progress - start) / (end - start)).coerceIn(0f, 1f)
    val arrival = phase(0.08f, 0.70f)
    val reveal = phase(0.28f, 0.75f)
    BoxWithConstraints(modifier.fillMaxSize().background(PaquitoColors.Background)) {
        val compact = maxHeight < 480.dp
        val artSize = minOf(maxWidth * 0.88f, maxHeight * if (compact) 0.38f else 0.53f, 340.dp)
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Brush.radialGradient(listOf(PaquitoColors.BrandPrimary.copy(alpha = 0.11f), Color.Transparent),
                center = center.copy(y = size.height * 0.44f), radius = size.width * 0.70f),
                radius = size.width * 0.70f, center = center.copy(y = size.height * 0.44f))
            // Reflejo inferior tenue: conserva el lienzo blanco de la referencia.
            drawOval(Brush.verticalGradient(listOf(Color.Transparent, PaquitoColors.BrandPrimary.copy(alpha = 0.07f))),
                topLeft = androidx.compose.ui.geometry.Offset(-size.width * 0.25f, size.height * 0.85f),
                size = androidx.compose.ui.geometry.Size(size.width * 1.5f, size.height * 0.34f))
        }
        Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = 24.dp, vertical = if (compact) 12.dp else 28.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(artSize), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().graphicsLayer { alpha = arrival; scaleX = 0.75f + arrival * 0.25f; scaleY = scaleX }) {
                    val radius = size.minDimension * 0.35f
                    drawCircle(Brush.radialGradient(listOf(Color.White, PaquitoColors.BrandPrimary.copy(alpha = 0.04f))), radius)
                    drawCircle(Brush.sweepGradient(listOf(Color.White, PaquitoColors.BrandPrimary.copy(alpha = 0.28f),
                        Color.White, PaquitoColors.StateInfo.copy(alpha = 0.12f), Color.White)), radius, style = Stroke(2.dp.toPx()))
                    drawCircle(Color.White.copy(alpha = 0.75f), radius - 4.dp.toPx(), style = Stroke(1.dp.toPx()))
                }
                Image(painterResource(R.drawable.paquito_personaje), contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(artSize * 0.58f).graphicsLayer {
                        alpha = reveal
                        scaleX = 0.72f + reveal * 0.28f; scaleY = scaleX
                        translationY = (1f - reveal) * 32.dp.toPx() - sin(progress * PI).toFloat() * 5.dp.toPx()
                    })
                val icons = listOf(R.drawable.ic_paquito_book_fill, R.drawable.ic_paquito_calendar_fill,
                    R.drawable.ic_paquito_bell_outline, R.drawable.ic_paquito_lab_profile)
                icons.forEachIndexed { index, icon ->
                    val angle = (-135.0 + index * 90.0 + (1f - arrival) * 32.0) * PI / 180.0
                    val radius = artSize * (0.36f + (1f - arrival) * 0.22f)
                    val iconSize = minOf(artSize * 0.17f, 54.dp)
                    Box(Modifier.offset(x = radius * cos(angle).toFloat(), y = radius * sin(angle).toFloat())
                        .size(iconSize).graphicsLayer {
                            alpha = phase(index * 0.045f, 0.36f + index * 0.045f)
                            rotationZ = (1f - arrival) * if (index % 2 == 0) -18f else 18f
                            scaleX = 0.72f + arrival * 0.28f; scaleY = scaleX
                        }.shadow(5.dp, RoundedCornerShape(18.dp), ambientColor = PaquitoColors.StateInfo,
                            spotColor = PaquitoColors.StateInfo.copy(alpha = 0.18f))
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(Color.White.copy(alpha = 0.96f),
                            PaquitoColors.OverlayInfoSubtle.copy(alpha = 0.65f), Color.White.copy(alpha = 0.90f))))
                        .border(1.dp, Color.White, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                        Image(painterResource(icon), contentDescription = null, modifier = Modifier.fillMaxSize(0.42f),
                            colorFilter = ColorFilter.tint(PaquitoColors.StateInfoStrong))
                    }
                }
            }
            Spacer(Modifier.height(if (compact) 8.dp else 20.dp))
            Column(Modifier.graphicsLayer { alpha = reveal; translationY = (1f - reveal) * 16.dp.toPx() },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("PaquitoBot", style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextPrimary, textAlign = TextAlign.Center)
                Text("Tu compañero académico", style = PaquitoTypography.BodyLarge,
                    color = PaquitoColors.TextSecondary, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(if (compact) 12.dp else 28.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.graphicsLayer { alpha = reveal }) {
                repeat(3) { index ->
                    Box(Modifier.size(if (index == 1) 6.dp else 4.dp)
                        .background(PaquitoColors.BrandPrimary.copy(alpha = if (index == 1) 0.90f else 0.25f), CircleShape))
                }
            }
        }
        TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 8.dp)) {
            Text("Omitir", style = PaquitoTypography.BodySmall, color = PaquitoColors.StateInfoStrong)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Apertura · PaquitoBot")
@Composable
private fun LaunchIntroPreview() { PaquitoTheme { LaunchIntroArtwork(0.86f) } }
