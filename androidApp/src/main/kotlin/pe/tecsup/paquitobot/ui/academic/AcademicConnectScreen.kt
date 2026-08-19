package pe.tecsup.paquitobot.ui.academic

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Gate OPCIONAL de `canvas-mock` (ver skill `canvas-mock-backend`). A
 * diferencia del gate de Canvas real, este SIEMPRE tiene salida sin
 * conectar ([onSkipClick]) - los datos mock son una demo, no un
 * requisito para usar el resto de la app.
 */
@Composable
fun AcademicConnectScreen(
    uiState: AcademicConnectUiState,
    onConnectClick: (String) -> Unit,
    onSkipClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var apiKey by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background)
            .systemBarsPadding()
            .padding(horizontal = PaquitoSpacing.lg)
            .padding(top = 32.dp, bottom = PaquitoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "Datos de demo",
            style = PaquitoTypography.DisplayLarge,
            color = PaquitoColors.TextPrimary,
        )
        Text(
            text = "Conectá una cuenta de prueba para ver cursos, notas y asistencia reales (datos ficticios).",
            style = PaquitoTypography.HeadlineSmall,
            color = PaquitoColors.TextSecondary,
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .clip(PaquitoShapes.large)
                .background(PaquitoColors.SurfaceElevated)
                .padding(horizontal = 17.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = apiKey,
                onValueChange = { apiKey = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = PaquitoTypography.BodyLarge.fontSize,
                    color = PaquitoColors.TextOnCardStrong,
                ),
                cursorBrush = SolidColor(PaquitoColors.BrandPrimary),
                decorationBox = { inner ->
                    if (apiKey.isEmpty()) {
                        Text(
                            text = "stu_001",
                            style = PaquitoTypography.BodyLarge,
                            color = PaquitoColors.TextInputPlaceholder,
                        )
                    }
                    inner()
                },
            )
        }

        PaquitoPrimaryButton(
            text = "Conectar",
            onClick = { onConnectClick(apiKey) },
            isLoading = uiState.isConnecting,
        )

        Text(
            text = "Omitir por ahora",
            style = PaquitoTypography.BodySmall,
            color = PaquitoColors.TextLink,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clickable(onClick = onSkipClick),
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                style = PaquitoTypography.BodySmall,
                color = PaquitoColors.StateDanger,
            )
        }
    }
}

@Preview(showBackground = true, name = "5. Datos mock")
@Composable
private fun AcademicConnectScreenPreview() {
    PaquitoTheme {
        AcademicConnectScreen(
            uiState = AcademicConnectUiState(),
            onConnectClick = {},
            onSkipClick = {},
        )
    }
}
