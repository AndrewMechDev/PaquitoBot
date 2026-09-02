import SwiftUI

struct AcademicConnectView: View {
    let isConnecting: Bool
    let errorMessage: String?
    let onConnect: (String) -> Void
    let onSkip: () -> Void

    @State private var apiKey = ""

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Spacer()
            Text("Datos de demo")
                .font(PaquitoTypography.displayLarge)
            Text("Conectá una cuenta de prueba para ver cursos, notas y asistencia reales (datos ficticios).")
                .font(PaquitoTypography.headlineSmall)
                .foregroundStyle(PaquitoColors.textSecondary)

            TextField("stu_001", text: $apiKey)
                .textInputAutocapitalization(.never)
                .autocorrectionDisabled()
                .font(PaquitoTypography.bodyLarge)
                .padding(.horizontal, 17)
                .frame(height: 56)
                .background(PaquitoColors.surfaceElevated)
                .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.large, style: .continuous))

            PaquitoPrimaryButton(title: "Conectar", isLoading: isConnecting, isEnabled: !apiKey.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty) {
                onConnect(apiKey)
            }

            Button("Omitir por ahora", action: onSkip)
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.infoStrong)
                .buttonStyle(.plain)

            if let errorMessage {
                Text(errorMessage)
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.danger)
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
