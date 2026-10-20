import SwiftUI

struct CourseDetailView: View {
    let data: CourseDetailData
    @State private var expandedSections: Set<String> = ["Prácticas", "Laboratorios", "Foros"]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                hero
                absenceStrip
                evaluationSection("Prácticas", items: data.practices)
                evaluationSection("Laboratorios", items: data.labs)
                evaluationSection("Foros", items: data.forums)
            }
            .padding(.horizontal, PaquitoSpacing.lg)
            .padding(.vertical, PaquitoSpacing.md)
        }
        .scrollIndicators(.hidden)
        .background(PaquitoColors.background)
        .navigationTitle(data.course.name)
        .navigationBarTitleDisplayMode(.inline)
    }

    private var hero: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .lastTextBaseline) {
                Text(data.course.projectedGrade)
                    .font(.system(size: 44, weight: .bold, design: .rounded))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                Text("/ 20")
                    .font(PaquitoTypography.body)
                    .foregroundStyle(PaquitoColors.textSecondary)
                Spacer()
                Text(data.course.trackHint)
                    .font(PaquitoTypography.bodySmall.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
            }
            Text(data.headline)
                .font(PaquitoTypography.headline.weight(.semibold))
                .foregroundStyle(PaquitoColors.textPrimary)
            Text(data.projectionHint)
                .font(PaquitoTypography.body)
                .foregroundStyle(PaquitoColors.textSecondary)
        }
        .paquitoCard()
    }

    private var absenceStrip: some View {
        HStack(spacing: 12) {
            Image(systemName: "person.crop.circle.badge.checkmark")
                .font(.title3)
                .foregroundStyle(PaquitoColors.stateWarning)
            VStack(alignment: .leading, spacing: 3) {
                Text("Asistencia")
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text("\(data.course.absences) faltas de \(data.course.absenceLimit) permitidas")
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
            Spacer()
        }
        .paquitoCard()
    }

    private func evaluationSection(_ title: String, items: [EvaluationItem]) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Button {
                if expandedSections.contains(title) {
                    expandedSections.remove(title)
                } else {
                    expandedSections.insert(title)
                }
            } label: {
                HStack {
                    Text(title)
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Spacer()
                    Image(systemName: expandedSections.contains(title) ? "chevron.up" : "chevron.down")
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
            }
            .buttonStyle(.plain)
            .accessibilityLabel("\(title), \(expandedSections.contains(title) ? "expandido" : "contraído")")

            if expandedSections.contains(title) {
                VStack(spacing: 0) {
                    ForEach(Array(items.enumerated()), id: \.element.id) { index, item in
                        EvaluationRow(item: item)
                        if index < items.count - 1 {
                            Divider().overlay(PaquitoColors.borderDefault)
                        }
                    }
                }
                .paquitoCard()
            }
        }
    }
}

private struct EvaluationRow: View {
    let item: EvaluationItem

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: iconName)
                .foregroundStyle(color)
                .frame(width: 24)
            VStack(alignment: .leading, spacing: 3) {
                Text(item.name)
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text(item.weightLabel)
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
            Spacer()
            Text(item.scoresLabel)
                .font(PaquitoTypography.bodySmall.weight(.semibold))
                .foregroundStyle(color)
        }
        .padding(.vertical, 12)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(item.name), \(item.scoresLabel), \(item.weightLabel)")
    }

    private var iconName: String {
        switch item.state {
        case .graded: return "checkmark.circle.fill"
        case .pending: return "clock.fill"
        case .missing: return "exclamationmark.circle.fill"
        }
    }

    private var color: Color {
        switch item.state {
        case .graded: return PaquitoColors.stateSuccess
        case .pending: return PaquitoColors.stateWarning
        case .missing: return PaquitoColors.stateDanger
        }
    }
}
