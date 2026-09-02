import SwiftUI

struct CourseDetailView: View {
    let viewModel: CourseDetailViewModel
    let onBack: () -> Void

    @State private var attendanceExpanded = false
    @State private var expandedSection: String? = "Entregas"
    @State private var selectedEvaluation: EvaluationItem?

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                if let errorMessage = viewModel.errorMessage {
                    CanvasMockErrorBanner(message: errorMessage, onRetry: viewModel.load)
                }
                Text(viewModel.data.course.name).font(PaquitoTypography.displayLarge)
                Text(viewModel.data.headline).font(PaquitoTypography.headlineSmall).foregroundStyle(PaquitoColors.textSecondary)

                VStack(alignment: .leading, spacing: 4) {
                    Text(viewModel.data.course.projectedGrade).font(.system(size: 42, weight: .bold)).foregroundStyle(.white)
                    Text(viewModel.data.projectionHint).font(PaquitoTypography.bodySmall).foregroundStyle(.white.opacity(0.78))
                }
                .padding(22)
                .frame(maxWidth: .infinity, alignment: .leading)
                .background(PaquitoColors.darkCard)
                .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous))

                detailSection(title: "Asistencia", isExpanded: attendanceExpanded) {
                    VStack(alignment: .leading, spacing: 10) {
                        HStack(spacing: 8) {
                            attendanceDot(used: viewModel.data.course.absences, limit: viewModel.data.course.absenceLimit)
                            Text("\(viewModel.data.course.absences) de \(viewModel.data.course.absenceLimit) faltas").font(PaquitoTypography.bodyMedium)
                        }
                        if viewModel.data.attendance.isEmpty {
                            Text("Sin registros de asistencia").font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textMuted)
                        } else {
                            ForEach(viewModel.data.attendance) { entry in
                                HStack {
                                    Circle().fill(entry.isPresent ? PaquitoColors.success : PaquitoColors.danger).frame(width: 8, height: 8)
                                    Text(entry.dateLabel).font(PaquitoTypography.bodySmall)
                                    Spacer()
                                    Text(entry.isPresent ? "Presente" : "Falta").font(PaquitoTypography.caption).foregroundStyle(entry.isPresent ? PaquitoColors.success : PaquitoColors.danger)
                                }
                            }
                        }
                    }
                }

                if !viewModel.data.practices.isEmpty { evaluationSection(title: "Practicas", items: viewModel.data.practices) }
                if !viewModel.data.labs.isEmpty { evaluationSection(title: "Laboratorios", items: viewModel.data.labs) }
                if !viewModel.data.pending.isEmpty { evaluationSection(title: viewModel.data.pendingTitle, items: viewModel.data.pending) }
            }
            .padding(.horizontal, PaquitoSpacing.lg)
            .padding(.bottom, PaquitoSpacing.lg)
        }
        .scrollIndicators(.hidden)
        .background(PaquitoColors.background)
        .safeAreaPadding(.top)
        .toolbar(.hidden, for: .navigationBar)
        .overlay(alignment: .topLeading) { PaquitoBackButton(action: onBack).padding(.leading, 12) }
        .task { viewModel.load() }
        .sheet(item: $selectedEvaluation) { item in
            EvaluationDetailSheet(item: item).presentationDetents([.height(250), .medium])
        }
    }

    private func attendanceDot(used: Int, limit: Int) -> some View {
        HStack(spacing: 3) {
            ForEach(0..<max(limit, 1), id: \.self) { index in
                Circle().fill(index < used ? PaquitoColors.danger : PaquitoColors.success.opacity(0.35)).frame(width: 9, height: 9)
            }
        }
    }

    @ViewBuilder
    private func detailSection<Content: View>(title: String, isExpanded: Bool, @ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Button {
                withAnimation(.easeInOut(duration: 0.2)) { attendanceExpanded.toggle() }
            } label: { sectionHeader(title: title, expanded: isExpanded) }
                .buttonStyle(.plain)
            if isExpanded { content() }
        }
        .padding(16)
        .background(PaquitoColors.surfaceElevated.opacity(0.7))
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous))
    }

    @ViewBuilder
    private func evaluationSection(title: String, items: [EvaluationItem]) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            Button {
                withAnimation(.easeInOut(duration: 0.2)) { expandedSection = expandedSection == title ? nil : title }
            } label: { sectionHeader(title: title, expanded: expandedSection == title) }
                .buttonStyle(.plain)
            if expandedSection == title {
                ForEach(items) { item in
                    Button { selectedEvaluation = item } label: {
                        HStack {
                            VStack(alignment: .leading, spacing: 3) {
                                Text(item.name).font(PaquitoTypography.taskTitle).foregroundStyle(PaquitoColors.textPrimary)
                                Text(item.weightLabel).font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.textMuted)
                            }
                            Spacer()
                            VStack(alignment: .trailing, spacing: 3) {
                                Text(item.scoresLabel).font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textPrimary)
                                Text(item.state.label).font(PaquitoTypography.caption).foregroundStyle(item.state == .graded ? PaquitoColors.success : PaquitoColors.warning)
                            }
                            Image(systemName: "chevron.right").font(.caption).foregroundStyle(PaquitoColors.textMuted)
                        }
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        .padding(16)
        .background(PaquitoColors.surfaceElevated.opacity(0.7))
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.card, style: .continuous))
    }

    private func sectionHeader(title: String, expanded: Bool) -> some View {
        HStack {
            Text(title).font(PaquitoTypography.headlineSmall)
            Spacer()
            Image(systemName: expanded ? "chevron.up" : "chevron.down").font(.caption.weight(.bold))
        }
        .foregroundStyle(PaquitoColors.textPrimary)
    }
}

private struct EvaluationDetailSheet: View {
    let item: EvaluationItem

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(item.name).font(PaquitoTypography.displayLarge)
            Text(item.state.label).font(PaquitoTypography.bodyMedium).foregroundStyle(PaquitoColors.infoStrong)
            Text(item.detail).font(PaquitoTypography.bodyMedium).foregroundStyle(PaquitoColors.textSecondary)
            Spacer()
        }
        .padding(24)
    }
}
