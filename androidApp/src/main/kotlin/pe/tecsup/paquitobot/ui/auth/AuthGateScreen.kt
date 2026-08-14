package pe.tecsup.paquitobot.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont

/**
 * Primer gate de la app (2026-08-13): sin sesion de Google no se ve nada
 * mas, ni Home ni Chat. Antes vivia adentro de `ChatScreen` como
 * `SignInGate` - se movio aca porque ahora bloquea toda la app, no solo el
 * chat (decision del usuario). No viene de ningun frame de Figma (el login
 * no estaba diseñado todavia) - estilo minimo, coherente con la paleta.
 */
@Composable
fun AuthGateScreen(
    isSigningIn: Boolean,
    errorMessage: String?,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Conectá tu cuenta de Google para empezar a usar PaquitoBot",
            fontSize = 16.sp,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeMuted,
        )
        Text(
            text = "La primera vez puede tardar un minuto si el servidor estaba dormido.",
            fontSize = 13.sp,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeMuted,
            modifier = Modifier.padding(top = 8.dp),
        )
        Box(
            modifier = Modifier
                .padding(top = 24.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(if (isSigningIn) PaquitoColors.TextHomeMuted else PaquitoColors.BrandPrimary)
                .clickable(enabled = !isSigningIn, onClick = onSignInClick)
                .padding(horizontal = 24.dp, vertical = 14.dp),
        ) {
            if (isSigningIn) {
                CircularProgressIndicator(
                    modifier = Modifier.padding(2.dp),
                    color = PaquitoColors.TextOnWhite,
                )
            } else {
                Text(
                    text = "Conectar con Google",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    fontFamily = PaquitoFont.DMSans,
                    color = PaquitoColors.TextOnWhite,
                )
            }
        }
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                fontSize = 13.sp,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextHomeMuted,
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}
