import SwiftUI

struct NotificationsInboxView: View {
    @State private var items: [NotificationItem]
    @State private var isCompact = true
    @State private var selectedItem: NotificationItem?
    @State private var showDeleteAllConfirmation = false
    @State private var showPreferences = false
    let setup: NotificationSetup
    let onSaveSetup: (NotificationSetup) -> Void

    init(
        items: [NotificationItem],
        setup: NotificationSetup = .default,
        onSaveSetup: @escaping (NotificationSetup) -> Void = { _ in }
    ) {
        _items = State(initialValue: items)
        self.setup = setup
        self.onSaveSetup = onSaveSetup
    }

    var body: some View {
        VStack(spacing: 0) {
            Picker("Vista", selection: $isCompact) {
                Text("Cuadros").tag(true)
                Text("Lista").tag(false)
            }
            .pickerStyle(.segmented)
            .padding(.horizontal, PaquitoSpacing.lg)
            .padding(.top, 8)

            if items.isEmpty {
                emptyState
            } else {
                List {
                    ForEach(items) { item in
                        Button {
                            selectedItem = item
                        } label: {
                            if isCompact {
                                NotificationCard(item: item)
                            } else {
                                NotificationRow(item: item)
                            }
                        }
                        .buttonStyle(.plain)
                        .listRowInsets(EdgeInsets(top: 6, leading: 16, bottom: 6, trailing: 16))
                        .listRowSeparator(.hidden)
                        .listRowBackground(Color.clear)
                        .swipeActions(edge: .trailing, allowsFullSwipe: true) {
                            Button(role: .destructive) {
                                delete(item)
                            } label: {
                                Label("Eliminar", systemImage: "trash")
                            }
                        }
                    }
                }
                .listStyle(.plain)
                .scrollContentBackground(.hidden)
            }
        }
        .background(PaquitoColors.background)
        .navigationTitle("Notificaciones")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button("Preferencias") {
                    showPreferences = true
                }
                .font(PaquitoTypography.caption)
            }
            if !items.isEmpty {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Eliminar todo", role: .destructive) {
                        showDeleteAllConfirmation = true
                    }
                    .font(PaquitoTypography.caption)
                }
            }
        }
        .sheet(item: $selectedItem) { item in
            NotificationDetailView(item: item)
                .presentationDetents([.medium])
        }
        .sheet(isPresented: $showPreferences) {
            OnboardingNotificationsView(
                isRequestingPermission: false,
                initialSetup: setup,
                onContinue: { updatedSetup in
                    onSaveSetup(updatedSetup)
                    showPreferences = false
                },
                onCancel: {
                    showPreferences = false
                }
            )
        }
        .confirmationDialog(
            "¿Eliminar todas las notificaciones?",
            isPresented: $showDeleteAllConfirmation,
            titleVisibility: .visible
        ) {
            Button("Eliminar todo", role: .destructive) {
                withAnimation { items.removeAll() }
            }
            Button("Cancelar", role: .cancel) {}
        }
    }

    private var emptyState: some View {
        ContentUnavailableView(
            "Sin notificaciones",
            systemImage: "bell.slash",
            description: Text("Cuando Paquito tenga algo importante que avisarte, aparecerá aquí.")
        )
    }

    private func delete(_ item: NotificationItem) {
        withAnimation {
            items.removeAll { $0.id == item.id }
        }
    }
}

private struct NotificationCard: View {
    let item: NotificationItem

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack {
                Label(item.title, systemImage: item.severity == .urgent ? "exclamationmark.circle.fill" : "info.circle.fill")
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(color)
                Spacer()
                Text(item.timestamp)
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textMuted)
            }
            Text(item.body)
                .font(PaquitoTypography.bodyMedium)
                .foregroundStyle(PaquitoColors.textSecondary)
                .multilineTextAlignment(.leading)
        }
        .paquitoCard()
        .accessibilityElement(children: .combine)
    }

    private var color: Color {
        item.severity == .urgent ? PaquitoColors.stateDanger : PaquitoColors.stateInfoStrong
    }
}

private struct NotificationRow: View {
    let item: NotificationItem

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            Image(systemName: item.severity == .urgent ? "exclamationmark.circle.fill" : "info.circle.fill")
                .foregroundStyle(item.severity == .urgent ? PaquitoColors.stateDanger : PaquitoColors.stateInfoStrong)
            VStack(alignment: .leading, spacing: 4) {
                Text(item.title)
                    .font(PaquitoTypography.taskTitle)
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text(item.body)
                    .font(PaquitoTypography.bodySmall)
                    .foregroundStyle(PaquitoColors.textSecondary)
            }
            Spacer()
            Text(item.timestamp)
                .font(PaquitoTypography.caption)
                .foregroundStyle(PaquitoColors.textMuted)
        }
        .padding(14)
        .background(PaquitoColors.surfaceGlass)
        .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
        .accessibilityElement(children: .combine)
    }
}

private struct NotificationDetailView: View {
    let item: NotificationItem
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            VStack(alignment: .leading, spacing: 16) {
                Image(systemName: item.severity == .urgent ? "exclamationmark.circle.fill" : "info.circle.fill")
                    .font(.system(size: 42))
                    .foregroundStyle(item.severity == .urgent ? PaquitoColors.stateDanger : PaquitoColors.stateInfoStrong)
                Text(item.title)
                    .font(PaquitoTypography.headline.weight(.semibold))
                    .foregroundStyle(PaquitoColors.textPrimary)
                Text(item.body)
                    .font(PaquitoTypography.body)
                    .foregroundStyle(PaquitoColors.textSecondary)
                Text(item.timestamp)
                    .font(PaquitoTypography.caption)
                    .foregroundStyle(PaquitoColors.textMuted)
                Spacer()
            }
            .padding(PaquitoSpacing.lg)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(PaquitoColors.background)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Cerrar") { dismiss() }
                }
            }
        }
    }
}
