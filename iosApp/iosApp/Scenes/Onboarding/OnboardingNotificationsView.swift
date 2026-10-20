import SwiftUI

struct OnboardingNotificationsView: View {
    let isRequestingPermission: Bool
    let onContinue: (Set<NotificationPreference>) -> Void
    @State private var selected: Set<NotificationPreference> = [.assignments, .reminders]
    @State private var isCompact = true

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Notificaciones")
                .font(PaquitoTypography.display)
                .foregroundStyle(PaquitoColors.textPrimary)
            Text("Activa tus avisos importantes")
                .font(PaquitoTypography.headline)
                .foregroundStyle(PaquitoColors.textSecondary)
                .padding(.top, 8)

            Picker("Vista", selection: $isCompact) {
                Text("Cuadros").tag(true)
                Text("Lista").tag(false)
            }
            .pickerStyle(.segmented)
            .padding(.top, 20)
            .accessibilityLabel("Vista de preferencias")

            ScrollView {
                if isCompact {
                    LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                        ForEach(NotificationPreference.allCases, id: \.self) { preference in
                            preferenceCard(preference)
                        }
                    }
                } else {
                    LazyVStack(spacing: 10) {
                        ForEach(NotificationPreference.allCases, id: \.self) { preference in
                            preferenceRow(preference)
                        }
                    }
                }
            }
            .scrollIndicators(.hidden)
            .padding(.top, 20)

            PaquitoPrimaryButton(title: "Continuar", isLoading: isRequestingPermission) {
                onContinue(selected)
            }
            .padding(.top, 16)
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.top, PaquitoSpacing.xl)
        .padding(.bottom, PaquitoSpacing.lg)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
    }

    private func preferenceCard(_ preference: NotificationPreference) -> some View {
        Button {
            toggle(preference)
        } label: {
            VStack(alignment: .leading, spacing: 12) {
                Image(systemName: icon(for: preference))
                    .font(.title2)
                    .foregroundStyle(selected.contains(preference) ? PaquitoColors.stateInfoStrong : PaquitoColors.textSecondary)
                Text(preference.rawValue)
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Image(systemName: selected.contains(preference) ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(selected.contains(preference) ? PaquitoColors.brandPrimary : PaquitoColors.textMuted)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(16)
            .background(selected.contains(preference) ? PaquitoColors.brandPrimary.opacity(0.10) : PaquitoColors.surfaceGlass)
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
            .overlay(
                RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous)
                    .stroke(selected.contains(preference) ? PaquitoColors.brandPrimary.opacity(0.45) : PaquitoColors.borderSubtle, lineWidth: 1)
            )
        }
        .buttonStyle(.plain)
        .accessibilityLabel(preference.rawValue)
        .accessibilityValue(selected.contains(preference) ? "Activado" : "Desactivado")
    }

    private func preferenceRow(_ preference: NotificationPreference) -> some View {
        Button {
            toggle(preference)
        } label: {
            HStack(spacing: 14) {
                Image(systemName: icon(for: preference))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                    .frame(width: 28)
                Text(preference.rawValue)
                    .font(PaquitoTypography.body)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Spacer()
                Image(systemName: selected.contains(preference) ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(selected.contains(preference) ? PaquitoColors.brandPrimary : PaquitoColors.textMuted)
            }
            .padding(16)
            .background(PaquitoColors.surfaceGlass)
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityLabel(preference.rawValue)
        .accessibilityValue(selected.contains(preference) ? "Activado" : "Desactivado")
    }

    private func toggle(_ preference: NotificationPreference) {
        if selected.contains(preference) {
            selected.remove(preference)
        } else {
            selected.insert(preference)
        }
    }

    private func icon(for preference: NotificationPreference) -> String {
        switch preference {
        case .assignments: return "checklist"
        case .grades: return "chart.bar"
        case .attendance: return "person.crop.circle.badge.checkmark"
        case .reminders: return "bell"
        }
    }
}
