package pe.tecsup.paquitobot.ui.auth

import androidx.compose.runtime.Composable
import com.clerk.ui.auth.AuthView

/**
 * Gate de login: Clerk renderiza registro, inicio de sesion, MFA y los
 * providers sociales habilitados en el Dashboard (sin codigo por provider).
 */
@Composable
fun ClerkAuthGate() {
    AuthView()
}
