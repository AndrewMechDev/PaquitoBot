import SwiftUI

struct ProceduresView: View {
    @State private var query = ""
    @State private var selectedCategory: String?

    private var categories: [String] {
        Array(Set(DemoAcademicData.procedures.map(\.category))).sorted()
    }

    private var filteredProcedures: [ProcedureItem] {
        let normalizedQuery = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        return DemoAcademicData.procedures.filter { procedure in
            let matchesCategory = selectedCategory == nil || procedure.category == selectedCategory
            let matchesQuery = normalizedQuery.isEmpty
                || procedure.title.lowercased().contains(normalizedQuery)
                || procedure.code.lowercased().contains(normalizedQuery)
                || procedure.summary.lowercased().contains(normalizedQuery)
            return matchesCategory && matchesQuery
        }
    }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Secretaría")
                        .font(PaquitoTypography.display)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text("Guía de trámites · Arequipa")
                        .font(PaquitoTypography.bodyMedium)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }

                TextField("Buscar trámite o código", text: $query)
                    .font(PaquitoTypography.bodyMedium)
                    .textFieldStyle(.plain)
                    .padding(.horizontal, 14)
                    .frame(minHeight: 48)
                    .background(PaquitoColors.surfaceGlass)
                    .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
                    .overlay(
                        RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous)
                            .stroke(PaquitoColors.borderDefault, lineWidth: 1)
                    )
                    .overlay(alignment: .trailing) {
                        if !query.isEmpty {
                            Button("Limpiar") { query = "" }
                                .font(PaquitoTypography.caption.weight(.semibold))
                                .foregroundStyle(PaquitoColors.stateInfoStrong)
                                .padding(.trailing, 12)
                        }
                    }

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ProcedureFilterChip(title: "Todos", isSelected: selectedCategory == nil) {
                            selectedCategory = nil
                        }
                        ForEach(categories, id: \.self) { category in
                            ProcedureFilterChip(title: category, isSelected: selectedCategory == category) {
                                selectedCategory = category
                            }
                        }
                    }
                }

                Text("\(filteredProcedures.count) trámites · información de referencia 2024")
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textMuted)

                ProcedureSourceNote()

                if filteredProcedures.isEmpty {
                    VStack(alignment: .leading, spacing: 8) {
                        Text("No encontramos ese trámite")
                            .font(PaquitoTypography.taskTitle)
                            .foregroundStyle(PaquitoColors.textPrimary)
                        Text("Prueba con otra palabra o consulta todas las categorías.")
                            .font(PaquitoTypography.bodyMedium)
                            .foregroundStyle(PaquitoColors.textSecondary)
                        Button("Ver todos") {
                            query = ""
                            selectedCategory = nil
                        }
                        .font(PaquitoTypography.button)
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .paquitoCard()
                } else {
                    LazyVStack(spacing: 12) {
                        ForEach(filteredProcedures) { procedure in
                            NavigationLink(value: AppRoute.procedureDetail(procedure.id)) {
                                ProcedureSummaryCard(procedure: procedure)
                            }
                            .buttonStyle(.plain)
                        }
                    }
                }
            }
            .padding(PaquitoSpacing.lg)
        }
        .scrollIndicators(.hidden)
        .background(PaquitoColors.background)
        .navigationTitle("Procedimientos")
        .navigationBarTitleDisplayMode(.inline)
    }
}

private struct ProcedureSummaryCard: View {
    let procedure: ProcedureItem

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack(alignment: .firstTextBaseline) {
                Text("\(procedure.code) · \(procedure.category)")
                    .font(PaquitoTypography.caption.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption.weight(.semibold))
                    .foregroundStyle(PaquitoColors.textMuted)
            }
            Text(procedure.title)
                .font(PaquitoTypography.taskTitle)
                .foregroundStyle(PaquitoColors.textPrimary)
            Text(procedure.summary)
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(PaquitoColors.textSecondary)
                .lineLimit(3)
            Text("\(procedure.cost) · referencial")
                .font(PaquitoTypography.bodySmall.weight(.semibold))
                .foregroundStyle(PaquitoColors.textPrimary)
            Text("Ver requisitos y pasos  ›")
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.stateInfoStrong)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .paquitoCard()
    }
}

