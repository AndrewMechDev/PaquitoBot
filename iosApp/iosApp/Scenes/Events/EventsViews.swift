import SwiftUI

struct EventsView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                VStack(alignment: .leading, spacing: 6) {
                    Text("Eventos")
                        .font(PaquitoTypography.display)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text("Tecnología, investigación e innovación")
                        .font(PaquitoTypography.bodyMedium)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
                Text("Una selección para explorar, aprender y conectar con otras comunidades.")
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textSecondary)
                Text("Eventos de muestra · información recopilada el 17 oct 2026")
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textMuted)
                LazyVStack(spacing: 14) {
                    ForEach(DemoAcademicData.events) { event in
                        NavigationLink(value: AppRoute.eventDetail(event.id)) {
                            EventSummaryCard(event: event)
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(PaquitoSpacing.lg)
        }
        .background(PaquitoColors.background)
        .navigationTitle("Eventos")
        .navigationBarTitleDisplayMode(.inline)
    }
}

struct EventDetailView: View {
    let event: CampusEvent
    @Environment(\.openURL) private var openURL

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Text(event.category.uppercased())
                    .font(PaquitoTypography.caption.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                Text(event.title)
                    .font(PaquitoTypography.display)
                    .foregroundStyle(PaquitoColors.textPrimary)
                VStack(alignment: .leading, spacing: 12) {
                    EventFact(label: "FECHA", value: event.dateRangeLabel)
                    Divider().overlay(PaquitoColors.borderDefault)
                    EventFact(label: "LUGAR", value: event.location)
                }
                .paquitoCard()
                eventTextSection(title: "¿De qué trata?", body: event.description)
                eventTextSection(title: "¿Por qué podría interesarte?", body: event.whyWorthIt)
                VStack(alignment: .leading, spacing: 10) {
                    Text("Inscripción")
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text(event.registrationNote)
                        .font(PaquitoTypography.bodyMedium)
                        .foregroundStyle(PaquitoColors.textSecondary)
                    if let actionURL = event.actionURL {
                        Button {
                            openURL(actionURL)
                        } label: {
                            Text(event.actionLabel)
                                .font(PaquitoTypography.button)
                                .foregroundStyle(PaquitoColors.textOnPrimary)
                                .frame(maxWidth: .infinity, minHeight: 48)
                                .background(PaquitoColors.brandPrimary)
                                .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
                        }
                    }
                }
                .paquitoCard()
                if let sourceURL = event.sourceURL {
                    Button {
                        openURL(sourceURL)
                    } label: {
                        Text("Fuente: \(event.sourceName)")
                            .font(PaquitoTypography.caption)
                            .foregroundStyle(PaquitoColors.stateInfoStrong)
                    }
                }
            }
            .padding(PaquitoSpacing.lg)
        }
        .background(PaquitoColors.background)
        .navigationTitle("Evento")
        .navigationBarTitleDisplayMode(.inline)
    }

    private func eventTextSection(title: String, body: String) -> some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(title)
                .font(PaquitoTypography.taskTitle)
                .foregroundStyle(PaquitoColors.textPrimary)
            Text(body)
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(PaquitoColors.textSecondary)
                .fixedSize(horizontal: false, vertical: true)
        }
    }
}

private struct EventSummaryCard: View {
    let event: CampusEvent

    var body: some View {
        HStack(alignment: .top, spacing: 14) {
            VStack(spacing: 2) {
                Text(event.dateLabel.split(separator: " ").first.map(String.init)?.uppercased() ?? "")
                    .font(PaquitoTypography.bodyMedium.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                Text(event.dateLabel.split(separator: " ").dropFirst().joined(separator: " ").uppercased())
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textSecondary)
                    .multilineTextAlignment(.center)
            }
            .frame(width: 58)
            .padding(.horizontal, 8)
            .padding(.vertical, 12)
            .background(PaquitoColors.brandPrimary.opacity(0.10))
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))

            VStack(alignment: .leading, spacing: 7) {
                Text(event.category.uppercased())
                    .font(PaquitoTypography.caption.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                    .lineLimit(1)
                Text(event.title)
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text(event.location)
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textSecondary)
                    .lineLimit(2)
                Text(event.summary)
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textPrimary)
                    .lineLimit(3)
                Text("Ver detalles  ›")
                    .font(PaquitoTypography.button)
                    .foregroundStyle(PaquitoColors.textLink)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .paquitoCard()
    }
}

private struct EventFact: View {
    let label: String
    let value: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(PaquitoTypography.caption.weight(.semibold))
                .foregroundStyle(PaquitoColors.textSecondary)
            Text(value)
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(PaquitoColors.textPrimary)
        }
    }
}
