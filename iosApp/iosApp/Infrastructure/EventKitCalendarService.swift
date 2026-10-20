import EventKit
import Foundation

@MainActor
protocol CalendarEventService: AnyObject {
    var isAuthorized: Bool { get }
    func requestAuthorization() async throws
    func sync(tasks: [CalendarTask]) async throws -> CalendarSyncSummary
}

@MainActor
final class EventKitCalendarService: CalendarEventService {
    private let store = EKEventStore()
    private var calendar: EKCalendar?

    var isAuthorized: Bool {
        EKEventStore.authorizationStatus(for: .event) == .fullAccess
    }

    func requestAuthorization() async throws {
        if !isAuthorized {
            let granted = try await store.requestFullAccessToEvents()
            guard granted else { throw CalendarEventServiceError.permissionDenied }
        }

        guard let defaultCalendar = store.defaultCalendarForNewEvents else {
            throw CalendarEventServiceError.noWritableCalendar
        }
        calendar = defaultCalendar
    }

    func sync(tasks: [CalendarTask]) async throws -> CalendarSyncSummary {
        guard isAuthorized else { throw CalendarEventServiceError.permissionDenied }
        let targetCalendar = try writableCalendar()
        guard !tasks.isEmpty else { return CalendarSyncSummary(created: 0, updated: 0, unchanged: 0) }

        var created = 0
        var updated = 0
        var unchanged = 0

        for task in tasks {
            guard let dueDate = task.dueDate else {
                throw CalendarEventServiceError.missingDate(task.title)
            }

            let title = "\(task.course) · \(task.title)"
            let endDate = dueDate.addingTimeInterval(15 * 60)
            let marker = marker(for: task)
            if let existing = findEvent(marker: marker, around: dueDate, in: targetCalendar) {
                let matches = existing.title == title
                    && abs(existing.startDate.timeIntervalSince(dueDate)) < 1
                    && abs(existing.endDate.timeIntervalSince(endDate)) < 1

                if matches {
                    unchanged += 1
                } else {
                    existing.title = title
                    existing.startDate = dueDate
                    existing.endDate = endDate
                    existing.calendar = targetCalendar
                    existing.notes = notes(for: task, marker: marker)
                    try store.save(existing, span: .thisEvent, commit: false)
                    updated += 1
                }
            } else {
                let event = EKEvent(eventStore: store)
                event.title = title
                event.startDate = dueDate
                event.endDate = endDate
                event.calendar = targetCalendar
                event.notes = notes(for: task, marker: marker)
                try store.save(event, span: .thisEvent, commit: false)
                created += 1
            }
        }

        try store.commit()
        return CalendarSyncSummary(created: created, updated: updated, unchanged: unchanged)
    }

    private func writableCalendar() throws -> EKCalendar {
        if let calendar { return calendar }
        guard let defaultCalendar = store.defaultCalendarForNewEvents else {
            throw CalendarEventServiceError.noWritableCalendar
        }
        calendar = defaultCalendar
        return defaultCalendar
    }

    private func findEvent(marker: String, around date: Date, in calendar: EKCalendar) -> EKEvent? {
        let predicate = store.predicateForEvents(
            withStart: date.addingTimeInterval(-86_400),
            end: date.addingTimeInterval(86_400),
            calendars: [calendar]
        )
        return store.events(matching: predicate).first { event in
            event.notes?.contains(marker) == true
        }
    }

    private func marker(for task: CalendarTask) -> String {
        "PaquitoBot task: \(task.id)"
    }

    private func notes(for task: CalendarTask, marker: String) -> String {
        "\(marker)\nEntrega académica demo · \(task.course)"
    }
}

private enum CalendarEventServiceError: LocalizedError {
    case permissionDenied
    case noWritableCalendar
    case missingDate(String)

    var errorDescription: String? {
        switch self {
        case .permissionDenied:
            return "No hay permiso para acceder al calendario. Puedes habilitarlo desde Ajustes."
        case .noWritableCalendar:
            return "No hay un calendario disponible para guardar eventos. Añade una cuenta en Calendario."
        case let .missingDate(title):
            return "La entrega «\(title)» no tiene una fecha válida para sincronizar."
        }
    }
}
