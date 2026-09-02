import SwiftUI

struct PaquitoPrimaryButtonStyle: ButtonStyle {
    var isLoading = false

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(PaquitoTypography.buttonLabel)
            .foregroundStyle(PaquitoColors.textOnPrimary)
            .frame(maxWidth: .infinity)
            .frame(height: 56)
            .background(PaquitoColors.brandPrimary.opacity(configuration.isPressed ? 0.78 : 1))
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.large, style: .continuous))
    }
}

struct PaquitoBackButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "chevron.left")
                .font(.system(size: 21, weight: .semibold))
                .foregroundStyle(PaquitoColors.textPrimary)
                .frame(width: 44, height: 44)
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

struct PaquitoLoadingView: View {
    var body: some View {
        ProgressView()
            .tint(PaquitoColors.textOnPrimary)
            .scaleEffect(1.05)
    }
}
