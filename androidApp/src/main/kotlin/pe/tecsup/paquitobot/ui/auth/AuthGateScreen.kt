package pe.tecsup.paquitobot.ui.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pe.tecsup.paquitobot.ui.onboarding.WelcomeScreen
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Gate de Google. Figma no tiene frame de login: se reusa `112:3` (Welcome)
 * via [WelcomeScreen] para no duplicar el layout de entrada.
 */
@Composable
fun AuthGateScreen(
    isSigningIn: Boolean,
    errorMessage: String?,
    onSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    WelcomeScreen(
        onContinueClick = onSignInClick,
        modifier = modifier,
        title = "PaquitoBot",
        subtitle = "Conectá tu cuenta de Google para empezar",
        buttonText = "Conectar con Google",
        supportingText = "La primera vez puede tardar un minuto si el servidor estaba dormido.",
        errorMessage = errorMessage,
        isLoading = isSigningIn,
    )
}

@Preview(showBackground = true)
@Composable
private fun AuthGateScreenPreview() {
    PaquitoTheme {
        AuthGateScreen(
            isSigningIn = false,
            errorMessage = null,
            onSignInClick = {},
        )
    }
}
