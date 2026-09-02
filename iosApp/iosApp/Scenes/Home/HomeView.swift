import Foundation
import SwiftUI

struct HomeView: View {
    @Binding var selectedTab: AppTab
    let viewModel: HomeViewModel
    let onChatbot: () -> Void
    let onNotifications: () -> Void
    let onTabSelected: (AppTab) -> Void

    @State private var selectedDayNumber = ""
    @State private var selectedCycleIndex = 0

    var body: some View {
        AppTabScaffold(
            selectedTab: $selectedTab,
            pendingCount: viewModel.data.pendingCount,
            onChatbot: onChatbot,
            onTabSelected: onTabSelected
        ) {
            if let errorMessage = viewModel.errorMessage {
                CanvasMockErrorBanner(message: errorMessage, onRetry: viewModel.load)
            }
            HStack(alignment: .top, spacing: 12) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(viewModel.data.greeting)
                        .font(PaquitoTypography.greeting)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text(viewModel.data.dateLabel)
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textMuted)
                }
                Spacer()
                NotificationBellButton(count: viewModel.data.unreadNotificationsCount, action: onNotifications)
            }

            HStack(spacing: 10) {
                snapshot(label: "Ranking", value: String(format: "%.1f", viewModel.data.overallRanking), hint: "de 20")
                let cycle = viewModel.data.cycleRankings[safe: selectedCycleIndex]
                snapshot(label: "Ranking ciclo", value: cycle.map { String(format: "%.1f", $0.ranking) } ?? "—", hint: cycle.map { "\($0.cycle)° ciclo · toca para cambiar" } ?? "sin ciclos previos", action: cycle == nil ? nil : cycleAction)
                snapshot(label: "Carrera", value: viewModel.data.careerCode, hint: "\(viewModel.data.currentCycle)° ciclo actual")
            }

            Text(viewModel.data.weekTitle)
                .font(PaquitoTypography.headlineSmall)
                .foregroundStyle(PaquitoColors.textPrimary)

            VStack(spacing: 10) {
                HStack(spacing: 4) {
                    ForEach(viewModel.data.days) { day in
                        Button {
                            selectedDayNumber = day.dayNumber
                        } label: {
                            VStack(spacing: 5) {
                                Text(String(day.dayOfWeek.prefix(2)))
                                    .font(PaquitoTypography.dayOfWeek)
                                Text(day.dayNumber)
                                    .font(PaquitoTypography.dayNumber)
                                Circle()
                                    .fill(day.isCritical ? PaquitoColors.danger : PaquitoColors.brandPrimary)
                                    .frame(width: 5, height: 5)
                                    .opacity(day.isCritical || viewModel.data.tasks.contains { $0.dayNumber == day.dayNumber } ? 1 : 0)
                            }
                            .frame(maxWidth: .infinity, minHeight: 64)
                            .foregroundStyle(selectedDayNumber == day.dayNumber ? PaquitoColors.textOnPrimary : PaquitoColors.textPrimary)
                            .background(selectedDayNumber == day.dayNumber ? PaquitoColors.infoStrong : .clear)
                            .clipShape(RoundedRectangle(cornerRadius: 18, style: .continuous))
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(12)
            .background(PaquitoColors.darkCard)
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.week, style: .continuous))

            let selectedDay = viewModel.data.days.first { $0.dayNumber == selectedDayNumber }
            HStack(alignment: .firstTextBaseline) {
                Text(viewModel.data.tasksHeader).font(PaquitoTypography.headlineSmall)
                if let selectedDay { Text("· \(selectedDay.dateLabel)").font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textMuted) }
            }
            taskList(for: selectedDayNumber)
        }
        .onAppear { setInitialSelection() }
        .onChange(of: viewModel.data) { _, _ in setInitialSelection() }
    }

    private func setInitialSelection() {
        if !viewModel.data.days.contains(where: { $0.dayNumber == selectedDayNumber }) {
            selectedDayNumber = viewModel.data.days.first(where: { $0.isToday })?.dayNumber ?? viewModel.data.days.first?.dayNumber ?? ""
        }
        selectedCycleIndex = min(max(selectedCycleIndex, 0), max(viewModel.data.cycleRankings.count - 1, 0))
    }

    @ViewBuilder
    private func snapshot(label: String, value: String, hint: String, action: (() -> Void)? = nil) -> some View {
        Group {
            if let action {
                Button(action: action) { snapshotContent(label: label, value: value, hint: hint) }
                    .buttonStyle(.plain)
            } else {
                snapshotContent(label: label, value: value, hint: hint)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    private func snapshotContent(label: String, value: String, hint: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(label).font(.system(size: 12, weight: .medium)).foregroundStyle(PaquitoColors.textSecondary).lineLimit(1)
            Text(value).font(.system(size: 21, weight: .semibold)).foregroundStyle(PaquitoColors.textPrimary).lineLimit(1)
            Text(hint).font(.system(size: 11)).foregroundStyle(PaquitoColors.textMuted).lineLimit(2)
        }
        .padding(.horizontal, 10)
        .padding(.vertical, 12)
        .background(PaquitoColors.surfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.large, style: .continuous))
    }

    private var cycleAction: (() -> Void)? {
        guard !viewModel.data.cycleRankings.isEmpty else { return nil }
        return {
            selectedCycleIndex = selectedCycleIndex == 0 ? viewModel.data.cycleRankings.count - 1 : selectedCycleIndex - 1
        }
    }

    @ViewBuilder
    private func taskList(for dayNumber: String) -> some View {
        let tasks = viewModel.data.tasks.filter { $0.dayNumber == dayNumber }
        VStack(spacing: 0) {
            if tasks.isEmpty {
                Text("No hay tareas pendientes")
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.textMuted)
                    .frame(maxWidth: .infinity, minHeight: 80)
            } else {
                ForEach(Array(tasks.enumerated()), id: \.element.id) { index, task in
                    HStack(spacing: 10) {
                        Image(systemName: task.systemImage)
                            .font(.system(size: 17, weight: .medium))
                            .frame(width: 20)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(task.label).font(.system(size: 11)).foregroundStyle(PaquitoColors.textMuted)
                            Text(task.title).font(PaquitoTypography.taskTitle).lineLimit(1)
                        }
                        Spacer()
                        Text(task.timestamp)
                            .font(PaquitoTypography.timestamp)
                            .foregroundStyle(task.urgency == .urgent ? PaquitoColors.danger : task.urgency == .future ? PaquitoColors.textMuted : PaquitoColors.infoStrong)
                    }
                    .padding(.horizontal, 14)
                    .padding(.vertical, 9)
                    if index < tasks.count - 1 { Divider().padding(.leading, 44) }
                }
            }
        }
        .background(PaquitoColors.surfaceElevated.opacity(0.6))
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.week, style: .continuous))
    }
}

private extension Collection {
    subscript(safe index: Index) -> Element? {
        indices.contains(index) ? self[index] : nil
    }
}
