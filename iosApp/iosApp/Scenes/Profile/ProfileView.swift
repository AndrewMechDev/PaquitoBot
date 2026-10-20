import SwiftUI

struct ProfileView: View {
    let data: ProfileData
    let isSigningOut: Bool
    let errorMessage: String?
    let onProcedures: () -> Void
    let onEnrollment: () -> Void
    let onEvents: () -> Void
    let onSignOut: () -> Void

    @State private var showSignOutConfirmation = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                Text("Mi perfil")
                    .font(PaquitoTypography.display)
                    .foregroundStyle(PaquitoColors.textPrimary)

                VStack(spacing: 12) {
                    Text(initials)
                        .font(PaquitoTypography.headline.weight(.semibold))
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                        .frame(width: 76, height: 76)
                        .background(PaquitoColors.brandPrimary.opacity(0.14))
                        .clipShape(Circle())
                    Text(data.name)
                        .font(PaquitoTypography.headline)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text(data.email)
                        .font(PaquitoTypography.bodyMedium)
                        .foregroundStyle(PaquitoColors.textSecondary)
                    Text("Cuenta institucional")
                        .font(PaquitoTypography.caption)
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                }
                .frame(maxWidth: .infinity)
                .paquitoCard()

                Text("Servicios al estudiante")
                    .font(PaquitoTypography.headline)
                    .foregroundStyle(PaquitoColors.textPrimary)

                ProfileActionRow(title: "Procedimientos", subtitle: "Requisitos y canales de atención", systemImage: "doc.text.magnifyingglass", action: onProcedures)
                ProfileActionRow(title: "Guía de matrícula", subtitle: "Pasos para completar tu matrícula", systemImage: "checklist", action: onEnrollment)
                ProfileActionRow(title: "Eventos del campus", subtitle: "Actividades y comunicados institucionales", systemImage: "sparkles.rectangle.stack", action: onEvents)

                Text("Los cursos, notas, horarios y tareas son datos de demostración. No corresponden a tu cuenta institucional.")
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textSecondary)

                Button {
                    showSignOutConfirmation = true
                } label: {
                    HStack {
                        Spacer()
                        if isSigningOut { ProgressView().tint(PaquitoColors.stateDanger) }
                        Text(isSigningOut ? "Cerrando sesión…" : "Cerrar sesión")
                            .font(PaquitoTypography.bodyMedium.weight(.semibold))
                        Spacer()
                    }
                    .foregroundStyle(PaquitoColors.stateDanger)
                    .frame(minHeight: 48)
                    .overlay(RoundedRectangle(cornerRadius: PaquitoRadius.medium).stroke(PaquitoColors.stateDanger, lineWidth: 1))
                }
                .disabled(isSigningOut)

                if let errorMessage {
                    Text(errorMessage)
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.stateDanger)
                }
            }
            .padding(.horizontal, PaquitoSpacing.lg)
            .padding(.top, PaquitoSpacing.md)
            .padding(.bottom, PaquitoSpacing.lg)
        }
        .scrollIndicators(.hidden)
        .background(PaquitoColors.background)
        .confirmationDialog("¿Cerrar sesión?", isPresented: $showSignOutConfirmation, titleVisibility: .visible) {
            Button("Cerrar sesión", role: .destructive, action: onSignOut)
            Button("Volver", role: .cancel) {}
        } message: {
            Text("Podrás volver a ingresar con tu cuenta institucional.")
        }
    }

    private var initials: String {
        data.name.split(separator: " ").prefix(2).compactMap(\.first).map(String.init).joined().uppercased()
    }
}

private struct ProfileActionRow: View {
    let title: String
    let subtitle: String
    let systemImage: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 14) {
                Image(systemName: systemImage)
                    .font(.title3)
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                    .frame(width: 30)
                VStack(alignment: .leading, spacing: 4) {
                    Text(title)
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text(subtitle)
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(PaquitoColors.textMuted)
            }
            .contentShape(Rectangle())
            .paquitoCard()
        }
        .buttonStyle(.plain)
    }
}
