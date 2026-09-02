import Foundation

enum AppTab: Int, CaseIterable, Identifiable {
    case inicio
    case cursos
    case horarios

    var id: Int { rawValue }

    var title: String {
        switch self {
        case .inicio: return "Inicio"
        case .cursos: return "Cursos"
        case .horarios: return "Horarios"
        }
    }

    var systemImage: String {
        switch self {
        case .inicio: return "house"
        case .cursos: return "book.closed"
        case .horarios: return "calendar"
        }
    }
}

enum RootScreen {
    case tabs
    case chat
    case courseDetail
    case notifications
}

enum OnboardingStep {
    case welcome
    case notifications
    case tour
}

enum NotificationPreference: String, CaseIterable, Hashable {
    case laboratorios
    case plazos
    case asistencias

    var title: String {
        switch self {
        case .laboratorios: return "Laboratorios"
        case .plazos: return "Plazos"
        case .asistencias: return "Asistencias"
        }
    }

    var systemImage: String {
        switch self {
        case .laboratorios: return "flask"
        case .plazos: return "calendar"
        case .asistencias: return "person.crop.circle"
        }
    }
}

struct WeekDayData: Identifiable, Equatable {
    let id: String
    let dayOfWeek: String
    let dayNumber: String
    let dateLabel: String
    let isToday: Bool
    let isCritical: Bool

    init(dayOfWeek: String, dayNumber: String, dateLabel: String, isToday: Bool, isCritical: Bool) {
        self.id = dayNumber
        self.dayOfWeek = dayOfWeek
        self.dayNumber = dayNumber
        self.dateLabel = dateLabel
        self.isToday = isToday
        self.isCritical = isCritical
    }
}

enum TaskUrgency: Equatable {
    case normal
    case urgent
    case future
}

struct TaskEntry: Identifiable, Equatable {
    let id = UUID()
    let label: String
    let title: String
    let timestamp: String
    let urgency: TaskUrgency
    let dayNumber: String
    let systemImage: String
}

struct CycleRanking: Identifiable, Equatable {
    let cycle: Int
    let ranking: Double
    var id: Int { cycle }
}

struct HomeScreenData: Equatable {
    var greeting: String
    var dateLabel: String
    var weekTitle: String
    var days: [WeekDayData]
    var tasksHeader: String
    var tasks: [TaskEntry]
    var pendingCount: Int
    var overallRanking: Double
    var careerCode: String
    var currentCycle: Int
    var cycleRankings: [CycleRanking]
    var unreadNotificationsCount: Int

