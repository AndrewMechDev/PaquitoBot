import Foundation

final class MiniMaxChatService: ChatService {
    private let apiKey: String
    private let session: URLSession
    private var history: [MiniMaxMessage] = []

    init(apiKey: String, session: URLSession? = nil) {
        self.apiKey = apiKey.trimmingCharacters(in: .whitespacesAndNewlines)
        if let session {
            self.session = session
        } else {
            let configuration = URLSessionConfiguration.ephemeral
            configuration.timeoutIntervalForRequest = 15
            configuration.timeoutIntervalForResource = 90
            configuration.waitsForConnectivity = false
            self.session = URLSession(configuration: configuration)
        }
    }

    func askAssistant(question: String) async throws -> String {
        guard !apiKey.isEmpty else { throw MiniMaxChatServiceError.missingAPIKey }

        var messages = [
            MiniMaxMessage(role: "system", content: Self.systemInstruction +
                "\n\nDatos académicos de esta cuenta (JSON):\n" +
                AcademicDemoStore.minimaxContextJSON())
        ]
        messages.append(contentsOf: history)
        messages.append(MiniMaxMessage(role: "user", content: question))

        var request = URLRequest(url: Self.endpoint)
        request.httpMethod = "POST"
        request.timeoutInterval = 90
        request.setValue("application/json; charset=utf-8", forHTTPHeaderField: "Content-Type")
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.httpBody = try JSONEncoder().encode(
            MiniMaxRequest(
                model: Self.model,
                messages: messages,
                thinking: MiniMaxThinking(type: "disabled"),
                maxCompletionTokens: 1024
            )
        )

        let data: Data
        let response: URLResponse
        do {
            (data, response) = try await session.data(for: request)
        } catch {
            throw MiniMaxChatServiceError.network(underlying: error)
        }

        guard let httpResponse = response as? HTTPURLResponse else {
            throw MiniMaxChatServiceError.invalidResponse
        }

        guard (200...299).contains(httpResponse.statusCode) else {
            throw Self.error(for: httpResponse.statusCode)
        }

        let decoded: MiniMaxResponse
        do {
            decoded = try JSONDecoder().decode(MiniMaxResponse.self, from: data)
        } catch {
            throw MiniMaxChatServiceError.invalidResponse
        }

        if let baseResponse = decoded.baseResponse, baseResponse.statusCode != 0 {
            throw MiniMaxChatServiceError.rejected(
                baseResponse.statusMessage ?? "MiniMax rechazó la solicitud."
            )
        }

        guard let answer = decoded.choices.first?.message.content
            .trimmingCharacters(in: .whitespacesAndNewlines),
              !answer.isEmpty else {
            throw MiniMaxChatServiceError.emptyAnswer
        }

        history.append(MiniMaxMessage(role: "user", content: question))
        history.append(MiniMaxMessage(role: "assistant", content: answer))
        if history.count > Self.maxHistoryMessages {
            history.removeFirst(history.count - Self.maxHistoryMessages)
        }
        return answer
    }

    private static func error(for statusCode: Int) -> MiniMaxChatServiceError {
        switch statusCode {
        case 400:
            return .rejected("MiniMax rechazó la solicitud. Revisa la configuración del modelo.")
        case 401, 403:
            return .unauthorized
        case 429:
            return .rateLimited
        default:
            return .http(statusCode)
        }
    }

    private static let endpoint = URL(string: "https://api.minimax.io/v1/chat/completions")!
    private static let model = "MiniMax-M3"
    private static let maxHistoryMessages = 12
    private static let systemInstruction = """
        Eres Paquito, asistente académico de PaquitoBot. Responde en español, de forma breve y clara. Evita el uso de tablas.

        Solo atiende consultas sobre cursos, notas, tareas, horarios, asistencias, avisos y funcionalidades del sistema académico.

        Usa exclusivamente los datos académicos de demostración de esta cuenta. No inventes información. Para fechas relativas, como «hoy», usa fecha_actual del JSON.

        Rechaza cualquier consulta ajena al sistema académico, incluso preguntas básicas de matemáticas, programación, traducción, cultura general o temas generales. No respondas parcialmente ni indirectamente a esas consultas.

        Si la consulta está fuera de alcance, responde únicamente: «Solo puedo ayudarte con consultas relacionadas con tu sistema académico de PaquitoBot».
        """
}

private enum MiniMaxChatServiceError: LocalizedError {
    case missingAPIKey
    case network(underlying: Error)
    case invalidResponse
    case rejected(String)
    case unauthorized
    case rateLimited
    case http(Int)
    case emptyAnswer

    var errorDescription: String? {
        switch self {
        case .missingAPIKey:
            return "Falta MINIMAX_API_KEY en iosApp/Configuration/Config.local.xcconfig."
        case .network:
            return "No pude conectar con MiniMax. Revisa tu conexión e inténtalo de nuevo."
        case .invalidResponse:
            return "MiniMax devolvió una respuesta inválida. Inténtalo de nuevo."
        case let .rejected(message):
            return message
        case .unauthorized:
            return "La clave de MiniMax no es válida o no tiene acceso al modelo."
        case .rateLimited:
            return "MiniMax alcanzó el límite de solicitudes. Inténtalo más tarde."
        case let .http(statusCode):
            return "MiniMax no respondió (HTTP \(statusCode)). Inténtalo de nuevo."
        case .emptyAnswer:
            return "MiniMax no devolvió una respuesta de texto. Inténtalo de nuevo."
        }
    }
}

private struct MiniMaxRequest: Encodable {
    let model: String
    let messages: [MiniMaxMessage]
    let thinking: MiniMaxThinking
    let maxCompletionTokens: Int

    enum CodingKeys: String, CodingKey {
        case model, messages, thinking
        case maxCompletionTokens = "max_completion_tokens"
    }
}

private struct MiniMaxMessage: Codable {
    let role: String
    let content: String
}

private struct MiniMaxThinking: Encodable {
    let type: String
}

private struct MiniMaxResponse: Decodable {
    let baseResponse: MiniMaxBaseResponse?
    let choices: [MiniMaxChoice]

    enum CodingKeys: String, CodingKey {
        case choices
        case baseResponse = "base_resp"
    }
}

private struct MiniMaxBaseResponse: Decodable {
    let statusCode: Int
    let statusMessage: String?

    enum CodingKeys: String, CodingKey {
        case statusCode = "status_code"
        case statusMessage = "status_msg"
    }
}

private struct MiniMaxChoice: Decodable {
    let message: MiniMaxMessage
}
