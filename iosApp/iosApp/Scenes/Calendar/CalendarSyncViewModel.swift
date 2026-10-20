import Foundation

@MainActor
final class CalendarSyncViewModel: ObservableObject {
    @Published private(set) var isConnected: Bool
    @Published private(set) var isWorking = false
    @Published private(set) var message: String?
    @Published private(set) var summary: CalendarSyncSummary?

    private let service: CalendarEventService

    init(service: CalendarEventService) {
        self.service = service
        isConnected = service.isAuthorized
    }

    func connect() {
        guard !isWorking else { return }
        isWorking = true
        message = nil

        Task { [weak self] in
            guard let self else { return }
            do {
                try await service.requestAuthorization()
                isConnected = true
                message = "Calendario conectado. Revisa las entregas y elige cuáles quieres enviar."
            } catch {
                isConnected = false
                message = error.localizedDescription
            }
            isWorking = false
        }
    }

    func disconnect() {
        isConnected = false
        summary = nil
        message = "La app dejó de usar el calendario. Los permisos se administran desde Ajustes."
    }

    func sync(tasks: [CalendarTask]) {
        guard isConnected, !tasks.isEmpty, !isWorking else { return }
        isWorking = true
        summary = nil
        message = nil

        Task { [weak self] in
            guard let self else { return }
            do {
                summary = try await service.sync(tasks: tasks)
                message = "Sincronización completada. Puedes repetirla sin duplicar tareas."
            } catch {
                message = error.localizedDescription
            }
            isWorking = false
        }
    }
}
