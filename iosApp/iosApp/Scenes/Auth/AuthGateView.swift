import SwiftUI

struct AuthGateView: View {
    let isSigningIn: Bool
    let isUnauthorized: Bool
    let errorMessage: String?
    let onSignIn: () -> Void
    let onSignOut: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()

            PaquitoMascotView()
                .padding(.bottom, 24)

            Text(isUnauthorized ? "Cuenta no autorizada" : "Conecta tu cuenta")
                .font(PaquitoTypography.display)
                .foregroundStyle(PaquitoColors.textPrimary)
                .multilineTextAlignment(.center)

            Text(
                isUnauthorized
                    ? "Solo se permite un correo principal verificado de @tecsup.edu.pe."
                    : "Usa tu cuenta institucional para entrar a PaquitoBot."
            )
            .font(PaquitoTypography.body)
            .foregroundStyle(PaquitoColors.textSecondary)
            .multilineTextAlignment(.center)
            .padding(.top, 10)
            .frame(maxWidth: 340)

            if let errorMessage {
                Text(errorMessage)
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.stateDanger)
                    .multilineTextAlignment(.center)
                    .padding(.top, 16)
            }

            Spacer()

            PaquitoPrimaryButton(
                title: isUnauthorized ? "Cerrar sesión y usar otro correo" : "Iniciar sesión con Clerk",
                isLoading: isSigningIn,
                action: isUnauthorized ? onSignOut : onSignIn
            )
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.vertical, PaquitoSpacing.lg)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
    }
}
