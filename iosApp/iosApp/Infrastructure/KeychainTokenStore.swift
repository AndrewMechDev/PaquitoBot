import Foundation
import Security

final class KeychainTokenStore {
    private let service = "pe.tecsup.paquitobot"

    private enum Key: String {
        case accessToken
        case tokenExpiry
        case firstName
        case onboardingComplete
        case canvasMockKey
        case canvasMockSkipped
    }

    var accessToken: String? {
        get { string(for: .accessToken) }
        set { set(newValue, for: .accessToken) }
    }

    var tokenExpiry: Date? {
        get {
            guard let value = string(for: .tokenExpiry), let time = TimeInterval(value) else { return nil }
            return Date(timeIntervalSince1970: time)
        }
        set { set(newValue.map { String($0.timeIntervalSince1970) }, for: .tokenExpiry) }
    }

    var firstName: String? {
        get { string(for: .firstName) }
        set { set(newValue, for: .firstName) }
    }

    var isOnboardingComplete: Bool {
        get { string(for: .onboardingComplete) == "true" }
        set { set(newValue ? "true" : "false", for: .onboardingComplete) }
    }

    var canvasMockKey: String? {
        get { string(for: .canvasMockKey) }
        set { set(newValue, for: .canvasMockKey) }
    }

    var canvasMockSkipped: Bool {
        get { string(for: .canvasMockSkipped) == "true" }
        set { set(newValue ? "true" : "false", for: .canvasMockSkipped) }
    }

    var hasValidSession: Bool {
        guard let token = accessToken, !token.isEmpty else { return false }
        guard let expiry = tokenExpiry else { return true }
        return expiry > Date()
    }

    func clearSession() {
        accessToken = nil
        tokenExpiry = nil
        firstName = nil
    }

    private func accountName(for key: Key) -> String {
        "\(service).\(key.rawValue)"
    }

    private func string(for storageKey: Key) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: accountName(for: storageKey),
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
        ]
        var result: CFTypeRef?
        guard SecItemCopyMatching(query as CFDictionary, &result) == errSecSuccess,
              let data = result as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    private func set(_ value: String?, for storageKey: Key) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrService as String: service,
            kSecAttrAccount as String: accountName(for: storageKey)
        ]
        SecItemDelete(query as CFDictionary)
        guard let value else { return }
        var item = query
        item[kSecValueData as String] = Data(value.utf8)
        SecItemAdd(item as CFDictionary, nil)
    }
}
