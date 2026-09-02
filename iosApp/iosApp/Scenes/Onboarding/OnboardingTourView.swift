import SwiftUI

struct OnboardingTourView: View {
    let onContinue: () -> Void

    private let cards = [
        ("chart.bar.xaxis", "Como vas, de verdad", "Ves el promedio ahora, no cuando ya no hay tiempo.", "Pestaña Cursos"),
        ("person.crop.circle", "Faltas antes del limite", "Te aviso antes de llegar al límite aunque las notas estén bien.", "Pestaña Horarios"),
        ("doc.text", "Una sola lista de entregas", "Foros, labs y prácticas salen juntas en una misma vista.", "Pestaña Inicio")
    ]

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Así te ayudo")
                .font(PaquitoTypography.displayLarge)
            Text("Tres cosas que suelen enterarse tarde")
                .font(PaquitoTypography.headlineSmall)
                .foregroundStyle(PaquitoColors.textSecondary)
                .padding(.top, 10)
                .padding(.bottom, 20)

            ScrollView {
                VStack(spacing: 12) {
                    ForEach(cards, id: \.1) { card in
                        HStack(alignment: .top, spacing: 14) {
                            Image(systemName: card.0)
                                .font(.system(size: 22, weight: .semibold))
                                .foregroundStyle(PaquitoColors.infoStrong)
                                .frame(width: 40, height: 40)
                            VStack(alignment: .leading, spacing: 4) {
                                Text(card.1).font(PaquitoTypography.taskTitle)
                                Text(card.2).font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textMuted)
                                Text(card.3).font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.infoStrong)
                            }
                        }
                        .padding(16)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .background(PaquitoColors.surfaceElevated)
                        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous))
                    }
                }
            }
            .scrollIndicators(.hidden)

            PaquitoPrimaryButton(title: "Continuar", action: onContinue)
                .padding(.top, 16)
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.top, PaquitoSpacing.xl)
        .padding(.bottom, PaquitoSpacing.lg)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
    }
}
