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
    private static let key = "paquitobot.notificationPreferences"

    static func load() -> Set<NotificationPreference> {
        let values = UserDefaults.standard.stringArray(forKey: key) ?? []
        return Set(values.compactMap(NotificationPreference.init(rawValue:)))
    }

    static func save(_ preferences: Set<NotificationPreference>) {
        UserDefaults.standard.set(preferences.map(\.rawValue).sorted(), forKey: key)
    }
}
