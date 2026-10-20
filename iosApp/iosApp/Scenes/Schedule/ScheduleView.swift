import SwiftUI

struct ScheduleView: View {
    let data: ScheduleData
    let onCalendar: () -> Void

    init(data: ScheduleData, onCalendar: @escaping () -> Void = {}) {
        self.data = data
        self.onCalendar = onCalendar
    }

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 16) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Horarios")
                        .font(PaquitoTypography.display)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text(data.weekLabel)
                        .font(PaquitoTypography.body)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
                .padding(.bottom, 8)

                Button(action: onCalendar) {
                    Label("Sincronizar calendario", systemImage: "calendar.badge.plus")
                        .font(PaquitoTypography.bodyMedium.weight(.semibold))
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .paquitoCard()
                }
                .buttonStyle(.plain)

                ForEach(data.days) { day in
                    VStack(alignment: .leading, spacing: 10) {
                        Text(day.label)
                            .font(PaquitoTypography.taskTitle)
                            .foregroundStyle(PaquitoColors.textPrimary)
                        ForEach(day.events) { event in
                            ScheduleEventRow(event: event)
                        }
                    }
                }
            }
            .padding(.horizontal, PaquitoSpacing.lg)
            .padding(.top, PaquitoSpacing.md)
            .padding(.bottom, 12)
        }
        .scrollIndicators(.hidden)
        .background(PaquitoColors.background)
    }
}

private struct ScheduleEventRow: View {
    let event: ScheduleEvent

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Text(event.time)
                .font(PaquitoTypography.caption.weight(.semibold))
                .foregroundStyle(PaquitoColors.textSecondary)
                .frame(width: 48, alignment: .leading)
            Rectangle()
                .fill(color)
                .frame(width: 3)
                .clipShape(Capsule())
            VStack(alignment: .leading, spacing: 5) {
                Text(event.title)
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text(event.subtitle)
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
            Spacer()
            Text(event.kind.label)
                .font(PaquitoTypography.caption.weight(.semibold))
                .foregroundStyle(color)
        }
        .padding(14)
        .background(PaquitoColors.surfaceGlass)
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(event.time), \(event.title), \(event.subtitle)")
    }

    private var color: Color {
        switch event.kind {
        case .classSession: return PaquitoColors.stateInfo
        case .assignment: return PaquitoColors.stateWarning
        case .absence: return PaquitoColors.stateDanger
        }
    }
}
