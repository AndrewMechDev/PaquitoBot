import SwiftUI
import UIKit

struct WelcomeView: View {
    let title: String
    let subtitle: String
    let buttonTitle: String
    var supportingText: String?
    var errorMessage: String?
    var isLoading = false
    let onContinue: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Spacer()
            PaquitoMascotView()
                .frame(width: 220, height: 280)
                .padding(.bottom, 28)

            VStack(spacing: 10) {
                Text(title)
                    .font(PaquitoTypography.displayLarge)
                    .foregroundStyle(PaquitoColors.textPrimary)
                    .multilineTextAlignment(.center)
                Text(subtitle)
                    .font(PaquitoTypography.headlineSmall)
                    .foregroundStyle(PaquitoColors.textSecondary)
                    .multilineTextAlignment(.center)
            }

            Spacer()

            if let supportingText {
                Text(supportingText)
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.textMuted)
                    .multilineTextAlignment(.center)
                    .padding(.bottom, 10)
            }
            if let errorMessage {
                Text(errorMessage)
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.danger)
                    .multilineTextAlignment(.center)
                    .padding(.bottom, 10)
            }

            PaquitoPrimaryButton(title: buttonTitle, isLoading: isLoading, action: onContinue)
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.top, PaquitoSpacing.md)
        .padding(.bottom, PaquitoSpacing.lg)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
    }
}

private struct PaquitoMascotView: View {
    var body: some View {
        ZStack {
            if let image = UIImage(named: "paquito_personaje") {
                Image(uiImage: image)
                    .resizable()
                    .scaledToFit()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                Circle()
                    .fill(PaquitoColors.brandPrimary.opacity(0.12))
                    .frame(width: 180, height: 180)
                Image(systemName: "face.smiling.inverse")
                    .font(.system(size: 105, weight: .regular))
                    .foregroundStyle(PaquitoColors.infoStrong)
            }
        }
        .accessibilityLabel("PaquitoBot")
    }
}
