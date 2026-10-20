import SwiftUI

struct PaquitoPrimaryButton: View {
    let title: String
    var isLoading = false
    var action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: PaquitoSpacing.sm) {
                if isLoading {
                    ProgressView()
                        .tint(PaquitoColors.textOnPrimary)
                }
                Text(isLoading ? "Cargando…" : title)
                    .font(PaquitoTypography.button)
            }
            .foregroundStyle(PaquitoColors.textOnPrimary)
            .frame(maxWidth: .infinity)
            .frame(minHeight: 52)
            .background(PaquitoColors.brandPrimary)
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.large, style: .continuous))
        }
        .disabled(isLoading)
        .accessibilityLabel(isLoading ? "Cargando" : title)
        .accessibilityAddTraits(.isButton)
    }
}
