import SwiftUI

struct NotificationsInboxView: View {
    let onBack: () -> Void
    @State private var variant: NotificationCardVariant = .full
    @State private var items: [NotificationInboxItem] = [
        NotificationInboxItem(id: "deadline", title: "Entrega próxima", body: "Laboratorio 4 vence hoy a las 23:59.", timestamp: "Hoy", severity: .urgent, systemImage: "calendar.badge.exclamationmark"),
        NotificationInboxItem(id: "grade", title: "Nota nueva", body: "Ya hay una calificación nueva en Cálculo II.", timestamp: "Ayer", severity: .info, systemImage: "checkmark.circle"),
        NotificationInboxItem(id: "attendance", title: "Revisá tus faltas", body: "Registramos una falta en Física I.", timestamp: "2 ago", severity: .urgent, systemImage: "person.crop.circle.badge.exclamationmark"),
        NotificationInboxItem(id: "sync", title: "Datos sincronizados", body: "Tus datos académicos están actualizados.", timestamp: "2 ago", severity: .info, systemImage: "arrow.triangle.2.circlepath")
    ]
    @State private var selectedItem: NotificationInboxItem?
    @State private var showingDeleteAll = false
    @State private var lastDeleted: (Int, NotificationInboxItem)?
    @State private var showUndo = false

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack(alignment: .leading, spacing: 0) {
                HStack {
                    PaquitoBackButton(action: onBack)
                    Spacer()
                    Picker("Vista", selection: $variant) {
                        Text("Lista").tag(NotificationCardVariant.full)
                        Text("Cuadros").tag(NotificationCardVariant.compact)
                    }
                    .pickerStyle(.segmented)
                    .frame(width: 150)
                    Button { showingDeleteAll = true } label: { Image(systemName: "trash").foregroundStyle(PaquitoColors.danger) }
                        .buttonStyle(.plain)
                        .disabled(items.isEmpty)
                }
                .padding(.horizontal, 12)
                Text("Notificaciones").font(PaquitoTypography.displayLarge).padding(.horizontal, PaquitoSpacing.lg)
                ScrollView {
                    if items.isEmpty {
                        ContentUnavailableView("No hay notificaciones", systemImage: "bell.slash", description: Text("Cuando haya algo importante, aparecerá aquí."))
                            .frame(maxWidth: .infinity, minHeight: 300)
                    } else if variant == .compact {
                        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) { ForEach(items) { item in notificationCard(item) } }
                    } else {
                        VStack(spacing: 10) { ForEach(items) { item in notificationRow(item) } }
                    }
                }
                .scrollIndicators(.hidden)
                .padding(.horizontal, PaquitoSpacing.lg)
                .padding(.top, 16)
            }
            .background(PaquitoColors.background)

            if showUndo, let lastDeleted {
                HStack {
                    Text("Notificación eliminada").font(PaquitoTypography.bodySmall).foregroundStyle(.white)
                    Spacer()
                    Button("Deshacer") { undo(lastDeleted) }.font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.brandPrimary)
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(PaquitoColors.darkCard)
                .clipShape(RoundedRectangle(cornerRadius: 16, style: .continuous))
                .padding(.horizontal, 16)
                .padding(.bottom, 20)
            }
        }
        .safeAreaPadding(.top)
        .sheet(item: $selectedItem) { item in
            VStack(alignment: .leading, spacing: 12) {
                Text(item.title).font(PaquitoTypography.displayLarge)
                Text(item.timestamp).font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.textMuted)
                PaquitoMarkdown(content: item.body)
                Spacer()
            }
            .padding(24)
            .presentationDetents([.medium])
        }
        .alert("¿Eliminar todas las notificaciones?", isPresented: $showingDeleteAll) {
            Button("Eliminar", role: .destructive) { items.removeAll() }
            Button("Cancelar", role: .cancel) {}
        }
    }

    @ViewBuilder
    private func notificationRow(_ item: NotificationInboxItem) -> some View {
        Button { selectedItem = item } label: {
            HStack(spacing: 12) {
                Image(systemName: item.systemImage).font(.system(size: 19, weight: .semibold)).foregroundStyle(item.severity == .urgent ? PaquitoColors.danger : PaquitoColors.infoStrong).frame(width: 34, height: 34)
                VStack(alignment: .leading, spacing: 3) {
                    Text(item.title).font(PaquitoTypography.taskTitle).foregroundStyle(PaquitoColors.textPrimary)
                    Text(item.body).font(PaquitoTypography.bodySmall).foregroundStyle(PaquitoColors.textSecondary).lineLimit(2)
                }
                Spacer()
                Text(item.timestamp).font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.textMuted)
            }
            .padding(14)
            .background(PaquitoColors.surfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.medium, style: .continuous))
        }
        .buttonStyle(.plain)
        .swipeActions(edge: .trailing, allowsFullSwipe: true) { Button(role: .destructive) { delete(item) } label: { Label("Eliminar", systemImage: "trash") } }
    }

    private func notificationCard(_ item: NotificationInboxItem) -> some View {
        Button { selectedItem = item } label: {
            VStack(alignment: .leading, spacing: 10) {
                Image(systemName: item.systemImage).font(.system(size: 22, weight: .semibold)).foregroundStyle(item.severity == .urgent ? PaquitoColors.danger : PaquitoColors.infoStrong)
                Text(item.title).font(PaquitoTypography.taskTitle).foregroundStyle(PaquitoColors.textPrimary)
                Text(item.timestamp).font(PaquitoTypography.caption).foregroundStyle(PaquitoColors.textMuted)
            }
            .frame(maxWidth: .infinity, minHeight: 130, alignment: .topLeading)
            .padding(14)
            .background(PaquitoColors.surfaceElevated)
            .clipShape(RoundedRectangle(cornerRadius: PaquitoRadius.large, style: .continuous))
        }
        .buttonStyle(.plain)
    }

    private func delete(_ item: NotificationInboxItem) {
        guard let index = items.firstIndex(of: item) else { return }
        lastDeleted = (index, item)
        items.remove(at: index)
        showUndo = true
        DispatchQueue.main.asyncAfter(deadline: .now() + 3) { showUndo = false }
    }

    private func undo(_ deleted: (Int, NotificationInboxItem)) {
        items.insert(deleted.1, at: min(deleted.0, items.count))
        showUndo = false
    }
}
