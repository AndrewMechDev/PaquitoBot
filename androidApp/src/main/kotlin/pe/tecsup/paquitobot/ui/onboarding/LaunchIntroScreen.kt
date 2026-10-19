package pe.tecsup.paquitobot.ui.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.*
import kotlin.math.PI
import kotlin.math.sin

/** Paquito es el protagonista. Reutiliza el personaje, colores y tipografía de Welcome. */
@Composable
fun LaunchIntroScreen(onFinished: () -> Unit, modifier: Modifier = Modifier) {
    val preview = LocalInspectionMode.current
    val progress = remember { Animatable(if (preview) 0.78f else 0f) }
    val finished by rememberUpdatedState(onFinished)
    LaunchedEffect(Unit) {
        if (!preview) {
            progress.animateTo(1f, tween(2300, easing = LinearEasing))
            finished()
        }
    }
    LaunchIntroArtwork(progress.value, modifier, onSkip = { finished() })
}

@Composable
private fun LaunchIntroArtwork(progress: Float, modifier: Modifier = Modifier, onSkip: () -> Unit = {}) {
    fun phase(start: Float, end: Float) = FastOutSlowInEasing.transform(((progress - start) / (end - start)).coerceIn(0f, 1f))
    val entrance = phase(0f, 0.44f)
    val lettering = phase(0.30f, 0.67f)
    BoxWithConstraints(modifier.fillMaxSize().background(PaquitoColors.Background)) {
        val compact = maxHeight < 480.dp
        val characterSize = minOf(maxWidth * 0.80f, maxHeight * if (compact) 0.43f else 0.46f, 320.dp)
        Column(Modifier.fillMaxSize().systemBarsPadding().padding(horizontal = PaquitoSpacing.lg, vertical = 20.dp),
            verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(characterSize), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize().graphicsLayer { alpha = entrance }) {
                    val glow = Offset(center.x, size.height * 0.48f)
                    drawCircle(Brush.radialGradient(listOf(PaquitoColors.BrandPrimary.copy(alpha = 0.13f), Color.Transparent),
                        center = glow, radius = size.width * 0.48f), radius = size.width * 0.48f, center = glow)
                    val shadow = Offset(center.x, size.height * 0.90f)
                    drawOval(Brush.radialGradient(listOf(PaquitoColors.StateInfoStrong.copy(alpha = 0.12f), Color.Transparent),
                        center = shadow, radius = size.width * 0.32f),
                        topLeft = Offset(size.width * 0.16f, size.height * 0.84f), size = Size(size.width * 0.68f, size.height * 0.13f))
                }
                Image(painterResource(R.drawable.paquito_personaje), contentDescription = "Paquito, tu asistente académico",
                    contentScale = ContentScale.Fit, modifier = Modifier.fillMaxSize(0.90f).graphicsLayer {
                        alpha = entrance
                        scaleX = 0.84f + entrance * 0.16f; scaleY = scaleX
                        translationY = (1f - entrance) * 32.dp.toPx() - sin(progress * PI).toFloat() * 6.dp.toPx()
                        rotationZ = (1f - entrance) * -5f
                    })
            }
            Spacer(Modifier.height(if (compact) 8.dp else 24.dp))
            Column(Modifier.graphicsLayer { alpha = lettering; translationY = (1f - lettering) * 18.dp.toPx() },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("PaquitoBot", style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextPrimary, textAlign = TextAlign.Center)
                Text("Tu compañero académico", style = PaquitoTypography.BodyLarge,
                    color = PaquitoColors.TextSecondary, textAlign = TextAlign.Center)
            }
        }
        TextButton(onClick = onSkip, modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding().padding(bottom = 8.dp)) {
            Text("Omitir", style = PaquitoTypography.BodySmall, color = PaquitoColors.StateInfoStrong)
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800, name = "Apertura · Paquito al centro")
@Composable
private fun LaunchIntroPreview() { PaquitoTheme { LaunchIntroArtwork(0.78f) } }
