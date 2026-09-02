import Foundation
import Observation

@MainActor
@Observable
final class AcademicConnectViewModel {
    private let bridge: SharedLogicBridge

    var isConnecting = false
    var isConnected = false
    var errorMessage: String?

    init(bridge: SharedLogicBridge, initialKey: String? = nil) {
        self.bridge = bridge
        isConnected = initialKey != nil
    }

    func connect(apiKey: String) async -> Bool {
        let key = apiKey.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !key.isEmpty, !isConnecting else { return false }
        isConnecting = true
        errorMessage = nil
        defer { isConnecting = false }
        do {
            let temporary = SharedLogicBridge(accessToken: nil, canvasMockKey: key)
            _ = try await temporary.academicProfile()
            bridge.setCanvasMockKey(key)
            isConnected = true
            return true
        } catch {
            errorMessage = userFacingMessage(for: error)
            return false
        }
    }
}
