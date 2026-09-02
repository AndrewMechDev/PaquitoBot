import SwiftUI

struct CanvasConnectView: View {
    let isConnecting: Bool
    let errorMessage: String?
    let onConnect: (String) -> Void

    @State private var token = ""

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Spacer()
            Text("Conectá Canvas")
                .font(PaquitoTypography.displayLarge)
            Text("Usá tu token personal para sincronizar tus datos académicos.")
                .font(PaquitoTypography.headlineSmall)
                .foregroundStyle(PaquitoColors.textSecondary)
            SecureField("Token de Canvas", text: $token)
                .padding(.horizontal, 17)
                .frame(height: 56)
                .background(PaquitoColors.surfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.large, style: .continuous))
            PaquitoPrimaryButton(title: "Conectar", isLoading: isConnecting, isEnabled: !token.isEmpty) {
                onConnect(token)
            }
            if let errorMessage {
                Text(errorMessage).font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.danger)
            }
            Spacer()
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.top, PaquitoSpacing.xl)
        .padding(.bottom, PaquitoSpacing.lg)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
    }
}
