package pe.tecsup.paquitobot.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Welcome screen del onboarding (Figma 112:3).
 *
 * Tokens observados en Figma el 2026-08-05 (ver requerimientos/UI_INTEGRATION.md):
 *   - bg botón        = #00C9FB  → PaquitoColors.BrandPrimary
 *   - texto botón     = #FFFFFF  → PaquitoColors.TextOnPrimary
 *   - texto título    = rgba(0,0,0,0.85) → PaquitoColors.TextPrimary
 *   - texto subtítulo = rgba(0,0,0,0.7)  → PaquitoColors.TextSecondary
 *   - fondo pantalla  = #FFFFFF  → PaquitoColors.Background
 *   - radio grande    = 20dp
 *   - botón alto      = 51dp
 *   - tamaño título   = 48px Figma (34sp Android)
 *   - tamaño subtítulo = 32px Figma (23sp Android)
 */
@Composable
fun WelcomeScreen(
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background),
    ) {
        // Imagen decorativa del personaje Paquito (Figma nodeId 2005:360).
        // En Figma está en absolute, debajo del bloque central; en Compose
        // la anclamos a la parte superior para que no choque con el botón en
        // pantallas de poca altura.
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 11.dp, top = 359.dp)
                .size(width = 236.dp, height = 321.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.paquito_personaje),
                contentDescription = "Personaje Paquito",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        // Bloque central (Figma nodeId 112:134 body): icono bot + título + subtítulo + botón.
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(
                    start = PaquitoSpacing.lg,
                    end = PaquitoSpacing.lg,
                    bottom = PaquitoSpacing.xxl,
                )
                .padding(top = PaquitoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.md),
        ) {
            // Ícono del bot (Figma nodeId 112:133): rounded-rect 100x100 con imagen adentro.
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(PaquitoShapes.large.topStart))
                    .background(Color.White),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.paquito_bot_icon),
                    contentDescription = "PaquitoBot",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                )
            }

            // Título + subtítulo (Figma nodeId 112:132 content).
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "PaquitoBot",
                    style = PaquitoTypography.DisplayLarge,
                    color = PaquitoColors.TextPrimary,
                )
                Text(
                    text = "Configuremos tu asistente",
                    style = PaquitoTypography.HeadlineSmall,
                    color = PaquitoColors.TextSecondary,
                )
            }

            // Botón "Continuar" (Figma nodeId 112:44 Botón?).
            // bg #00C9FB, rounded 20, height 51, padding 15.
            Button(
                onClick = onContinueClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp),
                shape = PaquitoShapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaquitoColors.BrandPrimary,
                    contentColor = PaquitoColors.TextOnPrimary,
                ),
                contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp),
            ) {
                Text(
                    text = "Continuar",
                    style = PaquitoTypography.BodyLarge,
                    color = PaquitoColors.TextOnPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WelcomeScreenPreviewLight() {
    PaquitoTheme {
        WelcomeScreen(onContinueClick = {})
    }
}