import SwiftUI

struct CalendarSyncView: View {
    @State private var connected = false
    @State private var showPast = false
    @State private var selected: Set<String> = Set(DemoAcademicData.calendarTasks.filter { !$0.isPast }.map(\.id))
    @State private var isSyncing = false
    @State private var summary: CalendarSyncSummary?
    @State private var message: String?

    private var visibleTasks: [CalendarTask] {
        DemoAcademicData.calendarTasks.filter { showPast || !$0.isPast }
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    accountCard
                    Text("Datos académicos de demostración · horario de Perú. La sincronización es manual y no elimina eventos existentes.")
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textSecondary)
                    Toggle("Mostrar tareas vencidas", isOn: $showPast)
                        .font(PaquitoTypography.bodyMedium)
                        .tint(PaquitoColors.stateInfoStrong)
                    Text("Elige tus entregas")
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    ForEach(visibleTasks) { task in
                        taskRow(task)
                    }
                    if let message {
                        Text(message)
                            .font(PaquitoTypography.bodySmall)
                            .foregroundStyle(PaquitoColors.stateInfoStrong)
                            .paquitoCard()
                    }
                    if let summary {
                        Text("\(summary.created) creados · \(summary.updated) actualizados · \(summary.unchanged) sin cambios")
                            .font(PaquitoTypography.bodySmall)
                            .foregroundStyle(PaquitoColors.stateSuccess)
                            .paquitoCard()
                    }
                }
                .padding(PaquitoSpacing.lg)
            }
            PaquitoPrimaryButton(title: "Sincronizar \(selected.count) tareas", isLoading: isSyncing, action: sync)
                .padding(.horizontal, PaquitoSpacing.lg)
                .padding(.vertical, 10)
                .disabled(!connected || selected.isEmpty)
        }
        .background(PaquitoColors.background)
        .navigationTitle("Google Calendar")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var accountCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label(connected ? "Google Calendar conectado" : "Conecta tu cuenta de Google", systemImage: connected ? "checkmark.circle.fill" : "calendar")
                .font(PaquitoTypography.taskTitle)
                .foregroundStyle(connected ? PaquitoColors.stateSuccess : PaquitoColors.textPrimary)
            Text("Se crearán o actualizarán las tareas elegidas en tu calendario principal.")
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.textSecondary)
            if connected {
                Button("Revocar acceso", role: .destructive) {
                    connected = false
                    summary = nil
                    message = "Acceso revocado. Los eventos ya creados se conservan en Google Calendar."
                }
                .font(PaquitoTypography.bodyMedium)
            } else {
                PaquitoPrimaryButton(title: "Conectar Google Calendar", action: {
                    connected = true
                    message = "Cuenta demo conectada. Elige las tareas que quieres enviar."
                })
            }
        }
        .paquitoCard()
    }

    private func taskRow(_ task: CalendarTask) -> some View {
        Button {
            if selected.contains(task.id) { selected.remove(task.id) }
            else { selected.insert(task.id) }
        } label: {
            HStack(spacing: 12) {
                Image(systemName: selected.contains(task.id) ? "checkmark.square.fill" : "square")
                    .foregroundStyle(selected.contains(task.id) ? PaquitoColors.stateInfoStrong : PaquitoColors.textMuted)
                    .font(.title3)
                VStack(alignment: .leading, spacing: 4) {
                    Text(task.title).font(PaquitoTypography.taskTitle).foregroundStyle(PaquitoColors.textPrimary)
                    Text(task.course).font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.stateInfoStrong)
                    Text(task.dueLabel).font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textSecondary)
                }
                Spacer()
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .paquitoCard()
        }
        .buttonStyle(.plain)
    }

    private func sync() {
        guard connected, !selected.isEmpty else { return }
        isSyncing = true
        summary = nil
        Task { @MainActor in
            try? await Task.sleep(for: .milliseconds(350))
            let count = selected.count
            summary = CalendarSyncSummary(created: count, updated: 0, unchanged: 0)
            message = "Sincronización completada. Puedes repetirla sin duplicar tareas."
            isSyncing = false
        }
    }
}
