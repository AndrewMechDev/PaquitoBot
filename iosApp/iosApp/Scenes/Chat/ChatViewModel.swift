import Foundation

@MainActor
final class ChatViewModel: ObservableObject {
    @Published private(set) var messages: [ChatMessage] = []
    @Published private(set) var isSending = false

    private let service: ChatService

    init(service: ChatService) {
        self.service = service
    }

    func send(_ rawText: String) {
        let text = rawText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !text.isEmpty, !isSending else { return }

        messages.append(ChatMessage(role: .user, body: text))
        isSending = true

        Task { [weak self] in
            guard let self else { return }
            do {
                let answer = try await service.askAssistant(question: text)
                messages.append(ChatMessage(role: .bot, body: answer))
            } catch {
                messages.append(
                    ChatMessage(
                        role: .system,
                        body: error.localizedDescription.isEmpty
                            ? "No pude responder. Inténtalo de nuevo."
                            : error.localizedDescription
                    )
                )
            }
            isSending = false
        }
    }
}
