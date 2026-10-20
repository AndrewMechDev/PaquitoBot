import Foundation

enum AppTab: String, CaseIterable, Identifiable {
    case home
    case courses
    case schedule
    case profile

    var id: String { rawValue }

    var title: String {
        switch self {
        case .home: return "Inicio"
        case .courses: return "Cursos"
        case .schedule: return "Horarios"
        case .profile: return "Perfil"
        }
    }

    var iconName: String {
        switch self {
        case .home: return "house"
        case .courses: return "book.closed"
        case .schedule: return "calendar"
        case .profile: return "person.crop.circle"
        }
    }
}

enum OnboardingStep: Int, CaseIterable {
    case intro
    case welcome
    case notifications
    case tour
}

enum AppRoute: Hashable {
    case chat
    case notifications
    case courseDetail(Int)
    case calendar
    case procedures
    case procedureDetail(String)
    case enrollment
    case events
    case eventDetail(String)
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
    let dayID: String?
}

struct HomeData: Hashable {
    let dateLabel: String
    let weekTitle: String
    let days: [WeekDayItem]
    let tasks: [TaskItem]
    let overallRanking: Double
    let careerCode: String
    let currentCycle: Int
    let cycleRankings: [CycleRanking]
    let importantNotices: [HomeFocusItem]
    let importantNoticeCount: Int
}

struct HomeFocusItem: Identifiable, Hashable {
    let id: String
    let title: String
    let detail: String
}

struct CycleRanking: Identifiable, Hashable {
    let cycle: Int
    let ranking: Double

    var id: Int { cycle }
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

struct ProfileData: Hashable {
    let name: String
    let email: String
}

enum ProcedureChannelKind: Hashable {
    case web
    case email
}

struct ProcedureChannel: Identifiable, Hashable {
    let id: String
    let label: String
    let target: String
    let kind: ProcedureChannelKind
}

struct ProcedureItem: Identifiable, Hashable {
    let id: String
    let code: String
    let title: String
    let category: String
    let summary: String
    let requirements: [String]
    let steps: [String]
    let cost: String
    let responseTime: String
    let responsibleArea: String
    let channels: [ProcedureChannel]
}

struct EnrollmentStep: Identifiable, Hashable {
    let id: String
    let title: String
    let body: [String]
    let optional: Bool
}

struct CampusEvent: Identifiable, Hashable {
    let id: String
    let title: String
    let dateLabel: String
    let location: String
    let description: String
    let sourceName: String
    let sourceURL: URL?
}

struct CalendarTask: Identifiable, Hashable {
    let id: String
    let title: String
    let course: String
    let dueLabel: String
    let isPast: Bool
}

struct CalendarSyncSummary: Hashable {
    let created: Int
    let updated: Int
    let unchanged: Int
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
    case laboratories = "Laboratorios calificados"
    case deadlines = "Plazos de entrega"
    case attendance = "Asistencias"

    var storageKey: String {
        switch self {
        case .laboratories: return "laboratories"
        case .deadlines: return "deadlines"
        case .attendance: return "attendance"
        }
    }

    var systemImage: String {
        switch self {
        case .laboratories: return "flask"
        case .deadlines: return "calendar.badge.clock"
        case .attendance: return "person.crop.circle.badge.checkmark"
        }
    }
}

struct NotificationSetup: Hashable {
    var enabled: Set<NotificationPreference>
    var reminderHours: Int
    var dailySummaryEnabled: Bool
    var summaryHour: Int
    var summaryMinute: Int

    static let `default` = NotificationSetup(
        enabled: [.laboratories, .deadlines],
        reminderHours: 24,
        dailySummaryEnabled: true,
        summaryHour: 8,
        summaryMinute: 0
    )
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
