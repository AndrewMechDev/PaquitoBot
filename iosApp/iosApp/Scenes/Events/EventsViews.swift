import SwiftUI

struct EventsView: View {
    var body: some View {
        List(DemoAcademicData.events) { event in
            NavigationLink(value: AppRoute.eventDetail(event.id)) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(event.title)
                        .font(PaquitoTypography.taskTitle)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Text(event.dateLabel)
                        .font(PaquitoTypography.bodySmall.weight(.semibold))
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                    Text("\(event.location) · \(event.sourceName)")
                        .font(PaquitoTypography.caption)
                        .foregroundStyle(PaquitoColors.textSecondary)
                }
                .padding(.vertical, 8)
            }
        }
        .scrollContentBackground(.hidden)
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
                Image(systemName: "sparkles.rectangle.stack")
                    .font(.system(size: 44))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                Text(event.title)
                    .font(PaquitoTypography.display)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Label(event.dateLabel, systemImage: "calendar")
                    .font(PaquitoTypography.bodyMedium.weight(.semibold))
                    .foregroundStyle(PaquitoColors.stateInfoStrong)
                Label(event.location, systemImage: "mappin.and.ellipse")
                    .font(PaquitoTypography.bodyMedium)
                    .foregroundStyle(PaquitoColors.textSecondary)
                Text(event.description)
                    .font(PaquitoTypography.body)
                    .foregroundStyle(PaquitoColors.textSecondary)
                    .fixedSize(horizontal: false, vertical: true)
                Text("Fuente: \(event.sourceName)")
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textMuted)
                if let sourceURL = event.sourceURL {
                    Button {
                        openURL(sourceURL)
                    } label: {
                        Label("Ver fuente del evento", systemImage: "arrow.up.right.square")
                            .font(PaquitoTypography.bodyMedium.weight(.semibold))
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
}
