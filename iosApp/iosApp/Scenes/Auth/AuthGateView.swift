import SwiftUI

struct AuthGateView: View {
    let isSigningIn: Bool
    let errorMessage: String?
    let onSignIn: () -> Void

    var body: some View {
        WelcomeView(
            title: "PaquitoBot",
            subtitle: "Conectá tu cuenta de Google para empezar",
            buttonTitle: "Conectar con Google",
            supportingText: "La primera vez puede tardar un minuto si el servidor estaba dormido.",
            errorMessage: errorMessage,
            isLoading: isSigningIn,
            onContinue: onSignIn
        )
    }
}
