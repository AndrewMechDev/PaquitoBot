import SwiftUI

struct NotificationsInboxView: View {
    @State private var items: [NotificationItem]
    @State private var isCompact = true
    @State private var selectedItem: NotificationItem?
    @State private var showDeleteAllConfirmation = false
    @State private var showPreferences = false
    @State private var period: NotificationPeriod = .all
    @State private var deletedItem: NotificationItem?
    @State private var deletedIndex: Int?
    @State private var showUndo = false
    let setup: NotificationSetup
    let onSaveSetup: (NotificationSetup) -> Void
    let onItemsChanged: ([NotificationItem]) -> Void

    init(
        items: [NotificationItem],
        setup: NotificationSetup = .default,
        onSaveSetup: @escaping (NotificationSetup) -> Void = { _ in },
        onItemsChanged: @escaping ([NotificationItem]) -> Void = { _ in }
    ) {
        _items = State(initialValue: items)
        self.setup = setup
        self.onSaveSetup = onSaveSetup
        self.onItemsChanged = onItemsChanged
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

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(NotificationPeriod.allCases) { option in
                        NotificationPeriodChip(title: option.title, isSelected: period == option) {
                            period = option
                        }
                    }
                }
                .padding(.horizontal, PaquitoSpacing.lg)
                .padding(.vertical, 10)
            }

            if visibleItems.isEmpty {
                emptyState
            } else {
                List {
                    ForEach(visibleItems) { item in
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
        .safeAreaInset(edge: .bottom, spacing: 0) {
            if showUndo, let deletedItem {
                HStack(spacing: 12) {
                    Text("Notificación eliminada")
                        .font(PaquitoTypography.bodySmall)
                        .foregroundStyle(PaquitoColors.textPrimary)
                    Spacer()
                    Button("Deshacer") { undoDelete(deletedItem) }
                        .font(PaquitoTypography.bodyMedium.weight(.semibold))
                        .foregroundStyle(PaquitoColors.stateInfoStrong)
                }
                .padding(.horizontal, PaquitoSpacing.lg)
                .padding(.vertical, 12)
                .background(.ultraThinMaterial)
            }
        }
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
                withAnimation {
                    items.removeAll()
                    onItemsChanged(items)
                }
            }
            Button("Cancelar", role: .cancel) {}
        }
    }

    private var emptyState: some View {
        ContentUnavailableView(
            period == .all ? "Sin notificaciones" : "Sin avisos en este período",
            systemImage: "bell.slash",
            description: Text(period == .all ? "Cuando Paquito tenga algo importante que avisarte, aparecerá aquí." : "Prueba con otro período o vuelve a Todos.")
        )
    }

    private func delete(_ item: NotificationItem) {
        guard let index = items.firstIndex(where: { $0.id == item.id }) else { return }
        withAnimation {
            deletedItem = item
            deletedIndex = index
            items.remove(at: index)
            showUndo = true
            onItemsChanged(items)
        }
        Task { @MainActor in
            try? await Task.sleep(for: .seconds(4))
            if deletedItem?.id == item.id {
                showUndo = false
                deletedItem = nil
                deletedIndex = nil
            }
        }
    }

    private func undoDelete(_ item: NotificationItem) {
        withAnimation {
            let index = deletedIndex ?? items.count
            items.insert(item, at: min(index, items.count))
            onItemsChanged(items)
            showUndo = false
            deletedItem = nil
            deletedIndex = nil
        }
    }

    private var visibleItems: [NotificationItem] {
        items.filter { period.matches($0) }
    }
}

private enum NotificationPeriod: String, CaseIterable, Identifiable {
    case all
    case today
    case week
    case older

    var id: String { rawValue }

    var title: String {
        switch self {
        case .all: return "Todos"
        case .today: return "Hoy"
        case .week: return "Semana"
        case .older: return "Anteriores"
        }
    }

    func matches(_ item: NotificationItem) -> Bool {
        let timestamp = item.timestamp.lowercased()
        let isToday = timestamp.contains("hoy") || timestamp.contains(" h") || timestamp.contains("min") || timestamp.contains("ahora")
        let isWeek = isToday || timestamp.contains("mañana") || timestamp.contains("ayer") || timestamp.contains(" d")
        switch self {
        case .all: return true
        case .today: return isToday
        case .week: return isWeek
        case .older: return !isWeek
        }
    }
}

private struct NotificationPeriodChip: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(title, action: action)
            .font(PaquitoTypography.caption.weight(isSelected ? .semibold : .regular))
            .foregroundStyle(isSelected ? PaquitoColors.stateInfoStrong : PaquitoColors.textSecondary)
            .padding(.horizontal, 14)
            .padding(.vertical, 9)
            .background(isSelected ? PaquitoColors.brandPrimary.opacity(0.14) : PaquitoColors.surfaceGlass)
            .clipShape(Capsule())
            .overlay(Capsule().stroke(isSelected ? PaquitoColors.stateInfoStrong : PaquitoColors.borderDefault, lineWidth: 1))
            .buttonStyle(.plain)
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
