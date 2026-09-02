import SwiftUI

struct ScheduleView: View {
    @Binding var selectedTab: AppTab
    let viewModel: ScheduleViewModel
    let onChatbot: () -> Void
    let onTabSelected: (AppTab) -> Void

    var body: some View {
        AppTabScaffold(selectedTab: $selectedTab, pendingCount: viewModel.data.pendingCount, onChatbot: onChatbot, onTabSelected: onTabSelected) {
            if let errorMessage = viewModel.errorMessage {
                CanvasMockErrorBanner(message: errorMessage, onRetry: viewModel.load)
            }
            VStack(alignment: .leading, spacing: 4) {
                Text("Horarios").font(PaquitoTypography.displayLarge)
                Text(viewModel.data.weekLabel).font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textMuted)
            }
            if viewModel.data.days.isEmpty {
                Text("No hay eventos programados").font(PaquitoTypography.bodyMedium).foregroundStyle(PaquitoColors.textMuted).frame(maxWidth: .infinity, minHeight: 140)
            } else {
                VStack(alignment: .leading, spacing: 16) {
                    ForEach(viewModel.data.days) { day in
                        VStack(alignment: .leading, spacing: 8) {
                            Text(day.label).font(PaquitoTypography.taskTitle)
                            ForEach(day.events) { event in ScheduleEventRow(event: event) }
                        }
                    }
                }
            }
        }
    }
}

private struct ScheduleEventRow: View {
    let event: ScheduleEvent

    private var color: Color {
        switch event.kind {
        case .clase: return PaquitoColors.infoStrong
        case .entrega: return PaquitoColors.warning
        case .falta: return PaquitoColors.danger
        }
    }

    var body: some View {
        HStack(spacing: 12) {
            Text(event.time).font(.system(size: 13, weight: .semibold)).frame(width: 56, alignment: .leading)
            VStack(alignment: .leading, spacing: 3) {
                Text(event.title).font(PaquitoTypography.taskTitle).lineLimit(1)
                Text(event.subtitle).font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.textMuted).lineLimit(1)
            }
            Spacer()
            Text(event.kind.label)
                .font(.system(size: 9, weight: .bold))
                .foregroundStyle(color)
                .padding(.horizontal, 8)
                .padding(.vertical, 5)
                .background(color.opacity(0.12))
                .clipShape(Capsule())
        }
        .padding(14)
        .background(PaquitoColors.surfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
    }
}
