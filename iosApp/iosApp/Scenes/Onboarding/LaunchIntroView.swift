import SwiftUI

struct LaunchIntroView: View {
    let onFinished: () -> Void

    @State private var progress = 0.0
    @State private var didFinish = false

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(spacing: 22) {
                PaquitoMascotView()
                    .frame(maxWidth: 280, maxHeight: 320)
                    .opacity(entrance)
                    .scaleEffect(0.84 + entrance * 0.16)
                    .offset(y: (1 - entrance) * 32 - sin(progress * .pi) * 6)
                    .rotationEffect(.degrees((1 - entrance) * -5))

                VStack(spacing: 10) {
                    Text("PaquitoBot")
                        .font(PaquitoTypography.display)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text("Tu compañero académico")
                        .font(PaquitoTypography.body)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
                .opacity(lettering)
                .offset(y: (1 - lettering) * 18)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            .padding(.horizontal, PaquitoSpacing.lg)

            Button("Omitir", action: finish)
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.stateInfoStrong)
                .padding(.bottom, PaquitoSpacing.sm)
        }
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
        .task {
            withAnimation(.linear(duration: 2.3)) {
                progress = 1
            }
            try? await Task.sleep(for: .milliseconds(2300))
            finish()
        }
    }

    private var entrance: Double {
        ease((progress - 0.0) / 0.44)
    }

    private var lettering: Double {
        ease((progress - 0.30) / 0.37)
    }

    private func ease(_ value: Double) -> Double {
        min(max(value, 0), 1)
    }

    private func finish() {
        guard !didFinish else { return }
        didFinish = true
        onFinished()
    }
}
