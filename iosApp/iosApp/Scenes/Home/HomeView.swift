import SwiftUI

struct HomeView: View {
    let userName: String
    let data: HomeData
    let unreadNotifications: Int
    let onNotifications: () -> Void
    let onEvents: () -> Void
    let onSignOut: () -> Void

    @State private var selectedDayID: String?
    @State private var selectedCycleIndex: Int?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                header
                Text("Datos académicos de demostración")
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                snapshot
                highlights
                weekSection
                tasksSection
            }
            .padding(.horizontal, PaquitoSpacing.lg)
            .padding(.top, PaquitoSpacing.md)
            .padding(.bottom, 12)
        }
        .scrollIndicators(.hidden)
        .background(PaquitoColors.background)
    }

    private var header: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(alignment: .leading, spacing: 6) {
                Text("¡Bienvenido, \(userName)!")
                    .font(PaquitoTypography.greeting)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text(data.dateLabel)
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }

            Spacer(minLength: 8)

            HStack(spacing: 8) {
                NotificationBellButton(count: unreadNotifications, action: onNotifications)
                Menu {
                    Button("Cerrar sesión", role: .destructive, action: onSignOut)
                } label: {
                    Image(systemName: "ellipsis")
                        .font(.system(size: 18, weight: .semibold))
                        .foregroundStyle(PaquitoColors.textPrimary)
                        .frame(width: 44, height: 44)
                        .background(PaquitoColors.surfaceGlass)
                        .clipShape(Circle())
                }
                .accessibilityLabel("Más opciones")
            }
        }
    }

    private var snapshot: some View {
        HStack(spacing: 10) {
            HomeSnapshotTile(label: "Ranking", value: String(format: "%.1f", data.overallRanking), hint: "de 20")
            HomeSnapshotTile(
                label: "Ranking ciclo",
                value: selectedCycle.map { String(format: "%.1f", $0.ranking) } ?? "—",
                hint: selectedCycle.map { "\($0.cycle)° ciclo · toca para cambiar" } ?? "sin ciclos previos",
                action: data.cycleRankings.isEmpty ? nil : changeCycle
            )
            HomeSnapshotTile(label: "Carrera", value: data.careerCode, hint: "\(data.currentCycle)° ciclo actual")
        }
    }

    private var highlights: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 12) {
                HomeHighlightCard(
                    title: "Próximos eventos",
                    count: DemoAcademicData.events.count,
                    items: DemoAcademicData.events.prefix(2).map {
                        HomeFocusItem(id: "event-\($0.id)", title: $0.title, detail: $0.dateLabel)
                    },
                    accent: PaquitoColors.stateInfoStrong,
                    actionLabel: "Ver todos",
                    action: onEvents
                )
                HomeHighlightCard(
                    title: "Avisos importantes",
                    count: data.importantNoticeCount,
                    items: data.importantNotices,
                    accent: PaquitoColors.stateDanger
                )
            }
            .padding(.vertical, 1)
        }
    }

    private var weekSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(data.weekTitle)
                .font(PaquitoTypography.taskTitle)
                .foregroundStyle(PaquitoColors.textPrimary)

            HStack(spacing: 8) {
                ForEach(data.days) { day in
                    weekDayButton(day)
                }
            }
            .padding(12)
            .background(PaquitoColors.surfaceHomeWeek)
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous))
        }
    }

    private var tasksSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Tareas pendientes")
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Spacer()
                if selectedDayID != nil {
                    Button("Todas") { selectedDayID = nil }
                        .font(PaquitoTypography.caption.weight(.semibold))
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                }
                Text("\(visibleTasks.count)")
                    .font(PaquitoTypography.caption.weight(.bold))
                    .foregroundStyle(PaquitoColors.textOnPrimaryDim)
                    .padding(.horizontal, 9)
                    .padding(.vertical, 5)
                    .background(PaquitoColors.brandPrimary.opacity(0.22))
                    .clipShape(Capsule())
            }

            if visibleTasks.isEmpty {
                VStack(spacing: 6) {
                    Image(systemName: "checkmark.circle")
                        .font(.title2)
                        .foregroundStyle(PaquitoColors.stateSuccess)
                    Text("No hay tareas pendientes")
                        .font(PaquitoTypography.bodyMedium.weight(.semibold))
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text("Prueba con otro día o muestra todas las tareas.")
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 24)
                .paquitoCard()
            } else {
                VStack(spacing: 0) {
                    ForEach(visibleTasks) { task in
                        TaskRow(task: task)
                    }
                }
                .paquitoCard()
            }
        }
    }

    private var selectedCycle: CycleRanking? {
        guard !data.cycleRankings.isEmpty else { return nil }
        let index = min(selectedCycleIndex ?? data.cycleRankings.count - 1, data.cycleRankings.count - 1)
        return data.cycleRankings[index]
    }

    private var visibleTasks: [TaskItem] {
        guard let selectedDayID else { return data.tasks }
        return data.tasks.filter { $0.dayID == selectedDayID }
    }

    private func changeCycle() {
        guard !data.cycleRankings.isEmpty else { return }
        let current = selectedCycleIndex ?? data.cycleRankings.count - 1
        selectedCycleIndex = current == 0 ? data.cycleRankings.count - 1 : current - 1
    }

    private func weekDayButton(_ day: WeekDayItem) -> some View {
        let isSelected = selectedDayID == day.id
        return Button {
            selectedDayID = isSelected ? nil : day.id
        } label: {
            VStack(spacing: 5) {
                Text(day.shortLabel)
                    .font(PaquitoTypography.dayLabel)
                    .foregroundStyle(isSelected ? .white.opacity(0.9) : PaquitoColors.textOnDarkMuted)
                Text(day.dateLabel)
                    .font(PaquitoTypography.dayNumber)
                    .foregroundStyle(isSelected ? .white : (day.isCritical ? PaquitoColors.stateDanger : PaquitoColors.brandPrimary))
                Circle()
                    .fill(day.hasTask ? (day.isCritical ? PaquitoColors.stateDanger : PaquitoColors.stateInfo) : .clear)
                    .frame(width: 5, height: 5)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 11)
            .background(isSelected ? PaquitoColors.stateInfoStrong : (day.isToday ? Color.white.opacity(0.16) : .clear))
            .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
        }
        .buttonStyle(.plain)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(day.shortLabel) \(day.dateLabel)")
        .accessibilityValue(isSelected ? "Filtro seleccionado" : (day.isToday ? "Hoy" : (day.hasTask ? "Tiene entregas" : "Sin entregas")))
    }
}

