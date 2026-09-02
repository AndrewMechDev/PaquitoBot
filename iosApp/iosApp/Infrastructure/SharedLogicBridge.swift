import Foundation

#if canImport(SharedLogic)
import SharedLogic
#endif

struct SharedAuthSession {
    let accessToken: String
    let expiresInSeconds: Int
    let email: String?
}

struct SharedChatAnswer {
    let text: String
    let route: String
}

struct SharedAcademicProfile {
    let id: Int
    let name: String
    let email: String
}

struct SharedAcademicCourse {
    let id: Int
    let code: String
    let name: String
}

struct SharedAcademicGrade {
    let assignmentId: Int
    let score: Double
}

struct SharedAcademicAttendance {
    let classSessionId: Int
    let isPresent: Bool
}

struct SharedAcademicAssignment {
    let id: Int
    let courseId: Int
    let name: String
    let pointsPossible: Double
    let dueAt: String?
}

struct SharedAcademicClassSession {
    let id: Int
    let courseId: Int
    let startAt: String?
    let endAt: String?
}

enum SharedLogicBridgeError: LocalizedError {
    case frameworkUnavailable
    case invalidResponse

    var errorDescription: String? {
        switch self {
        case .frameworkUnavailable:
            return "El módulo compartido no está disponible. Compila el framework SharedLogic desde Xcode."
        case .invalidResponse:
            return "El módulo compartido devolvió una respuesta inválida."
        }
    }
}

/// Single entry point from SwiftUI into the KMP domain layer.
/// It intentionally returns Swift-owned DTOs so screen ViewModels never depend
/// on generated Kotlin types or Ktor implementation details.
final class SharedLogicBridge {
    #if canImport(SharedLogic)
    private let client: SharedLogicClient
    #endif

    init(accessToken: String? = nil, canvasMockKey: String? = nil) {
        #if canImport(SharedLogic)
        client = SharedLogicClient(accessToken: accessToken, canvasMockKey: canvasMockKey)
        #endif
    }

    func setAccessToken(_ token: String?) {
        #if canImport(SharedLogic)
        client.setAccessToken(accessToken: token)
        #endif
    }

    func setCanvasMockKey(_ key: String?) {
        #if canImport(SharedLogic)
        client.setCanvasMockKey(canvasMockKey: key)
        #endif
    }

    func wakeBackend() async throws {
        #if canImport(SharedLogic)
        try await client.wakeBackend()
        #else
        throw SharedLogicBridgeError.frameworkUnavailable
        #endif
    }

    func loginWithGoogle(idToken: String) async throws -> SharedAuthSession {
        #if canImport(SharedLogic)
        let session = try await client.loginWithGoogle(idToken: idToken)
        return SharedAuthSession(
            accessToken: session.accessToken,
            expiresInSeconds: Int(session.expiresInSeconds),
            email: session.email
        )
        #else
        throw SharedLogicBridgeError.frameworkUnavailable
        #endif
    }

    func askAssistant(question: String) async throws -> SharedChatAnswer {
        #if canImport(SharedLogic)
        let answer = try await client.askAssistant(question: question)
        return SharedChatAnswer(text: answer.text, route: answer.route)
        #else
        throw SharedLogicBridgeError.frameworkUnavailable
        #endif
    }

    func academicProfile() async throws -> SharedAcademicProfile {
        #if canImport(SharedLogic)
        let profile = try await client.academicProfile()
        return SharedAcademicProfile(id: Int(profile.id), name: profile.name, email: profile.email)
        #else
        throw SharedLogicBridgeError.frameworkUnavailable
        #endif
    }

    func courses() async throws -> [SharedAcademicCourse] {
        #if canImport(SharedLogic)
        return try await client.courses().map {
            SharedAcademicCourse(id: Int($0.id), code: $0.code, name: $0.name)
        }
        #else
        throw SharedLogicBridgeError.frameworkUnavailable
        #endif
    }

    func courseAssignments(courseID: Int) async throws -> [SharedAcademicAssignment] {
        #if canImport(SharedLogic)
        return try await client.courseAssignments(courseId: Int32(courseID)).map {
            SharedAcademicAssignment(
                id: Int($0.id),
                courseId: Int($0.courseId),
                name: $0.name,
                pointsPossible: $0.pointsPossible,
                dueAt: $0.dueAt
            )
        }
        #else
        throw SharedLogicBridgeError.frameworkUnavailable
        #endif
    }

    func courseGrades(courseID: Int) async throws -> [SharedAcademicGrade] {
        #if canImport(SharedLogic)
        return try await client.courseGrades(courseId: Int32(courseID)).map {
            SharedAcademicGrade(assignmentId: Int($0.assignmentId), score: $0.score)
        }
        #else
        throw SharedLogicBridgeError.frameworkUnavailable
        #endif
    }

    func courseAttendance(courseID: Int) async throws -> [SharedAcademicAttendance] {
        #if canImport(SharedLogic)
        return try await client.courseAttendance(courseId: Int32(courseID)).map {
            SharedAcademicAttendance(classSessionId: Int($0.classSessionId), isPresent: $0.isPresent)
        }
        #else
        throw SharedLogicBridgeError.frameworkUnavailable
        #endif
    }

    func courseClassSessions(courseID: Int) async throws -> [SharedAcademicClassSession] {
        #if canImport(SharedLogic)
        return try await client.courseClassSessions(courseId: Int32(courseID)).map {
            SharedAcademicClassSession(
                id: Int($0.id),
                courseId: Int($0.courseId),
                startAt: $0.startAt,
                endAt: $0.endAt
            )
        }
        #else
        throw SharedLogicBridgeError.frameworkUnavailable
        #endif
    }
}

func userFacingMessage(for error: Error) -> String {
    let text = error.localizedDescription.lowercased()
    if text.contains("session") || text.contains("autentic") {
        return "Tu sesión expiró. Volvé a conectar con Google."
    }
    if text.contains("canvas") || text.contains("mock") {
        return "No se pudo conectar con Canvas. Revisá la clave y volvé a intentar."
    }
    if text.contains("timeout") || text.contains("tard") {
        return "El servidor tardó más de lo normal en responder. Intentá de nuevo."
    }
    if text.contains("network") || text.contains("conexión") || text.contains("conection") {
        return "Sin conexión. Revisá tu internet e intentá de nuevo."
    }
    return error.localizedDescription.isEmpty ? "Algo salió mal. Intentá de nuevo." : error.localizedDescription
}
