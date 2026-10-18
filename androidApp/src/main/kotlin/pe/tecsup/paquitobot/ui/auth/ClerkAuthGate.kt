package pe.tecsup.paquitobot.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clerk.api.Clerk
import com.clerk.ui.auth.AuthView
import kotlinx.coroutines.delay
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Gate de login: Clerk renderiza registro, inicio de sesion, MFA y los
 * providers sociales habilitados en el Dashboard (sin codigo por provider).
 */
@Composable
fun ClerkAuthGate(
    isUnauthorizedSession: Boolean = false,
    isSigningOut: Boolean = false,
    signOutError: String? = null,
    onSignOutClick: () -> Unit = {},
) {
    val isInitialized by Clerk.isInitialized.collectAsStateWithLifecycle()
    var isTakingTooLong by remember { mutableStateOf(false) }

    LaunchedEffect(isInitialized) {
        isTakingTooLong = false
        if (!isInitialized) {
            delay(10_000)
            isTakingTooLong = true
        }
    }

    if (isUnauthorizedSession) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Solo se permite el acceso con un correo principal verificado de @tecsup.edu.pe.",
                style = PaquitoTypography.BodyMedium,
                color = PaquitoColors.TextSecondary,
            )
            Button(
                onClick = onSignOutClick,
                enabled = !isSigningOut,
                modifier = Modifier.padding(top = 20.dp),
            ) {
                Text(if (isSigningOut) "Cerrando sesión…" else "Cerrar sesión y usar otro correo")
            }
            signOutError?.let { error ->
                Text(
                    text = error,
                    modifier = Modifier.padding(top = 12.dp),
                    style = PaquitoTypography.BodyMedium,
                    color = PaquitoColors.TextSecondary,
                )
            }
        }
    } else if (isInitialized) {
        AuthView()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CircularProgressIndicator()
            Text(
                text = if (isTakingTooLong) {
                    "No se pudo cargar Clerk. Revisa la conexión a internet del dispositivo; el inicio de sesión se reintentará automáticamente."
                } else {
                    "Cargando inicio de sesión con Clerk…"
                },
                modifier = Modifier.padding(top = 20.dp),
                style = PaquitoTypography.BodyMedium,
                color = PaquitoColors.TextSecondary,
            )
        }
    }
}
