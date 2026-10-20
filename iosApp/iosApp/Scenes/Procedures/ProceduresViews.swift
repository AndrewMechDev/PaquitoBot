import SwiftUI

struct ProceduresView: View {
    var body: some View {
        List {
            Section {
                Text("Consulta requisitos, pasos, costos y canales oficiales de atención.")
                    .font(PaquitoTypography.body)
                    .foregroundStyle(PaquitoColors.textSecondary)
                    .listRowBackground(Color.clear)
                    .listRowInsets(EdgeInsets(top: 12, leading: 0, bottom: 12, trailing: 0))
            }
            Section("Trámites disponibles") {
                ForEach(DemoAcademicData.procedures) { procedure in
                    NavigationLink(value: AppRoute.procedureDetail(procedure.id)) {
                        VStack(alignment: .leading, spacing: 5) {
                            Text(procedure.title)
                                .font(PaquitoTypography.taskTitle)
                                .foregroundStyle(PaquitoColors.textPrimary)
                            Text("\(procedure.code) · \(procedure.category)")
                                .font(PaquitoTypography.caption)
                                .foregroundStyle(PaquitoColors.stateInfoStrong)
                            Text(procedure.summary)
                                .font(PaquitoTypography.bodySmall)
                                .foregroundStyle(PaquitoColors.textSecondary)
                                .lineLimit(2)
                        }
                        .padding(.vertical, 8)
                    }
                }
            }
        }
        .scrollContentBackground(.hidden)
        .background(PaquitoColors.background)
        .navigationTitle("Procedimientos")
        .navigationBarTitleDisplayMode(.inline)
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
