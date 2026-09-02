import Foundation
import Observation

@MainActor
@Observable
final class ChatViewModel {
    private let bridge: SharedLogicBridge

    var messages: [ChatMessage] = []
    var isSending = false
    var userName: String?

    init(bridge: SharedLogicBridge, userName: String? = nil) {
        self.bridge = bridge
        self.userName = userName
    }

    var greeting: String {
        if let userName, !userName.isEmpty {
            return "Hola, \(userName)"
        }
        return "Hola"
    }

    func send(_ rawText: String) {
        let text = rawText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty, !isSending else { return }
        messages.append(ChatMessage(role: .user, body: text, footnote: nil, timestamp: currentTime(), status: .sent))
        isSending = true
        Task { @MainActor in
            defer { isSending = false }
            do {
                let answer = try await bridge.askAssistant(question: text)
                messages.append(ChatMessage(role: .bot, body: answer.text, footnote: answer.route.isEmpty ? nil : "Ruta: \(answer.route)", timestamp: currentTime(), status: nil))
            } catch {
                messages.append(ChatMessage(role: .system, body: userFacingMessage(for: error), footnote: nil, timestamp: currentTime(), status: nil))
            }
        }
    }

    private func currentTime() -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_PE")
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: Date())
    }
}
