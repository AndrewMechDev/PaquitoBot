package pe.tecsup.paquitobot.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Welcome / intro (Figma `112:3`).
 *
 * El frame tiene DOS assets de Paquito (`112:133` icono 100px y `366:86`
 * mascota). En dispositivo se superponian sobre el titulo. Decision de UX
 * (2026-08-14): se deja SOLO la mascota de atras; el icono cuadrado no se
 * pinta. El CTA usa [systemBarsPadding] porque la Activity es edge-to-edge
 * y sin eso "Continuar" queda bajo la barra de gestos.
 */
@Composable
fun WelcomeScreen(
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
    title: String = "PaquitoBot",
    subtitle: String = "Configuremos tu asistente",
    buttonText: String = "Continuar",
    supportingText: String? = null,
    errorMessage: String? = null,
    isLoading: Boolean = false,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background)
            .systemBarsPadding()
            .padding(horizontal = PaquitoSpacing.lg)
            .padding(bottom = PaquitoSpacing.lg),
        horizontalAlignment = Alignment.Start,
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Image(
            painter = painterResource(id = R.drawable.paquito_personaje),
            contentDescription = "Personaje Paquito",
            modifier = Modifier
                .width(220.dp)
                .height(280.dp)
                .align(Alignment.CenterHorizontally),
            contentScale = ContentScale.Fit,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = title,
            style = PaquitoTypography.DisplayLarge,
            color = PaquitoColors.TextPrimary,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = subtitle,
            style = PaquitoTypography.HeadlineSmall,
            color = PaquitoColors.TextSecondary,
            modifier = Modifier.fillMaxWidth(0.85f),
        )

        Spacer(modifier = Modifier.height(28.dp))

        PaquitoPrimaryButton(
            text = buttonText,
            onClick = onContinueClick,
            isLoading = isLoading,
        )

        if (supportingText != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = supportingText,
                style = PaquitoTypography.BodySmall,
                color = PaquitoColors.TextHomeMuted,
            )
        }
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                style = PaquitoTypography.BodySmall,
                color = PaquitoColors.StateDanger,
            )
        }
    }
}

@Preview(showBackground = true, name = "1. Welcome")
@Composable
private fun WelcomeScreenPreviewLight() {
    PaquitoTheme {
        WelcomeScreen(onContinueClick = {})
    }
}
