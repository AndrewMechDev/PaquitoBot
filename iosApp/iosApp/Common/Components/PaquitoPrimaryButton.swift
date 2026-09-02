import SwiftUI

struct PaquitoPrimaryButton: View {
    let title: String
    var isLoading = false
    var isEnabled = true
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Group {
                if isLoading {
                    ProgressView().tint(PaquitoColors.textOnPrimary)
                } else {
                    Text(title)
                }
            }
            .font(PaquitoTypography.buttonLabel)
            .foregroundStyle(PaquitoColors.textOnPrimary)
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(isEnabled ? PaquitoColors.brandPrimary : PaquitoColors.textMuted.opacity(0.4))
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.large, style: .continuous))
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled || isLoading)
    }
}
