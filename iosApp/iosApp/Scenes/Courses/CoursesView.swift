import SwiftUI

struct CoursesView: View {
    @Binding var selectedTab: AppTab
    let viewModel: CoursesViewModel
    let onChatbot: () -> Void
    let onCourseSelected: (CourseCardData) -> Void
    let onTabSelected: (AppTab) -> Void

    var body: some View {
        AppTabScaffold(selectedTab: $selectedTab, pendingCount: viewModel.data.pendingCount, onChatbot: onChatbot, onTabSelected: onTabSelected) {
            if let errorMessage = viewModel.errorMessage {
                CanvasMockErrorBanner(message: errorMessage, onRetry: viewModel.load)
            }
            VStack(alignment: .leading, spacing: 4) {
                Text("Cursos").font(PaquitoTypography.displayLarge)
                Text(viewModel.data.termLabel).font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textMuted)
            }
            VStack(spacing: 12) {
                ForEach(viewModel.data.courses) { course in
                    Button { onCourseSelected(course) } label: {
                        CourseSummaryCard(course: course)
                    }
                    .buttonStyle(.plain)
                }
            }
            if viewModel.isLoading {
                ProgressView().tint(PaquitoColors.infoStrong).frame(maxWidth: .infinity).padding()
            }
        }
    }
}

private struct CourseSummaryCard: View {
    let course: CourseCardData

    private var absenceColor: Color {
        let ratio = Double(course.absences) / Double(max(course.absenceLimit, 1))
        if ratio >= 0.75 { return PaquitoColors.danger }
        if ratio >= 0.5 { return PaquitoColors.warning }
        return PaquitoColors.success
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(course.code).font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.textMuted)
                    Text(course.name).font(PaquitoTypography.headlineSmall).foregroundStyle(PaquitoColors.textPrimary)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 2) {
                    Text(course.projectedGrade).font(.system(size: 28, weight: .bold)).foregroundStyle(PaquitoColors.infoStrong)
                    Text("promedio").font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.textMuted)
                }
            }
            HStack(alignment: .bottom) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(course.trackHint).font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textSecondary).lineLimit(2)
                    Text(course.nextDueLabel).font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.textMuted).lineLimit(1)
                }
                Spacer()
                Text("\(course.absences)/\(course.absenceLimit) faltas")
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(absenceColor)
            }
        }
        .padding(18)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(PaquitoColors.surfaceElevated)
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous))
    }
}
