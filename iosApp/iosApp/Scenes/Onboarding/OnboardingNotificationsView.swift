import SwiftUI

struct OnboardingNotificationsView: View {
    let onContinue: (Set<NotificationPreference>) -> Void
    @State private var variant: NotificationCardVariant = .compact
    @State private var enabled: Set<NotificationPreference> = [.laboratorios, .plazos]

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Notificaciones")
                .font(PaquitoTypography.displayLarge)
            Text("Activa tus avisos")
                .font(PaquitoTypography.headlineSmall)
                .foregroundStyle(PaquitoColors.textSecondary)
                .padding(.top, 10)

            HStack(spacing: 4) {
                toggle("Cuadros", selected: variant == .compact) { variant = .compact }
                toggle("Lista", selected: variant == .full) { variant = .full }
            }
            .padding(4)
            .background(PaquitoColors.surfaceElevated)
            .clipShape(Capsule())
            .padding(.top, 20)

            ScrollView {
                if variant == .compact {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 16) {
                        ForEach(NotificationPreference.allCases, id: \.self) { preference in
                            NotificationPreferenceCard(preference: preference, variant: .compact, isEnabled: enabled.contains(preference)) {
                                toggle(preference)
                            }
                        }
                    }
                } else {
                    VStack(spacing: 10) {
                        ForEach(NotificationPreference.allCases, id: \.self) { preference in
                            NotificationPreferenceCard(preference: preference, variant: .full, isEnabled: enabled.contains(preference)) {
                                toggle(preference)
                            }
                        }
                    }
                }
            }
            .scrollIndicators(.hidden)
            .padding(.top, 20)

            PaquitoPrimaryButton(title: "Continuar") {
                onContinue(enabled)
            }
            .padding(.top, 16)
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.top, PaquitoSpacing.xl)
        .padding(.bottom, PaquitoSpacing.lg)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
    }

    @ViewBuilder
    private func toggle(_ title: String, selected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(selected ? PaquitoColors.textOnPrimary : PaquitoColors.textSecondary)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 10)
                .background(selected ? PaquitoColors.brandPrimary : .clear)
                .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }

    private func toggle(_ preference: NotificationPreference) {
        if enabled.contains(preference) { enabled.remove(preference) } else { enabled.insert(preference) }
    }
}