    static let demo = HomeScreenData(
        greeting: "¡Bienvenido, estudiante!",
        dateLabel: "Lunes, 3 de agosto de 2026",
        weekTitle: "Semana 10",
        days: [
            WeekDayData(dayOfWeek: "Lunes", dayNumber: "03", dateLabel: "03/08/26", isToday: true, isCritical: false),
            WeekDayData(dayOfWeek: "Martes", dayNumber: "04", dateLabel: "04/08/26", isToday: false, isCritical: false),
            WeekDayData(dayOfWeek: "Miercoles", dayNumber: "05", dateLabel: "05/08/26", isToday: false, isCritical: false),
            WeekDayData(dayOfWeek: "Jueves", dayNumber: "06", dateLabel: "06/08/26", isToday: false, isCritical: false),
            WeekDayData(dayOfWeek: "Viernes", dayNumber: "07", dateLabel: "07/08/26", isToday: false, isCritical: false),
            WeekDayData(dayOfWeek: "Sabado", dayNumber: "08", dateLabel: "08/08/26", isToday: false, isCritical: true),
            WeekDayData(dayOfWeek: "Domingo", dayNumber: "09", dateLabel: "09/08/26", isToday: false, isCritical: false)
        ],
        tasksHeader: "Tareas Pendientes",
        tasks: [
            TaskEntry(label: "Calculo II · lab", title: "Laboratorio 4", timestamp: "12 h", urgency: .urgent, dayNumber: "03", systemImage: "flask"),
            TaskEntry(label: "Algoritmos · lectura", title: "Lectura: Complejidad algoritmica", timestamp: "18 h", urgency: .normal, dayNumber: "03", systemImage: "book.closed"),
            TaskEntry(label: "Fisica I · practica", title: "Practica 2", timestamp: "1 d", urgency: .normal, dayNumber: "04", systemImage: "doc.text"),
            TaskEntry(label: "Algoritmos · pendiente", title: "Pendiente 2", timestamp: "2 d", urgency: .urgent, dayNumber: "05", systemImage: "doc.text"),
            TaskEntry(label: "Calculo II · lab", title: "Laboratorio 5", timestamp: "2 d", urgency: .normal, dayNumber: "05", systemImage: "flask"),
            TaskEntry(label: "Fisica I · practica", title: "Practica 3", timestamp: "3 d", urgency: .normal, dayNumber: "06", systemImage: "doc.text"),
            TaskEntry(label: "Algoritmos · lectura", title: "Lectura: Arboles balanceados", timestamp: "4 d", urgency: .future, dayNumber: "07", systemImage: "book.closed"),
            TaskEntry(label: "Calculo II · practica", title: "Practica 3", timestamp: "7 d", urgency: .future, dayNumber: "08", systemImage: "doc.text")
        ],
        pendingCount: 8,
        overallRanking: 15.0,
        careerCode: "C-24",
        currentCycle: 6,
        cycleRankings: [
            CycleRanking(cycle: 1, ranking: 14.2),
            CycleRanking(cycle: 2, ranking: 15.6),
            CycleRanking(cycle: 3, ranking: 13.8),
            CycleRanking(cycle: 4, ranking: 16.4),
            CycleRanking(cycle: 5, ranking: 15.1)
        ],
        unreadNotificationsCount: 3
    )
}

struct CourseCardData: Identifiable, Equatable {
    let id: String
    let code: String
    let name: String
    let projectedGrade: String
    let trackHint: String
    let absences: Int
    let absenceLimit: Int
    let nextDueLabel: String
}

struct CoursesScreenData: Equatable {
    let termLabel: String
    let pendingCount: Int
    let courses: [CourseCardData]

    static let demo = CoursesScreenData(
        termLabel: "Ciclo 2026-2",
        pendingCount: 3,
        courses: [
            CourseCardData(id: "calculo-ii", code: "CALC-II", name: "Calculo II", projectedGrade: "14.8", trackHint: "Vas justo. Lab 4 mueve 15%.", absences: 2, absenceLimit: 5, nextDueLabel: "Lab 4 · hoy 23:59"),
            CourseCardData(id: "fisica-i", code: "FIS-I", name: "Fisica I", projectedGrade: "16.2", trackHint: "Encaminado", absences: 1, absenceLimit: 5, nextDueLabel: "Practica 3 · jueves"),
            CourseCardData(id: "algoritmos", code: "ALG-I", name: "Algoritmos", projectedGrade: "12.4", trackHint: "En riesgo si falla el foro", absences: 4, absenceLimit: 5, nextDueLabel: "Foro 2 · manana")
        ]
    )
}

enum ScheduleKind: Equatable {
    case clase
    case entrega
    case falta

    var label: String {
        switch self {
        case .clase: return "CLASE"
        case .entrega: return "ENTREGA"
        case .falta: return "FALTA"
        }
    }
}

struct ScheduleEvent: Identifiable, Equatable {
    let id = UUID()
    let time: String
    let title: String
    let subtitle: String
    let kind: ScheduleKind
}

struct ScheduleDay: Identifiable, Equatable {
    let id = UUID()
    let label: String
    let events: [ScheduleEvent]
}

struct ScheduleScreenData: Equatable {
    let weekLabel: String
    let pendingCount: Int
    let days: [ScheduleDay]

