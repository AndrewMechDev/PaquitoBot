import Foundation

#if canImport(SharedLogic)
import SharedLogic
#endif

#if canImport(ClerkKit)
import ClerkKit
#endif

struct AuthUser: Equatable {
    let firstName: String
    let email: String
}

enum AuthSessionState: Equatable {
    case signedOut
    case authorized(AuthUser)
    case unauthorized
}

@MainActor
protocol AuthService {
    var currentUser: AuthUser? { get }
    var currentSessionState: AuthSessionState { get }
    func signIn() async throws -> AuthUser
    func signOut() async throws
}

enum AuthServiceError: LocalizedError {
    case unavailable
    case unauthorized

    var errorDescription: String? {
        switch self {
        case .unavailable:
            return "No se pudo iniciar sesión. Revisa tu conexión e inténtalo de nuevo."
        case .unauthorized:
            return "Solo se permite un correo principal verificado de @tecsup.edu.pe."
        }
    }
}

/// Local fallback used when the iOS target has no Clerk configuration.
@MainActor
final class DemoAuthService: AuthService {
    private(set) var currentUser: AuthUser?

    var currentSessionState: AuthSessionState {
        if let currentUser {
            return .authorized(currentUser)
        }
        return .signedOut
    }

    func signIn() async throws -> AuthUser {
        try await Task.sleep(nanoseconds: 250_000_000)
        let user = AuthUser(firstName: "estudiante", email: "estudiante@tecsup.edu.pe")
        currentUser = user
        return user
    }

    func signOut() async throws {
        try await Task.sleep(nanoseconds: 120_000_000)
        currentUser = nil
    }
}

@MainActor
enum AuthServiceFactory {
    static func makeDefault() -> AuthService {
        #if canImport(ClerkKit)
        if let publishableKey = Bundle.main.object(forInfoDictionaryKey: "CLERK_PUBLISHABLE_KEY") as? String,
           publishableKey.hasPrefix("pk_") {
            return ClerkAuthService(clerk: Clerk.shared)
        }
        #endif

        return DemoAuthService()
    }
}

#if canImport(ClerkKit)
@MainActor
final class ClerkAuthService: AuthService {
    private let clerk: Clerk

    init(clerk: Clerk) {
        self.clerk = clerk
    }

    var currentUser: AuthUser? {
        guard case let .authorized(user) = currentSessionState else { return nil }
        return user
    }

    var currentSessionState: AuthSessionState {
        guard let user = clerk.user else { return .signedOut }
        guard let primaryEmail = user.primaryEmailAddress,
              primaryEmail.verification?.status == .verified,
              Self.isInstitutionalEmail(primaryEmail.emailAddress) else {
            return .unauthorized
        }

        let firstName = user.firstName?.trimmingCharacters(in: .whitespacesAndNewlines)
        let displayName = firstName.flatMap { $0.isEmpty ? nil : $0 } ?? "estudiante"
        return .authorized(AuthUser(firstName: displayName, email: primaryEmail.emailAddress))
    }

    func signIn() async throws -> AuthUser {
        _ = try await clerk.auth.startHostedAuth()
        guard case let .authorized(user) = currentSessionState else {
            throw AuthServiceError.unauthorized
        }
        return user
    }

    func signOut() async throws {
        try await clerk.auth.signOut()
    }

    private static func isInstitutionalEmail(_ value: String) -> Bool {
        let parts = value.split(separator: "@", omittingEmptySubsequences: false)
        guard parts.count == 2, !parts[0].isEmpty else { return false }
        return parts[1].caseInsensitiveCompare("tecsup.edu.pe") == .orderedSame
    }
}
#endif

protocol ChatService {
    func askAssistant(question: String) async throws -> String
}

final class DemoChatService: ChatService {
    func askAssistant(question: String) async throws -> String {
        try await Task.sleep(nanoseconds: 420_000_000)
        let normalized = question.lowercased()
        if ["curso", "nota", "promedio", "calificación"].contains(where: normalized.contains) {
            return "Por ahora ves cursos y notas de ejemplo en la pestaña Cursos. Cuando conectemos tus datos reales, podré ayudarte con tu progreso."
        }
        if ["horario", "clase", "falta", "asistencia"].contains(where: normalized.contains) {
            return "Puedes consultar el horario de ejemplo en la pestaña Horarios. Todavía no tengo acceso a tu horario real."
        }
        if ["tarea", "entrega", "plazo", "pendiente"].contains(where: normalized.contains) {
            return "En Inicio encontrarás entregas de ejemplo. Cuando conectemos los datos reales, aparecerán tus pendientes."
        }
        return "Soy Paquito en modo demostración. Puedo orientarte sobre cursos, horarios y entregas usando los datos de ejemplo de la app."
    }
}

enum ChatServiceFactory {
    static func makeDefault() -> ChatService {
        #if DEBUG
        let apiKey = Bundle.main.object(forInfoDictionaryKey: "MINIMAX_API_KEY") as? String ?? ""
        return MiniMaxChatService(apiKey: apiKey)
        #else
        #if canImport(SharedLogic)
        return SharedLogicChatService()
        #else
        return DemoChatService()
        #endif
        #endif
    }
}

#if canImport(SharedLogic)
private enum SharedLogicChatServiceError: LocalizedError {
    case invalidResponse

    var errorDescription: String? {
        switch self {
        case .invalidResponse:
            return "El módulo compartido devolvió una respuesta inválida."
        }
    }
}

final class SharedLogicChatService: ChatService {
    private let repository: ChatRepository

    init(repository: ChatRepository = ChatRepositoryFactoryKt.createDefaultChatRepository()) {
        self.repository = repository
    }

    func askAssistant(question: String) async throws -> String {
        try await withCheckedThrowingContinuation { continuation in
            repository.askAssistant(question: question) { result, error in
                if let error {
                    continuation.resume(throwing: error)
                    return
                }

                guard let answer = result as? ChatAnswer else {
                    continuation.resume(throwing: SharedLogicChatServiceError.invalidResponse)
                    return
                }

                continuation.resume(returning: answer.text)
            }
        }
    }
}
#endif
