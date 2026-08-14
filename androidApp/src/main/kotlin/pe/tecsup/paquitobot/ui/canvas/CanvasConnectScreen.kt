package pe.tecsup.paquitobot.ui.canvas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont

/**
 * Segundo gate de la app (2026-08-13): pide el token de Canvas del
 * estudiante y lo manda a `POST /auth/canvas/connect`. Solo se muestra una
 * vez por sesion - tras un connect exitoso, [SessionViewModel] marca
 * `isCanvasConnected = true` y `MainActivity` deja de mostrar esta
 * pantalla (ni Home ni Chat vuelven a pedirlo).
 *
 * ⚠️ El token se escribe DIRECTO en este campo, nunca se pega en el chat
 * del asistente ni se comparte con Claude - es una credencial real del
 * estudiante (ver `requerimientos/BACKEND_INTEGRATION.md`).
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
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Conectá tu cuenta de Canvas",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextOnCardStrong,
        )
        Text(
            text = "Por ahora, pegá tu token de acceso de Canvas para que Paquito pueda responder con tus datos reales.",
            fontSize = 14.sp,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeMuted,
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(47.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(PaquitoColors.TextInputPlaceholder.copy(alpha = 0.08f))
                .padding(horizontal = 17.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            BasicTextField(
                value = canvasToken,
                onValueChange = { canvasToken = it },
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(fontSize = 14.sp, color = PaquitoColors.TextOnCardStrong),
                cursorBrush = SolidColor(PaquitoColors.BrandPrimary),
                decorationBox = { inner ->
                    if (canvasToken.isEmpty()) {
                        Text(
                            text = "Token de Canvas",
                            fontSize = 14.sp,
                            color = PaquitoColors.TextInputPlaceholder,
                        )
                    }
                    inner()
                },
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (uiState.isConnecting) PaquitoColors.TextHomeMuted else PaquitoColors.BrandPrimary)
                .clickable(enabled = !uiState.isConnecting) { onConnectClick(canvasToken) }
                .padding(horizontal = 24.dp, vertical = 14.dp),
        ) {
            if (uiState.isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(2.dp),
                    color = PaquitoColors.TextOnWhite,
                )
            } else {
                Text(
                    text = "Conectar",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PaquitoFont.DMSans,
                    color = PaquitoColors.TextOnWhite,
                )
            }
        }

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage,
                fontSize = 13.sp,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextHomeMuted,
                modifier = Modifier.padding(top = 16.dp),
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
