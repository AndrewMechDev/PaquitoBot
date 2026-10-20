import SwiftUI

@MainActor
struct CalendarSyncView: View {
    @StateObject private var viewModel: CalendarSyncViewModel
    @State private var showPast = false
    @State private var selected: Set<String>

    private let tasks: [CalendarTask]

    init(
        tasks: [CalendarTask] = AcademicDemoStore.calendarTasks,
        service: CalendarEventService? = nil
    ) {
        self.tasks = tasks
        _selected = State(initialValue: Set(tasks.filter { !$0.isPast }.map(\.id)))
        _viewModel = StateObject(
            wrappedValue: CalendarSyncViewModel(service: service ?? EventKitCalendarService())
        )
    }

    private var visibleTasks: [CalendarTask] {
        tasks.filter { showPast || !$0.isPast }
    }

    private var selectedTasks: [CalendarTask] {
        tasks.filter { selected.contains($0.id) }
    }

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(alignment: .leading, spacing: 14) {
                    accountCard

                    if viewModel.isWorking {
                        ProgressView("Actualizando calendario…")
                            .tint(PaquitoColors.stateInfoStrong)
                    }

                    Text("Se usará el calendario principal disponible en este iPhone. La sincronización es manual y no elimina eventos existentes.")
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textSecondary)

                    Toggle("Mostrar tareas vencidas", isOn: $showPast)
                        .font(PaquitoTypography.bodyMedium)
                        .tint(PaquitoColors.stateInfoStrong)
                        .disabled(viewModel.isWorking)

                    if showPast {
                        Text("Las vencidas se envían con su fecha original y solo si las seleccionas.")
                            .font(PaquitoTypography.caption)
                            .foregroundStyle(PaquitoColors.textSecondary)
                    }

                    Text("Elige tus entregas")
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)

                    if visibleTasks.isEmpty {
                        ContentUnavailableView(
                            "No hay entregas pendientes",
                            systemImage: "checkmark.circle",
                            description: Text("Activa las tareas vencidas para revisar el escenario demo.")
                        )
                    } else {
                        ForEach(visibleTasks) { task in
                            taskRow(task)
                        }
                    }

                    if let message = viewModel.message {
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

                    if let summary = viewModel.summary {
                        VStack(alignment: .leading, spacing: 5) {
                            Text("Sincronización completada")
                                .font(PaquitoTypography.taskTitle)
                                .foregroundStyle(PaquitoColors.stateSuccess)
                            Text("\(summary.created) creados · \(summary.updated) actualizados · \(summary.unchanged) sin cambios")
                                .font(PaquitoTypography.bodySmall)
                                .foregroundStyle(PaquitoColors.textSecondary)
                            Text("Los eventos existentes de PaquitoBot se actualizan por marcador y no se duplican.")
                                .font(PaquitoTypography.caption)
                                .foregroundStyle(PaquitoColors.textSecondary)
                        }
                        .paquitoCard()
                    }
                }
                .padding(PaquitoSpacing.lg)
            }

            PaquitoPrimaryButton(
                title: "Sincronizar \(selectedTasks.count) tareas",
                isLoading: viewModel.isWorking,
                action: { viewModel.sync(tasks: selectedTasks) }
            )
            .padding(.horizontal, PaquitoSpacing.lg)
            .padding(.vertical, 10)
            .disabled(!viewModel.isConnected || selectedTasks.isEmpty)
        }
        .background(PaquitoColors.background)
        .navigationTitle("Calendario")
        .navigationBarTitleDisplayMode(.inline)
    }

    private var accountCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Label(
                viewModel.isConnected ? "Calendario conectado" : "Conecta tu calendario",
                systemImage: viewModel.isConnected ? "checkmark.circle.fill" : "calendar"
            )
            .font(PaquitoTypography.taskTitle)
            .foregroundStyle(viewModel.isConnected ? PaquitoColors.stateSuccess : PaquitoColors.textPrimary)

            Text("PaquitoBot solicitará permiso para crear y actualizar las entregas elegidas en el calendario principal del dispositivo.")
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.textSecondary)

            if viewModel.isConnected {
                Button("Dejar de usar el calendario") {
                    viewModel.disconnect()
                }
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(PaquitoColors.stateDanger)
            } else {
                PaquitoPrimaryButton(title: "Conectar calendario", isLoading: viewModel.isWorking) {
                    viewModel.connect()
                }
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
                    Text(task.title)
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text(task.course)
                        .font(PaquitoTypography.caption)
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                    Text(task.dueLabel)
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textSecondary)
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
        .disabled(viewModel.isWorking)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(task.title), \(task.course), \(task.dueLabel)")
        .accessibilityValue(selected.contains(task.id) ? "Seleccionada" : "No seleccionada")
    }
}
