import Foundation

enum AppTab: String, CaseIterable, Identifiable {
    case home
    case courses
    case schedule

    var id: String { rawValue }

    var title: String {
        switch self {
        case .home: return "Inicio"
        case .courses: return "Cursos"
        case .schedule: return "Horarios"
        }
    }

    var iconName: String {
        switch self {
        case .home: return "house"
        case .courses: return "book.closed"
        case .schedule: return "calendar"
        }
    }
}

enum OnboardingStep: Int, CaseIterable {
    case welcome
    case notifications
    case tour
}

enum AppRoute: Hashable {
    case chat
    case notifications
    case courseDetail(Int)
}

enum TaskUrgency: Hashable {
    case normal
    case urgent
    case future
}

struct WeekDayItem: Identifiable, Hashable {
    let id: String
    let shortLabel: String
    let dateLabel: String
    let isToday: Bool
    let hasTask: Bool
    let isCritical: Bool
}

struct TaskItem: Identifiable, Hashable {
    let id: String
    let label: String
    let title: String
    let timestamp: String
    let urgency: TaskUrgency
}

struct HomeData: Hashable {
    let dateLabel: String
    let average: String
    let absences: String
    let dueThisWeek: String
    let days: [WeekDayItem]
    let tasks: [TaskItem]
}

struct CourseItem: Identifiable, Hashable {
    let id: Int
    let code: String
    let name: String
    let projectedGrade: String
    let trackHint: String
    let absences: Int
    let absenceLimit: Int
    let nextDueLabel: String
}

enum EvaluationState: Hashable {
    case graded
    case pending
    case missing
}

struct EvaluationItem: Identifiable, Hashable {
    let id: String
    let name: String
    let weightLabel: String
    let scoresLabel: String
    let state: EvaluationState
}

struct CourseDetailData: Hashable {
    let course: CourseItem
    let headline: String
    let projectionHint: String
    let practices: [EvaluationItem]
    let labs: [EvaluationItem]
    let forums: [EvaluationItem]
}

enum ScheduleKind: Hashable {
    case classSession
    case assignment
    case absence

    var label: String {
        switch self {
        case .classSession: return "Clase"
        case .assignment: return "Entrega"
        case .absence: return "Falta"
        }
    }
}

struct ScheduleEvent: Identifiable, Hashable {
    let id: String
    let time: String
    let title: String
    let subtitle: String
    let kind: ScheduleKind
}

struct ScheduleDay: Identifiable, Hashable {
    let id: String
    let label: String
    let events: [ScheduleEvent]
}

struct ScheduleData: Hashable {
    let weekLabel: String
    let days: [ScheduleDay]
}

enum NotificationSeverity: Hashable {
    case info
    case urgent
}

struct NotificationItem: Identifiable, Hashable {
    let id: String
    let title: String
    let body: String
    let timestamp: String
    let severity: NotificationSeverity
}

enum NotificationPreference: String, CaseIterable, Hashable {
    case assignments = "Entregas"
    case grades = "Notas"
    case attendance = "Asistencia"
    case reminders = "Recordatorios"
}

enum ChatRole: Hashable {
    case user
    case bot
    case system
}

struct ChatMessage: Identifiable, Hashable {
    let id: UUID
    let role: ChatRole
    let body: String
    let timestamp: Date

    init(id: UUID = UUID(), role: ChatRole, body: String, timestamp: Date = Date()) {
        self.id = id
        self.role = role
        self.body = body
        self.timestamp = timestamp
    }
}