private struct ProcedureFilterChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(PaquitoTypography.bodySmall.weight(.semibold))
                .foregroundStyle(isSelected ? PaquitoColors.stateInfoStrong : PaquitoColors.textSecondary)
                .padding(.horizontal, 14)
                .padding(.vertical, 9)
                .background(isSelected ? PaquitoColors.brandPrimary.opacity(0.14) : PaquitoColors.surfaceGlass)
                .clipShape(Capsule())
                .overlay(Capsule().stroke(isSelected ? PaquitoColors.stateInfoStrong : PaquitoColors.borderDefault, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

private struct ProcedureSourceNote: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 5) {
            Text("Antes de realizar tu trámite")
                .font(PaquitoTypography.bodyMedium.weight(.semibold))
                .foregroundStyle(PaquitoColors.stateInfoStrong)
            Text("Requisitos basados en el manual de referencia 2024. Confirma fechas, tarifas y condiciones actuales con Tecsup antes de solicitarlo.")
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.textSecondary)
        }
        .paquitoCard()
    }
}

struct ProcedureDetailView: View {
    let procedure: ProcedureItem
    @Environment(\.openURL) private var openURL

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text(procedure.code)
                    .font(PaquitoTypography.caption.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                Text(procedure.title)
                    .font(PaquitoTypography.display)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text(procedure.summary)
                    .font(PaquitoTypography.body)
                    .foregroundStyle(PaquitoColors.textSecondary)

                detailBlock(title: "Requisitos", items: procedure.requirements, icon: "checkmark.circle")
                detailBlock(title: "Pasos", items: procedure.steps, icon: "list.number")

                VStack(alignment: .leading, spacing: 8) {
                    Text("Información")
                        .font(PaquitoTypography.taskTitle)
                    infoRow("Costo", procedure.cost)
                    infoRow("Tiempo de respuesta", procedure.responseTime)
                    infoRow("Área responsable", procedure.responsibleArea)
                }
                .paquitoCard()

                VStack(alignment: .leading, spacing: 10) {
                    Text("Canales oficiales")
                        .font(PaquitoTypography.taskTitle)
                    ForEach(procedure.channels) { channel in
                        Button {
                            if let url = URL(string: channel.target) { openURL(url) }
                        } label: {
                            Label(channel.label, systemImage: channel.kind == .email ? "envelope" : "arrow.up.right.square")
                                .font(PaquitoTypography.bodyMedium.weight(.semibold))
                                .foregroundStyle(PaquitoColors.stateInfoStrong)
                        }
                    }
                }
                .paquitoCard()
            }
            .padding(PaquitoSpacing.lg)
        }
        .background(PaquitoColors.background)
        .navigationTitle("Detalle")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func detailBlock(title: String, items: [String], icon: String) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title).font(PaquitoTypography.taskTitle)
            ForEach(Array(items.enumerated()), id: \.offset) { _, item in
                Label(item, systemImage: icon)
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
        }
        .paquitoCard()
    }

    private func infoRow(_ label: String, _ value: String) -> some View {
        HStack(alignment: .top) {
            Text(label).foregroundStyle(PaquitoColors.textSecondary)
            Spacer()
            Text(value).multilineTextAlignment(.trailing).foregroundStyle(PaquitoColors.textPrimary)
        }
        .font(PaquitoTypography.bodySmall)
    }
}

struct EnrollmentGuideView: View {
    @Environment(\.openURL) private var openURL
    var onClose: (() -> Void)? = nil
    @State private var completed: Set<String> = []

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text("Guía de matrícula")
                    .font(PaquitoTypography.display)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text("Sigue estos pasos y verifica tu avance antes de iniciar el ciclo.")
                    .font(PaquitoTypography.body)
                    .foregroundStyle(PaquitoColors.textSecondary)
                ForEach(DemoAcademicData.enrollmentSteps) { step in
                    Button {
                        if completed.contains(step.id) { completed.remove(step.id) }
                        else { completed.insert(step.id) }
                    } label: {
                        HStack(alignment: .top, spacing: 14) {
                            Image(systemName: completed.contains(step.id) ? "checkmark.circle.fill" : "circle")
                                .foregroundStyle(completed.contains(step.id) ? PaquitoColors.stateSuccess : PaquitoColors.stateInfoStrong)
                                .font(.title3)
                            VStack(alignment: .leading, spacing: 8) {
                                Text(step.title).font(PaquitoTypography.taskTitle).foregroundStyle(PaquitoColors.textPrimary)
                                ForEach(step.body, id: \.self) { Text($0).font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textSecondary) }
                            }
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .paquitoCard()
                    }
                    .buttonStyle(.plain)
                }
                Button {
                    if let url = URL(string: "https://campus-cloud.tecsup.edu.pe/auth/login") { openURL(url) }
                } label: {
                    Label("Abrir Intranet Académica", systemImage: "arrow.up.right.square")
                        .font(PaquitoTypography.bodyMedium.weight(.semibold))
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                        .frame(maxWidth: .infinity, minHeight: 48)
                }
            }
            .padding(PaquitoSpacing.lg)
        }
        .background(PaquitoColors.background)
        .navigationTitle("Matrícula")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if let onClose {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Cerrar", action: onClose)
                }
            }
        }
    }
}
