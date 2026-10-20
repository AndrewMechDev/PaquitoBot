import SwiftUI

struct OnboardingTourView: View {
    let onContinue: () -> Void
    @State private var page = 0
    @Environment(\.accessibilityReduceMotion) private var reduceMotion

    private let cards = [
        ("progress", "Entiende cómo vas", "Mira tu promedio proyectado y detecta dónde puedes mejorar.", "chart.line.uptrend.xyaxis"),
        ("attendance", "No pierdas el ritmo", "Consulta tus faltas y conoce el límite de cada curso.", "person.crop.circle.badge.checkmark"),
        ("tasks", "Recuerda tus entregas", "Paquito reúne tus pendientes para que sepas qué vence primero.", "checklist")
    ]

    var body: some View {
        VStack(spacing: 0) {
            Text("Así te ayudo")
                .font(PaquitoTypography.display)
                .foregroundStyle(PaquitoColors.textPrimary)
                .frame(maxWidth: .infinity, alignment: .leading)
            Text(cards[page].1)
                .font(PaquitoTypography.headline)
                .foregroundStyle(PaquitoColors.textSecondary)
                .frame(maxWidth: .infinity, alignment: .leading)
                .padding(.top, 8)

            TabView(selection: $page) {
                ForEach(Array(cards.enumerated()), id: \.offset) { index, card in
                    VStack(spacing: 20) {
                        Image(systemName: card.3)
                            .font(.system(size: 72, weight: .medium))
                            .foregroundStyle(PaquitoColors.stateInfoStrong)
                            .frame(maxWidth: .infinity)
                        Text(card.2)
                            .font(PaquitoTypography.body)
                            .foregroundStyle(PaquitoColors.textSecondary)
                            .multilineTextAlignment(.center)
                            .frame(maxWidth: 320)
                    }
                    .padding(24)
                    .background(PaquitoColors.surfaceGlass)
                    .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous))
                    .tag(index)
                }
            }
            .tabViewStyle(.page(indexDisplayMode: .always))
            .padding(.top, 28)

            PaquitoPrimaryButton(title: page == cards.count - 1 ? "Empezar" : "Continuar") {
                if page < cards.count - 1 {
                    withAnimation(reduceMotion ? nil : .easeOut(duration: 0.25)) {
                        page += 1
                    }
                } else {
                    onContinue()
                }
            }
            .padding(.top, 20)
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.top, PaquitoSpacing.xl)
        .padding(.bottom, PaquitoSpacing.lg)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
    }
}
