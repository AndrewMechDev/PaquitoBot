import SwiftUI

struct ProceduresView: View {
    @State private var query = ""
    @State private var selectedCategory: String?

    private var categories: [String] {
        Array(Set(AcademicDemoStore.procedures.map(\.category))).sorted()
    }

    private var filteredProcedures: [ProcedureItem] {
        let normalizedQuery = query.trimmingCharacters(in: .whitespacesAndNewlines).lowercased()
        return AcademicDemoStore.procedures.filter { procedure in
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

                ProcedureSourceNote()

                VStack(alignment: .leading, spacing: 14) {
                    ProcedureFact(label: "Dirigido a", value: procedure.audience)
                    ProcedureFact(label: "Costo referencial", value: procedure.cost)
                    ProcedureFact(label: "Tiempo de respuesta indicado", value: procedure.responseTime)
                    if let applicationWindow = procedure.applicationWindow {
                        ProcedureFact(label: "Cuándo solicitarlo", value: applicationWindow)
                    }
                    if let duration = procedure.duration {
                        ProcedureFact(label: "Duración del programa", value: duration)
                    }
                    ProcedureFact(label: "Área responsable", value: procedure.responsibleArea)
                }
                .paquitoCard()

                detailBlock(title: "Requisitos", items: procedure.requirements, icon: "checkmark.circle")
                detailBlock(title: "Cómo realizarlo", items: procedure.steps, icon: "list.number", numbered: true)

                if !procedure.notes.isEmpty {
                    detailBlock(title: "Ten en cuenta", items: procedure.notes, icon: "info.circle")
                }

                VStack(alignment: .leading, spacing: 10) {
                    Text("Canales de atención")
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text("Los enlaces abren servicios externos. Si eliges correo, revisa el destinatario y el asunto antes de enviar.")
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textSecondary)
                    ForEach(procedure.channels) { channel in
                        ProcedureChannelActionView(channel: channel, subject: "Consulta: \(procedure.title)")
                    }
                }
                .paquitoCard()

                VStack(alignment: .leading, spacing: 8) {
                    Text("Fuente y vigencia")
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text(sourceDescription)
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textSecondary)
                    if let sourceURL = procedure.source.url {
                        Button {
                            openURL(sourceURL)
                        } label: {
                            Label("Consultar manual oficial", systemImage: "arrow.up.right.square")
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

    private var sourceDescription: String {
        let page = procedure.sourcePage.map { " · página \($0)" } ?? ""
        return "\(procedure.source.title) · \(procedure.source.edition)\(page). Contenido revisado el \(procedure.source.reviewedOn); confirma la vigencia de tarifas y contactos con Tecsup."
    }

    private func detailBlock(title: String, items: [String], icon: String, numbered: Bool = false) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            Text(title)
                .font(PaquitoTypography.taskTitle)
                .foregroundStyle(PaquitoColors.textPrimary)
            ForEach(Array(items.enumerated()), id: \.offset) { index, item in
                Label {
                    Text(numbered ? "\(index + 1). \(item)" : item)
                } icon: {
                    Image(systemName: numbered ? "circle.fill" : icon)
                        .font(numbered ? .system(size: 5) : .body)
                }
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
        }
        .paquitoCard()
    }
}

private struct ProcedureFact: View {
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

private struct ProcedureChannelActionView: View {
    let channel: ProcedureChannel
    let subject: String
    @Environment(\.openURL) private var openURL

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Button {
                if let url = destinationURL {
                    openURL(url)
                }
            } label: {
                Label(channel.label, systemImage: channel.kind == .email ? "envelope" : "arrow.up.right.square")
                    .font(PaquitoTypography.bodyMedium.weight(.semibold))
                    .foregroundStyle(PaquitoColors.textOnPrimary)
                    .frame(maxWidth: .infinity, minHeight: 46)
                    .background(PaquitoColors.brandPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
            }
            .buttonStyle(.plain)
            Text(channel.target)
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.textSecondary)
                .textSelection(.enabled)
        }
    }

    private var destinationURL: URL? {
        switch channel.kind {
        case .web:
            return URL(string: channel.target)
        case .email:
            let encodedSubject = subject.addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? subject
            return URL(string: "mailto:\(channel.target)?subject=\(encodedSubject)")
        }
    }
}

struct EnrollmentGuideView: View {
    @Environment(\.openURL) private var openURL
    var onClose: (() -> Void)? = nil
    @State private var stage = -1
    @State private var calendarOpen = false

    private var steps: [EnrollmentStep] { DemoAcademicData.enrollmentSteps }
    private var currentStage: Int { stage.clamped(to: -1...steps.count) }
    private var progress: Double { Double(currentStage + 1) / Double(steps.count + 1) }

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Matrícula")
                        .font(PaquitoTypography.display)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text("Tu recorrido · Arequipa")
                        .font(PaquitoTypography.bodyMedium)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }

                ProgressView(value: progress)
                    .tint(PaquitoColors.stateInfoStrong)
                    .scaleEffect(y: 1.4, anchor: .center)

