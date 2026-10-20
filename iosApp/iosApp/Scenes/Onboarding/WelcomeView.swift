import SwiftUI

struct WelcomeView: View {
    let onContinue: () -> Void
    var onEnrollmentGuide: (() -> Void)? = nil

    var body: some View {
        VStack(spacing: 0) {
            Spacer(minLength: 24)

            PaquitoMascotView()
                .padding(.bottom, 28)

            VStack(spacing: 10) {
                Text("PaquitoBot")
                    .font(PaquitoTypography.display)
                    .foregroundStyle(PaquitoColors.textPrimary)
                    .multilineTextAlignment(.center)
                Text("Configuremos tu asistente")
                    .font(PaquitoTypography.headline)
                    .foregroundStyle(PaquitoColors.textSecondary)
                    .multilineTextAlignment(.center)
            }

            Spacer(minLength: 24)

            PaquitoPrimaryButton(title: "Continuar", action: onContinue)

            if let onEnrollmentGuide {
                Button("Consultar guía de matrícula", action: onEnrollmentGuide)
                    .font(PaquitoTypography.bodyMedium.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                    .frame(maxWidth: .infinity, minHeight: 44)
            }
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.vertical, PaquitoSpacing.lg)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
    }
}
