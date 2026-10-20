import Foundation
import UserNotifications

protocol NotificationPermissionService {
    func requestAuthorization() async -> Bool
}

struct SystemNotificationPermissionService: NotificationPermissionService {
    func requestAuthorization() async -> Bool {
        do {
            return try await UNUserNotificationCenter.current().requestAuthorization(
                options: [.alert, .badge, .sound]
            )
        } catch {
            return false
        }
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