                Text(stageLabel)
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textSecondary)

                Group {
                    if currentStage < 0 {
                        enrollmentStart
                    } else if currentStage == steps.count {
                        enrollmentComplete
                    } else {
                        enrollmentStep(steps[currentStage])
                    }
                }

                Button {
                    withAnimation(.easeInOut(duration: 0.2)) {
                        calendarOpen.toggle()
                    }
                } label: {
                    HStack {
                        Text(calendarOpen ? "Ocultar fechas y plazos" : "Consultar fechas y plazos")
                        Spacer()
                        Image(systemName: calendarOpen ? "chevron.up" : "chevron.down")
                    }
                    .font(PaquitoTypography.bodyMedium.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                }
                .buttonStyle(.plain)

                if calendarOpen {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("Cronograma por confirmar")
                            .font(PaquitoTypography.taskTitle)
                            .foregroundStyle(PaquitoColors.textPrimary)
                        EnrollmentFact(label: "Matrícula regular", value: "Solicita el cronograma de tu sede y período")
                        EnrollmentFact(label: "Matrícula extraordinaria", value: "Consulta disponibilidad con Servicios Educativos")
                        EnrollmentFact(label: "Inicio de clases", value: "Fecha sujeta al período académico")
                        Text("Las fechas dependen de tu sede y período. Confírmalas en el portal oficial antes de realizar pagos.")
                            .font(PaquitoTypography.bodySmall)
                            .foregroundStyle(PaquitoColors.textSecondary)
                    }
                    .paquitoCard()
                }

                HStack(spacing: 12) {
                    if currentStage >= 0 {
                        Button("Anterior") {
                            withAnimation(.easeInOut(duration: 0.2)) { stage = currentStage - 1 }
                        }
                        .font(PaquitoTypography.bodyMedium.weight(.semibold))
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                        .frame(maxWidth: .infinity, minHeight: 48)
                        .overlay(
                            RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous)
                                .stroke(PaquitoColors.stateInfoStrong, lineWidth: 1)
                        )
                    }
                    Button(nextButtonTitle) {
                        withAnimation(.easeInOut(duration: 0.2)) {
                            stage = currentStage == steps.count ? -1 : currentStage + 1
                        }
                    }
                    .font(PaquitoTypography.button)
                    .foregroundStyle(PaquitoColors.textOnPrimary)
                    .frame(maxWidth: .infinity, minHeight: 48)
                    .background(PaquitoColors.brandPrimary)
                    .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
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

    private var stageLabel: String {
        switch currentStage {
        case -1: return "Antes de empezar"
        case steps.count: return "Recorrido revisado"
        default: return "Paso \(currentStage + 1) de \(steps.count)"
        }
    }

    private var nextButtonTitle: String {
        switch currentStage {
        case -1: return "Ver pasos"
        case steps.count: return "Revisar de nuevo"
        case steps.count - 1: return "Finalizar lectura"
        default: return "Siguiente"
        }
    }

    private var enrollmentStart: some View {
        VStack(alignment: .leading, spacing: 14) {
            Text("Prepara tu matrícula")
                .font(PaquitoTypography.headline)
                .foregroundStyle(PaquitoColors.textPrimary)
            ProcedureSourceNote()
            Text("Comprueba antes de empezar")
                .font(PaquitoTypography.taskTitle)
                .foregroundStyle(PaquitoColors.textPrimary)
            ForEach(steps.prefix(2)) { step in
                Label(step.title, systemImage: "checkmark.circle")
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
            Text("Puedes consultar esta guía sin iniciar sesión. No registra pagos ni confirma matrícula.")
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(PaquitoColors.textSecondary)
        }
        .paquitoCard()
    }

    private func enrollmentStep(_ step: EnrollmentStep) -> some View {
        VStack(alignment: .leading, spacing: 14) {
            Text(step.title)
                .font(PaquitoTypography.headline)
                .foregroundStyle(PaquitoColors.textPrimary)
            VStack(alignment: .leading, spacing: 10) {
                ForEach(Array(step.body.enumerated()), id: \.offset) { index, body in
                    Label {
                        Text("\(index + 1). \(body)")
                    } icon: {
                        Image(systemName: "circle.fill")
                            .font(.system(size: 5))
                    }
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textSecondary)
                }
            }
            .paquitoCard()
            if currentStage == steps.count - 1 {
                Button {
                    if let url = URL(string: "https://campus-cloud.tecsup.edu.pe/auth/login") { openURL(url) }
                } label: {
                    Label("Abrir Intranet Académica", systemImage: "arrow.up.right.square")
                        .font(PaquitoTypography.bodyMedium.weight(.semibold))
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                }
            }
            Text("Confirma las condiciones actuales con Tecsup antes de realizar cualquier pago o solicitud.")
                .font(PaquitoTypography.bodySmall)
                .foregroundStyle(PaquitoColors.textSecondary)
        }
    }

    private var enrollmentComplete: some View {
        VStack(alignment: .leading, spacing: 12) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 40))
                .foregroundStyle(PaquitoColors.stateSuccess)
            Text("Ya revisaste los pasos")
                .font(PaquitoTypography.headline)
                .foregroundStyle(PaquitoColors.textPrimary)
            Text("Tu matrícula se confirma únicamente en el portal de Tecsup. Llegar aquí no acredita un pago ni una inscripción.")
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(PaquitoColors.textSecondary)
            Button {
                if let url = URL(string: "https://campus-cloud.tecsup.edu.pe/auth/login") { openURL(url) }
            } label: {
                Label("Consultar matrícula", systemImage: "arrow.up.right.square")
                    .font(PaquitoTypography.bodyMedium.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
            }
        }
        .paquitoCard()
    }
}

private struct EnrollmentFact: View {
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

private extension Int {
    func clamped(to range: ClosedRange<Int>) -> Int {
        Swift.min(Swift.max(self, range.lowerBound), range.upperBound)
    }
}
