package pe.tecsup.paquitobot.ui.canvas

import androidx.compose.foundation.background
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
 * Gate de Canvas. Sin frame Figma: mismo ritmo que Welcome (titulo + CTA
 * visible sobre system bars), sin el icono 100px que se superponia.
 */
@Composable
fun CanvasConnectScreen(
    uiState: CanvasConnectUiState,
    onConnectClick: (String) -> Unit,
    onContinueClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var canvasToken by remember { mutableStateOf("") }

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
            text = "PaquitoBot",
            style = PaquitoTypography.DisplayLarge,
            color = PaquitoColors.TextPrimary,
        )
        Text(
            text = "Conectá tu cuenta de Canvas",
            style = PaquitoTypography.HeadlineSmall,
            color = PaquitoColors.TextSecondary,
        )
        Text(
            text = "Pegá tu token de acceso para que Paquito responda con tus datos reales.",
            style = PaquitoTypography.BodySmall,
            color = PaquitoColors.TextHomeMuted,
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
                value = canvasToken,
                onValueChange = { canvasToken = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontSize = PaquitoTypography.BodyLarge.fontSize,
                    color = PaquitoColors.TextOnCardStrong,
                ),
                cursorBrush = SolidColor(PaquitoColors.BrandPrimary),
                decorationBox = { inner ->
                    if (canvasToken.isEmpty()) {
                        Text(
                            text = "Token de Canvas",
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
            onClick = { onConnectClick(canvasToken) },
            isLoading = uiState.isConnecting,
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                style = PaquitoTypography.BodySmall,
                color = PaquitoColors.StateDanger,
            )
        }
    }

    if (uiState.isConnected) {
        AlertDialog(
            onDismissRequest = onContinueClick,
            confirmButton = {
                TextButton(onClick = onContinueClick) {
                    Text("Continuar")
                }
            },
            title = { Text("¡Listo!") },
            text = {
                Text(
                    uiState.syncWarning
                        ?: "Tu cuenta de Canvas se conectó y sincronizó correctamente.",
                )
            },
        )
    }
}

@Preview(showBackground = true, name = "4. Canvas")
@Composable
private fun CanvasConnectScreenPreview() {
    PaquitoTheme {
        CanvasConnectScreen(
            uiState = CanvasConnectUiState(),
            onConnectClick = {},
            onContinueClick = {},
        )
    }
}
