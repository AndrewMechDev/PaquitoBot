import SwiftUI

struct CoursesView: View {
    let courses: [CourseItem]
    let onCourseSelected: (CourseItem) -> Void

    var body: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 14) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Cursos")
                        .font(PaquitoTypography.display)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text("Revisa tu progreso por materia")
                        .font(PaquitoTypography.body)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
                .padding(.bottom, 8)

                ForEach(courses) { course in
                    Button {
                        onCourseSelected(course)
                    } label: {
                        CourseCard(course: course)
                    }
                    .buttonStyle(.plain)
                    .accessibilityLabel("\(course.name), promedio \(course.projectedGrade)")
                    .accessibilityHint("Abre el detalle del curso")
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

private struct CourseCard: View {
    let course: CourseItem

    var body: some View {
        VStack(alignment: .leading, spacing: 15) {
            CourseCardHeader(code: course.code)
            CourseCardTitle(name: course.name)
            CourseMetrics(course: course)
            CourseDueLabel(label: course.nextDueLabel)
        }
        .paquitoCard()
    }
}

private struct CourseCardHeader: View {
    let code: String

    var body: some View {
        HStack {
            Text(code)
                .font(PaquitoTypography.caption)
                .fontWeight(.bold)
                .foregroundStyle(PaquitoColors.textOnPrimaryDim)
                .padding(.horizontal, 9)
                .padding(.vertical, 6)
                .background(PaquitoColors.brandPrimary.opacity(0.28))
                .clipShape(Capsule())
            Spacer()
            Image(systemName: "chevron.right")
                .font(.caption)
                .fontWeight(.semibold)
                .foregroundStyle(PaquitoColors.textMuted)
        }
    }
}

private struct CourseCardTitle: View {
    let name: String

    var body: some View {
        Text(name)
            .font(PaquitoTypography.headline.weight(.semibold))
            .foregroundStyle(PaquitoColors.textPrimary)
    }
}

private struct CourseMetrics: View {
    let course: CourseItem

    var body: some View {
        HStack(spacing: 18) {
            CourseMetric(title: "Promedio", value: course.projectedGrade, color: PaquitoColors.stateInfoStrong)
            CourseMetric(title: "Faltas", value: absenceValue, color: absenceColor)
            Spacer()
        }
    }

    private var absenceValue: String {
        "\(course.absences) / \(course.absenceLimit)"
    }

    private var absenceColor: Color {
        course.absences >= course.absenceLimit ? PaquitoColors.stateDanger : PaquitoColors.textPrimary
    }
}

private struct CourseMetric: View {
    let title: String
    let value: String
    let color: Color

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.textSecondary)
            Text(value)
                .font(PaquitoTypography.greeting.weight(.semibold))
                .foregroundStyle(color)
        }
    }
}

private struct CourseDueLabel: View {
    let label: String

    var body: some View {
        HStack(spacing: 6) {
            Image(systemName: "clock")
            Text(label)
        }
        .font(PaquitoTypography.bodySmall)
        .foregroundStyle(PaquitoColors.textSecondary)
    }
}
