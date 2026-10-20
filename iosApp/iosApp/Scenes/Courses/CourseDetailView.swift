import SwiftUI

struct CourseDetailView: View {
    let data: CourseDetailData
    @State private var expandedSections: Set<String> = ["Prácticas", "Laboratorios", "Foros"]
    @State private var attendanceExpanded = false
    @State private var selectedEvaluation: EvaluationItem?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                hero
                absenceStrip
                evaluationSection("Prácticas", items: data.practices)
                evaluationSection("Laboratorios", items: data.labs)
                evaluationSection("Pendientes", items: data.forums)
            }
            .padding(.horizontal, PaquitoSpacing.lg)
            .padding(.vertical, PaquitoSpacing.md)
        }
        .scrollIndicators(.hidden)
        .background(PaquitoColors.background)
        .navigationTitle(data.course.name)
        .navigationBarTitleDisplayMode(.inline)
        .sheet(item: $selectedEvaluation) { item in
            EvaluationDetailSheet(item: item)
        }
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
        VStack(alignment: .leading, spacing: 12) {
            Button {
                withAnimation(.easeInOut(duration: 0.2)) {
                    attendanceExpanded.toggle()
                }
            } label: {
                HStack(spacing: 12) {
                    Image(systemName: "person.crop.circle.badge.checkmark")
                        .font(.title3)
                        .foregroundStyle(absenceColor)
                    VStack(alignment: .leading, spacing: 3) {
                        Text("Asistencia")
                            .font(PaquitoTypography.taskTitle)
                            .foregroundStyle(PaquitoColors.textPrimary)
                        Text("\(data.course.absences) de \(data.course.absenceLimit) faltas")
                            .font(PaquitoTypography.bodySmall)
                            .foregroundStyle(absenceColor)
                    }
                    Spacer()
                    Image(systemName: attendanceExpanded ? "chevron.up" : "chevron.down")
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
            }
            .buttonStyle(.plain)

            GeometryReader { geometry in
                Capsule()
                    .fill(PaquitoColors.borderDefault)
                    .overlay(alignment: .leading) {
                        Capsule()
                            .fill(absenceColor)
                            .frame(width: geometry.size.width * absenceProgress)
                    }
            }
            .frame(height: 8)

            if attendanceExpanded {
                Text("Detalle de asistencia académica de demostración. Las sesiones reales se mostrarán cuando el adaptador iOS consuma el registro del campus.")
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
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
                    Text("\(items.count)")
                        .font(PaquitoTypography.caption)
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                    Image(systemName: expandedSections.contains(title) ? "chevron.up" : "chevron.down")
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
            }
            .buttonStyle(.plain)
            .accessibilityLabel("\(title), \(expandedSections.contains(title) ? "expandido" : "contraído")")

            if expandedSections.contains(title) {
                VStack(spacing: 0) {
                    ForEach(Array(items.enumerated()), id: \.element.id) { index, item in
                        Button {
                            selectedEvaluation = item
                        } label: {
                            EvaluationRow(item: item)
                        }
                        .buttonStyle(.plain)
                        if index < items.count - 1 {
                            Divider().overlay(PaquitoColors.borderDefault)
                        }
                    }
                }
                .paquitoCard()
            }
        }
    }

    private var absenceProgress: CGFloat {
        guard data.course.absenceLimit > 0 else { return 0 }
        return min(CGFloat(data.course.absences) / CGFloat(data.course.absenceLimit), 1)
    }

    private var absenceColor: Color {
        let remaining = data.course.absenceLimit - data.course.absences
        if remaining <= 1 { return PaquitoColors.stateDanger }
        if remaining <= 2 { return PaquitoColors.stateWarning }
        return PaquitoColors.stateSuccess
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

private struct EvaluationDetailSheet: View {
    let item: EvaluationItem
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 18) {
                DetailFact(label: "Estado", value: stateLabel(item.state))
                DetailFact(label: "Peso en la nota final", value: item.weightLabel)
                DetailFact(label: "Nota / puntaje", value: item.scoresLabel)
                if !item.detail.isEmpty {
                    Text(item.detail)
                        .font(PaquitoTypography.bodyMedium)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
                Spacer()
            }
            .padding(PaquitoSpacing.lg)
            .background(PaquitoColors.background)
            .navigationTitle(item.name)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Cerrar") { dismiss() }
                }
            }
        }
    }

    private func stateLabel(_ state: EvaluationState) -> String {
        switch state {
        case .graded: return "Calificado"
        case .pending: return "Pendiente"
        case .missing: return "No entregado"
        }
    }
}

private struct DetailFact: View {
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.textSecondary)
            Text(value)
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(PaquitoColors.textPrimary)
        }
    }
}
