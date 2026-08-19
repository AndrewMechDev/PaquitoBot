package pe.tecsup.paquitobot.ui.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Paso de onboarding que ata los 3 dolores a las pestañas.
 * No hay frame Figma: reusa tokens de Welcome / notificaciones.
 */
@Composable
fun OnboardingTourScreen(
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background)
            .systemBarsPadding()
            .padding(horizontal = PaquitoSpacing.lg)
            .padding(top = 32.dp, bottom = PaquitoSpacing.lg),
    ) {
        Text(
            text = "Asi te ayudo",
            style = PaquitoTypography.DisplayLarge,
            color = PaquitoColors.TextPrimary,
        )
        Text(
            text = "Tres cosas que suelen enterarse tarde",
            style = PaquitoTypography.HeadlineSmall,
            color = PaquitoColors.TextSecondary,
            modifier = Modifier.padding(top = 10.dp, bottom = 20.dp),
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TourCard(
                iconRes = R.drawable.ic_paquito_lab_profile,
                title = "Como vas, de verdad",
                body = "4 practicas y 8 labs no caben en la cabeza. Ves el promedio ahora, no cuando ya no hay tiempo.",
                tabHint = "Pestaña Cursos",
            )
            TourCard(
                iconRes = R.drawable.ic_paquito_frame_person,
                title = "Faltas antes del limite",
                body = "5 inasistencias = jalado, aunque las notas esten bien. Te aviso en 3 y en 4.",
                tabHint = "Pestaña Horarios",
            )
            TourCard(
                iconRes = R.drawable.ic_paquito_docs_default,
                title = "Una sola lista de entregas",
                body = "Foros, labs y practicas viven en el aula, el correo y el pizarron. Acá salen juntas.",
                tabHint = "Pestaña Inicio",
            )
        }
        PaquitoPrimaryButton(
            text = "Continuar",
            onClick = onContinue,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun TourCard(
    iconRes: Int,
    title: String,
    body: String,
    tabHint: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PaquitoShapes.large)
            .background(PaquitoColors.SurfaceElevated)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            contentScale = ContentScale.Fit,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = PaquitoTypography.TaskTitle,
                color = PaquitoColors.TextOnCardStrong,
            )
            Text(
                text = body,
                style = PaquitoTypography.BodySmall,
                color = PaquitoColors.TextOnCardMuted,
            )
            Text(
                text = tabHint,
                style = PaquitoTypography.Caption,
                color = PaquitoColors.TextLink,
            )
        }
    }
}

@Preview(showBackground = true, name = "2b Tour dolores")
@Composable
private fun OnboardingTourScreenPreview() {
    PaquitoTheme { OnboardingTourScreen(onContinue = {}) }
}
