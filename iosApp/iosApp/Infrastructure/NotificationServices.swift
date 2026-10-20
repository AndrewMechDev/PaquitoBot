import Foundation
import UIKit
import UserNotifications

enum NotificationAuthorizationState: Equatable {
    case notDetermined
    case denied
    case authorized
    case provisional
}

protocol NotificationPermissionService {
    @MainActor func requestAuthorization() async -> Bool
    @MainActor func authorizationState() async -> NotificationAuthorizationState
    @MainActor func openSettings()
    @MainActor func sendTestNotification() async -> Bool
}

struct SystemNotificationPermissionService: NotificationPermissionService {
    @MainActor
    func requestAuthorization() async -> Bool {
        do {
            return try await UNUserNotificationCenter.current().requestAuthorization(
                options: [.alert, .badge, .sound]
            )
        } catch {
            return false
        }
    }

    @MainActor
    func authorizationState() async -> NotificationAuthorizationState {
        switch await UNUserNotificationCenter.current().notificationSettings().authorizationStatus {
        case .authorized: return .authorized
        case .provisional: return .provisional
        case .denied: return .denied
        default: return .notDetermined
        }
    }

    @MainActor
    func openSettings() {
        guard let url = URL(string: UIApplication.openSettingsURLString) else { return }
        UIApplication.shared.open(url)
    }

    @MainActor
    func sendTestNotification() async -> Bool {
        let content = UNMutableNotificationContent()
        content.title = "Aviso de prueba"
        content.body = "Las notificaciones de PaquitoBot están activas en este iPhone."
        content.sound = .default
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(
            identifier: "paquitobot.notification.test",
            content: content,
            trigger: trigger
        )
        do {
            try await UNUserNotificationCenter.current().add(request)
            return true
        } catch {
            return false
        }
    }
}

@MainActor
protocol NotificationScheduleService: AnyObject {
    func synchronize(setup: NotificationSetup, tasks: [CalendarTask]) async
}

@MainActor
final class LocalNotificationScheduleService: NotificationScheduleService {
    private let center: UNUserNotificationCenter

    init(center: UNUserNotificationCenter = .current()) {
        self.center = center
    }

    func synchronize(setup: NotificationSetup, tasks: [CalendarTask]) async {
        let pending = await center.pendingNotificationRequests()
        let managedIdentifiers = pending.map(\.identifier).filter {
            $0.hasPrefix(Self.taskIdentifierPrefix) || $0 == Self.dailySummaryIdentifier
        }
        center.removePendingNotificationRequests(withIdentifiers: managedIdentifiers)

        let settings = await center.notificationSettings()
        guard settings.authorizationStatus == .authorized || settings.authorizationStatus == .provisional else {
            return
        }

        if !setup.enabled.isEmpty {
            for task in tasks {
                guard setup.enabled.contains(task.notificationCategory),
                      let dueDate = task.dueDate,
                      dueDate > .now else { continue }
                let reminderDate = dueDate.addingTimeInterval(-Double(setup.reminderHours) * 3_600)
                let triggerDate = reminderDate > .now ? reminderDate : Date(timeIntervalSinceNow: 1)
                let content = UNMutableNotificationContent()
                content.title = task.notificationCategory == .laboratories ? "Laboratorio próximo" : "Entrega próxima"
                content.body = "\(task.title) · \(task.course)"
                content.sound = .default
                content.userInfo = ["taskID": task.id]

                let components = Calendar.current.dateComponents(
                    [.year, .month, .day, .hour, .minute],
                    from: triggerDate
                )
                let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
                let request = UNNotificationRequest(
                    identifier: Self.taskIdentifier(for: task),
                    content: content,
                    trigger: trigger
                )
                try? await center.add(request)
            }
        }

        if setup.dailySummaryEnabled && !setup.enabled.isEmpty {
            let content = UNMutableNotificationContent()
            content.title = "Resumen académico"
            let pendingCount = tasks.filter {
                !$0.isPast && setup.enabled.contains($0.notificationCategory)
            }.count
            content.body = pendingCount > 0
                ? "Tienes \(pendingCount) entregas o laboratorios pendientes en PaquitoBot."
                : "Revisa tus avisos académicos en PaquitoBot."
            content.sound = .default
            let trigger = UNCalendarNotificationTrigger(
                dateMatching: DateComponents(hour: setup.summaryHour, minute: setup.summaryMinute),
                repeats: true
            )
            let request = UNNotificationRequest(
                identifier: Self.dailySummaryIdentifier,
                content: content,
                trigger: trigger
            )
            try? await center.add(request)
        }
    }

    private static let dailySummaryIdentifier = "paquitobot.notification.daily-summary"
    private static let taskIdentifierPrefix = "paquitobot.notification.deadline."

    private static func taskIdentifier(for task: CalendarTask) -> String {
        "paquitobot.notification.deadline.\(task.id)"
    }
}

enum NotificationPreferenceStore {
    private static let key = "paquitobot.notificationSetup"
    private static let configuredKey = "\(key).configured"

    static func load() -> NotificationSetup {
        let defaults = UserDefaults.standard
        guard defaults.bool(forKey: configuredKey) else { return .default }
        let values = defaults.stringArray(forKey: "\(key).enabled") ?? []
        let enabled = Set(values.compactMap { raw in
            NotificationPreference.allCases.first { $0.storageKey == raw }
        })
        return NotificationSetup(
            enabled: enabled,
            reminderHours: defaults.object(forKey: "\(key).reminderHours") as? Int ?? 24,
            dailySummaryEnabled: defaults.object(forKey: "\(key).summaryEnabled") as? Bool ?? true,
            summaryHour: defaults.object(forKey: "\(key).summaryHour") as? Int ?? 8,
            summaryMinute: defaults.object(forKey: "\(key).summaryMinute") as? Int ?? 0
        )
    }

    static func save(_ setup: NotificationSetup) {
        let defaults = UserDefaults.standard
        defaults.set(true, forKey: configuredKey)
        defaults.set(setup.enabled.map(\.storageKey).sorted(), forKey: "\(key).enabled")
        defaults.set(setup.reminderHours, forKey: "\(key).reminderHours")
        defaults.set(setup.dailySummaryEnabled, forKey: "\(key).summaryEnabled")
        defaults.set(setup.summaryHour, forKey: "\(key).summaryHour")
        defaults.set(setup.summaryMinute, forKey: "\(key).summaryMinute")
    }
}

enum NotificationInboxStore {
    private static let key = "paquitobot.notificationInbox"

    static func load() -> [NotificationItem] {
        guard let data = UserDefaults.standard.data(forKey: key),
              let items = try? JSONDecoder().decode([NotificationItem].self, from: data) else {
            return AcademicDemoStore.notifications
        }
        return items
    }

    static func save(_ items: [NotificationItem]) {
        guard let data = try? JSONEncoder().encode(items) else { return }
        UserDefaults.standard.set(data, forKey: key)
    }
}