private struct HomeSnapshotTile: View {
    let label: String
    let value: String
    let hint: String
    var action: (() -> Void)? = nil

    var body: some View {
        Group {
            if let action {
                Button(action: action) { content }
                    .buttonStyle(.plain)
            } else {
                content
            }
        }
    }

    private var content: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.textSecondary)
            Text(value)
                .font(PaquitoTypography.headline.weight(.semibold))
                .foregroundStyle(PaquitoColors.textPrimary)
            Text(hint)
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.textSecondary)
                .lineLimit(2)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .frame(minHeight: 95, alignment: .topLeading)
        .paquitoCard()
    }
}

private struct HomeHighlightCard: View {
    let title: String
    let count: Int
    let items: [HomeFocusItem]
    let accent: Color
    var actionLabel: String?
    var action: (() -> Void)?

    var body: some View {
        Group {
            if let action {
                Button(action: action) { content }
                    .buttonStyle(.plain)
            } else {
                content
            }
        }
    }

    private var content: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(title)
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                    .lineLimit(1)
                Spacer()
                Text(actionLabel ?? "\(count)")
                    .font(PaquitoTypography.caption.weight(.semibold))
                    .foregroundStyle(actionLabel == nil ? accent : PaquitoColors.stateInfoStrong)
            }
            Divider().overlay(PaquitoColors.borderDefault)
            if items.isEmpty {
                Text("Todo al día por ahora")
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textSecondary)
            } else {
                ForEach(items.prefix(2)) { item in
                    VStack(alignment: .leading, spacing: 2) {
                        Text(item.title)
                            .font(PaquitoTypography.bodyMedium)
                            .foregroundStyle(PaquitoColors.textPrimary)
                            .lineLimit(1)
                        Text(item.detail)
                            .font(PaquitoTypography.caption)
                            .foregroundStyle(PaquitoColors.textSecondary)
                            .lineLimit(1)
                    }
                }
            }
        }
        .frame(width: 252, height: 172, alignment: .topLeading)
        .paquitoCard()
    }
}

private struct TaskRow: View {
    let task: TaskItem

    var body: some View {
        HStack(spacing: 12) {
            Circle()
                .fill(color(for: task.urgency))
                .frame(width: 9, height: 9)
                .accessibilityHidden(true)
            VStack(alignment: .leading, spacing: 4) {
                Text(task.title)
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text(task.label)
                    .font(PaquitoTypography.taskSubtitle)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
            Spacer(minLength: 8)
            Text(task.timestamp)
                .font(PaquitoTypography.caption)
                .foregroundStyle(color(for: task.urgency))
                .multilineTextAlignment(.trailing)
        }
        .padding(.vertical, 13)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(task.title), \(task.label), \(task.timestamp)")
    }

    private func color(for urgency: TaskUrgency) -> Color {
        switch urgency {
        case .normal: return PaquitoColors.stateInfo
        case .urgent: return PaquitoColors.stateDanger
        case .future: return PaquitoColors.textMuted
        }
    }
}
