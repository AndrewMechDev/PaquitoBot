import SwiftUI

struct CalendarSyncView: View {
    @State private var connected = false
    @State private var showPast = false
    @State private var selected: Set<String> = Set(AcademicDemoStore.calendarTasks.filter { !$0.isPast }.map(\.id))
    @State private var isSyncing = false
    @State private var summary: CalendarSyncSummary?
    @State private var message: String?

    private var visibleTasks: [CalendarTask] {
        AcademicDemoStore.calendarTasks.filter { showPast || !$0.isPast }
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    accountCard
                    if isSyncing {
                        ProgressView()
                            .tint(PaquitoColors.stateInfoStrong)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    Text("Datos académicos de demostración · horario de Perú. La sincronización es manual y no elimina eventos existentes.")
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textSecondary)
                    Button("Actualizar tareas") {
                        message = "Tareas demo actualizadas. Revisa las entregas antes de sincronizar."
                    }
                    .font(PaquitoTypography.bodyMedium.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                    .disabled(isSyncing)
                    Toggle("Mostrar tareas vencidas", isOn: $showPast)
                        .font(PaquitoTypography.bodyMedium)
                        .tint(PaquitoColors.stateInfoStrong)
                        .disabled(isSyncing)
                    if showPast {
                        Text("Las vencidas se envían con su fecha original y solo si las seleccionas.")
                            .font(PaquitoTypography.caption)
                            .foregroundStyle(PaquitoColors.textSecondary)
                    }
                    Text("Elige tus entregas")
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    if visibleTasks.isEmpty {
                        Text("No hay tareas futuras pendientes. Activa las vencidas para consultar los datos de demostración.")
                            .font(PaquitoTypography.bodyMedium)
                            .foregroundStyle(PaquitoColors.textSecondary)
                            .paquitoCard()
                    } else {
                        ForEach(visibleTasks) { task in
                            taskRow(task)
                        }
                    }
                    if let message {
                        VStack(alignment: .leading, spacing: 5) {
                            Text("Estado")
                                .font(PaquitoTypography.caption.weight(.semibold))
                                .foregroundStyle(PaquitoColors.stateInfoStrong)
                            Text(message)
                                .font(PaquitoTypography.bodySmall)
                                .foregroundStyle(PaquitoColors.textPrimary)
                        }
                        .paquitoCard()
                    }
                    if let summary {
                        VStack(alignment: .leading, spacing: 5) {
                            Text("Sincronización completada")
                                .font(PaquitoTypography.taskTitle)
                                .foregroundStyle(PaquitoColors.stateSuccess)
                            Text("\(summary.created) creados · \(summary.updated) actualizados · \(summary.unchanged) sin cambios")
                                .font(PaquitoTypography.bodySmall)
                                .foregroundStyle(PaquitoColors.textSecondary)
                            Text("Puedes reintentar sin duplicar las tareas ya enviadas.")
                                .font(PaquitoTypography.caption)
                                .foregroundStyle(PaquitoColors.textSecondary)
                        }
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
            if connected {
                Text("estudiante.demo@tecsup.edu.pe")
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
            }
            Text("Se crearán o actualizarán las tareas elegidas en tu calendario principal.")
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.textSecondary)
            if connected {
                Button("Revocar acceso de Google", role: .destructive) {
                    connected = false
                    summary = nil
                    message = "Acceso revocado. Los eventos ya creados se conservan en Google Calendar."
                }
                .font(PaquitoTypography.bodyMedium)
                Button("Renovar autorización") {
                    message = "Autorización demo renovada. Revisa las tareas antes de sincronizar."
                }
                .font(PaquitoTypography.bodyMedium.weight(.semibold))
                .foregroundStyle(PaquitoColors.stateInfoStrong)
                Text("Revocar el acceso conserva los eventos que ya se crearon. Google solicitará autorización nuevamente al conectar.")
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textSecondary)
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
                    if task.isPast {
                        Text("Vencida · se conservará esta fecha")
                            .font(PaquitoTypography.caption)
                            .foregroundStyle(PaquitoColors.textSecondary)
                    }
                }
                Spacer()
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .paquitoCard()
        }
        .buttonStyle(.plain)
        .disabled(isSyncing)
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