    static let demo = ScheduleScreenData(
        weekLabel: "Semana 10 · 3 al 9 ago",
        pendingCount: 3,
        days: [
            ScheduleDay(label: "Lunes 3", events: [
                ScheduleEvent(time: "08:00", title: "Calculo II", subtitle: "Aula 302", kind: .clase),
                ScheduleEvent(time: "23:59", title: "Lab 4 — Calculo II", subtitle: "Aula virtual", kind: .entrega)
            ]),
            ScheduleDay(label: "Martes 4", events: [
                ScheduleEvent(time: "10:00", title: "Fisica I", subtitle: "Lab B", kind: .clase),
                ScheduleEvent(time: "10:00", title: "Fisica I", subtitle: "No marcaste asistencia", kind: .falta)
            ]),
            ScheduleDay(label: "Jueves 6", events: [
                ScheduleEvent(time: "14:00", title: "Algoritmos", subtitle: "Aula 110", kind: .clase),
                ScheduleEvent(time: "23:59", title: "Practica 3 — Fisica I", subtitle: "Correo del docente", kind: .entrega)
            ]),
            ScheduleDay(label: "Viernes 7", events: [
                ScheduleEvent(time: "18:00", title: "Foro 2 — Algoritmos", subtitle: "Aula virtual", kind: .entrega)
            ])
        ]
    )
}

struct AttendanceEntry: Identifiable, Equatable {
    let id = UUID()
    let dateLabel: String
    let isPresent: Bool
}

enum EvaluationState: Equatable {
    case graded
    case pending
    case missing

    var label: String {
        switch self {
        case .graded: return "Calificado"
        case .pending: return "Pendiente"
        case .missing: return "No entregado"
        }
    }
}

struct EvaluationItem: Identifiable, Equatable {
    let id = UUID()
    let name: String
    let weightLabel: String
    let scoresLabel: String
    let state: EvaluationState
    let detail: String
}

struct CourseDetailData: Equatable {
    let course: CourseCardData
    let headline: String
    let projectionHint: String
    let attendance: [AttendanceEntry]
    let practices: [EvaluationItem]
    let labs: [EvaluationItem]
    let pending: [EvaluationItem]
    let pendingTitle: String

    static func forCourse(_ course: CourseCardData) -> CourseDetailData {
        CourseDetailData(
            course: course,
            headline: "Notas y faltas de este curso",
            projectionHint: course.trackHint,
            attendance: [
                AttendanceEntry(dateLabel: "04/08/26", isPresent: true),
                AttendanceEntry(dateLabel: "11/08/26", isPresent: course.absences == 0)
            ],
            practices: [
                EvaluationItem(name: "Practica 1", weightLabel: "10%", scoresLabel: "15 / 16", state: .graded, detail: "Evaluacion registrada en el curso."),
                EvaluationItem(name: "Practica 2", weightLabel: "10%", scoresLabel: "Pendiente", state: .pending, detail: "Todavia no hay una calificacion registrada.")
            ],
            labs: [
                EvaluationItem(name: "Lab 1", weightLabel: "8%", scoresLabel: "17 / 14", state: .graded, detail: "Evaluacion registrada en el curso."),
                EvaluationItem(name: "Lab 2", weightLabel: "8%", scoresLabel: course.nextDueLabel, state: .pending, detail: "Revisa la fecha de entrega en el aula virtual.")
            ],
            pending: [],
            pendingTitle: "Pendientes"
        )
    }
}

enum NotificationSeverity: Equatable {
    case info
    case urgent
}

struct NotificationInboxItem: Identifiable, Equatable {
    let id: String
    let title: String
    let body: String
    let timestamp: String
    let severity: NotificationSeverity
    let systemImage: String
}

enum MessageRole: Equatable {
    case bot
    case user
    case system
}

enum MessageStatus: Equatable {
    case sent
    case delivered
    case read
}

struct ChatMessage: Identifiable, Equatable {
    let id = UUID()
    let role: MessageRole
    let body: String
    let footnote: String?
    let timestamp: String?
    let status: MessageStatus?
}
