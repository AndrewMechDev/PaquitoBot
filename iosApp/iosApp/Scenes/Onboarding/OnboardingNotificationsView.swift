import SwiftUI

struct OnboardingNotificationsView: View {
    let isRequestingPermission: Bool
    let initialSetup: NotificationSetup
    let onContinue: (NotificationSetup) -> Void
    var onCancel: (() -> Void)? = nil

    @State private var step = 0
    @State private var selected: Set<NotificationPreference>
    @State private var isCompact = true
    @State private var reminderHours: Int
    @State private var dailySummaryEnabled: Bool
    @State private var summaryDate: Date

    init(
        isRequestingPermission: Bool,
        initialSetup: NotificationSetup = .default,
        onContinue: @escaping (NotificationSetup) -> Void,
        onCancel: (() -> Void)? = nil
    ) {
        self.isRequestingPermission = isRequestingPermission
        self.initialSetup = initialSetup
        self.onContinue = onContinue
        self.onCancel = onCancel
        _selected = State(initialValue: initialSetup.enabled)
        _reminderHours = State(initialValue: initialSetup.reminderHours)
        _dailySummaryEnabled = State(initialValue: initialSetup.dailySummaryEnabled)
        _summaryDate = State(initialValue: Calendar.current.date(
            bySettingHour: initialSetup.summaryHour,
            minute: initialSetup.summaryMinute,
            second: 0,
            of: .now
        ) ?? .now)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            header
            ScrollView {
                Group {
                    switch step {
                    case 0: categoriesStep
                    case 1: timingStep
                    default: summaryStep
                    }
                }
                .padding(.top, 24)
                .padding(.bottom, 20)
            }
            .scrollIndicators(.hidden)
            footer
        }
        .padding(.horizontal, PaquitoSpacing.lg)
        .padding(.top, PaquitoSpacing.xl)
        .padding(.bottom, PaquitoSpacing.lg)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
    }

    private var header: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("Notificaciones")
                .font(PaquitoTypography.display)
                .foregroundStyle(PaquitoColors.textPrimary)
            Text("Paso \(step + 1) de 3")
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(PaquitoColors.textSecondary)
            ProgressView(value: Double(step + 1), total: 3)
                .tint(PaquitoColors.brandPrimary)
                .accessibilityLabel("Progreso de configuración")
        }
    }

    private var categoriesStep: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("¿Qué avisos quieres recibir?")
                .font(PaquitoTypography.headline)
                .foregroundStyle(PaquitoColors.textPrimary)
            Text("Elige una o varias categorías. Puedes cambiarlas después.")
                .font(PaquitoTypography.body)
                .foregroundStyle(PaquitoColors.textSecondary)
            Picker("Presentación", selection: $isCompact) {
                Text("Cuadros").tag(true)
                Text("Lista").tag(false)
            }
            .pickerStyle(.segmented)
            if isCompact {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 14) {
                    ForEach(NotificationPreference.allCases, id: \.self, content: preferenceCard)
                }
            } else {
                LazyVStack(spacing: 10) {
                    ForEach(NotificationPreference.allCases, id: \.self, content: preferenceRow)
                }
            }
        }
    }

    private var timingStep: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("¿Cuándo te recordamos?")
                .font(PaquitoTypography.headline)
                .foregroundStyle(PaquitoColors.textPrimary)
            Text("Define con cuánta anticipación quieres recibir los recordatorios de entregas.")
                .font(PaquitoTypography.body)
                .foregroundStyle(PaquitoColors.textSecondary)
            VStack(alignment: .leading, spacing: 12) {
                Text("Anticipación")
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                LazyVGrid(columns: [GridItem(.adaptive(minimum: 72))], spacing: 10) {
                    ForEach([1, 6, 12, 24, 48], id: \.self) { hours in
                        Button("\(hours) h") { reminderHours = hours }
                            .font(PaquitoTypography.bodyMedium.weight(.semibold))
                            .foregroundStyle(reminderHours == hours ? PaquitoColors.textOnPrimary : PaquitoColors.textPrimary)
                            .frame(maxWidth: .infinity, minHeight: 48)
                            .background(reminderHours == hours ? PaquitoColors.brandPrimary : PaquitoColors.background)
                            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
                            .overlay(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous)
                                .stroke(reminderHours == hours ? PaquitoColors.brandPrimary : PaquitoColors.borderDefault, lineWidth: 1))
                    }
                }
            }
            .paquitoCard()
        }
    }

    private var summaryStep: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Organiza tu resumen diario")
                .font(PaquitoTypography.headline)
                .foregroundStyle(PaquitoColors.textPrimary)
            Text("Recibe una vista rápida de tus avisos académicos cada día.")
                .font(PaquitoTypography.body)
                .foregroundStyle(PaquitoColors.textSecondary)
            VStack(alignment: .leading, spacing: 14) {
                Toggle("Resumen diario", isOn: $dailySummaryEnabled)
                    .font(PaquitoTypography.bodyMedium)
                    .tint(PaquitoColors.stateInfoStrong)
                if dailySummaryEnabled {
                    DatePicker("Hora del resumen", selection: $summaryDate, displayedComponents: .hourAndMinute)
                        .font(PaquitoTypography.bodyMedium)
                        .tint(PaquitoColors.stateInfoStrong)
                    Text("Los avisos usan la hora local del dispositivo.")
                        .font(PaquitoTypography.caption)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
            }
            .paquitoCard()
            VStack(alignment: .leading, spacing: 8) {
                Label("Permiso de notificaciones", systemImage: "bell.badge")
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text("iOS te pedirá permiso para mostrar recordatorios y el resumen de Paquito.")
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
            .paquitoCard()
        }
    }

    private var footer: some View {
        VStack(spacing: 8) {
            HStack(spacing: 12) {
                if step > 0 {
                    Button("Atrás") { step -= 1 }
                        .font(PaquitoTypography.body)
                        .foregroundStyle(PaquitoColors.textPrimary)
                        .frame(minHeight: 48)
                }
                PaquitoPrimaryButton(
                    title: step == 2 ? (onCancel == nil ? "Finalizar" : "Guardar cambios") : "Continuar",
                    isLoading: isRequestingPermission
                ) {
                    if step < 2 {
                        withAnimation(.easeOut(duration: 0.25)) { step += 1 }
                    } else {
                        let components = Calendar.current.dateComponents([.hour, .minute], from: summaryDate)
                        onContinue(NotificationSetup(
                            enabled: selected,
                            reminderHours: reminderHours,
                            dailySummaryEnabled: dailySummaryEnabled,
                            summaryHour: components.hour ?? 8,
                            summaryMinute: components.minute ?? 0
                        ))
                    }
                }
            }
            if let onCancel {
                Button("Cancelar", action: onCancel)
                    .font(PaquitoTypography.body)
                    .foregroundStyle(PaquitoColors.textPrimary)
                    .frame(maxWidth: .infinity, minHeight: 44)
            }
        }
    }

    private func preferenceCard(_ preference: NotificationPreference) -> some View {
        Button { toggle(preference) } label: {
            VStack(alignment: .leading, spacing: 12) {
                Image(systemName: preference.systemImage)
                    .font(.title2)
                    .foregroundStyle(selected.contains(preference) ? PaquitoColors.stateInfoStrong : PaquitoColors.textSecondary)
                Text(preference.rawValue)
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                    .multilineTextAlignment(.leading)
                Image(systemName: selected.contains(preference) ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(selected.contains(preference) ? PaquitoColors.brandPrimary : PaquitoColors.textMuted)
            }
            .frame(maxWidth: .infinity, minHeight: 132, alignment: .leading)
            .padding(16)
            .background(selected.contains(preference) ? PaquitoColors.brandPrimary.opacity(0.10) : PaquitoColors.surfaceGlass)
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.large, style: .continuous))
            .overlay(RoundedRectangle(cornerRadius: PaquitoRadius.large, style: .continuous)
                .stroke(selected.contains(preference) ? PaquitoColors.brandPrimary.opacity(0.45) : PaquitoColors.borderSubtle, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityValue(selected.contains(preference) ? "Activado" : "Desactivado")
    }

    private func preferenceRow(_ preference: NotificationPreference) -> some View {
        Button { toggle(preference) } label: {
            HStack(spacing: 14) {
                Image(systemName: preference.systemImage)
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
        .accessibilityValue(selected.contains(preference) ? "Activado" : "Desactivado")
    }

    private func toggle(_ preference: NotificationPreference) {
        if selected.contains(preference) { selected.remove(preference) }
        else { selected.insert(preference) }
    }
}
