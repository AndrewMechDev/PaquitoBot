import SwiftUI

struct HomeView: View {
    let userName: String
    let data: HomeData
    let unreadNotifications: Int
    let onNotifications: () -> Void
    let onSignOut: () -> Void

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 20) {
                header
                summary
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

    private var summary: some View {
        HStack(spacing: 10) {
            SummaryTile(title: "Promedio", value: data.average, tint: PaquitoColors.stateInfoStrong)
            SummaryTile(title: "Faltas", value: data.absences, tint: PaquitoColors.stateWarning)
            SummaryTile(title: "Entregas", value: data.dueThisWeek, tint: PaquitoColors.stateDanger)
        }
    }

    private var weekSection: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Esta semana")
                .font(PaquitoTypography.taskTitle)
                .foregroundStyle(PaquitoColors.textPrimary)

            HStack(spacing: 8) {
                ForEach(data.days) { day in
                    VStack(spacing: 5) {
                        Text(day.shortLabel)
                            .font(PaquitoTypography.dayLabel)
                            .foregroundStyle(day.isToday ? .white.opacity(0.9) : PaquitoColors.textOnDarkMuted)
                        Text(day.dateLabel)
                            .font(PaquitoTypography.dayNumber)
                            .foregroundStyle(day.isCritical ? PaquitoColors.stateDanger : (day.isToday ? .white : PaquitoColors.brandPrimary))
                        Circle()
                            .fill(day.hasTask ? (day.isCritical ? PaquitoColors.stateDanger : PaquitoColors.stateInfo) : .clear)
                            .frame(width: 5, height: 5)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 11)
                    .background(day.isToday ? Color.white.opacity(0.16) : .clear)
                    .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                    .accessibilityElement(children: .combine)
                    .accessibilityLabel("\(day.shortLabel) \(day.dateLabel)")
                    .accessibilityValue(day.isToday ? "Hoy" : (day.hasTask ? "Tiene entregas" : "Sin entregas"))
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
                Text("\(data.tasks.count)")
                    .font(PaquitoTypography.caption.weight(.bold))
                    .foregroundStyle(PaquitoColors.textOnPrimaryDim)
                    .padding(.horizontal, 9)
                    .padding(.vertical, 5)
                    .background(PaquitoColors.brandPrimary.opacity(0.22))
                    .clipShape(Capsule())
            }

            VStack(spacing: 0) {
                ForEach(data.tasks) { task in
                    TaskRow(task: task)
                }
            }
            .paquitoCard()
        }
    }
}

private struct SummaryTile: View {
    let title: String
    let value: String
    let tint: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.textSecondary)
            Text(value)
                .font(PaquitoTypography.greeting.weight(.semibold))
                .foregroundStyle(tint)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(14)
        .background(PaquitoColors.surfaceGlass)
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
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
