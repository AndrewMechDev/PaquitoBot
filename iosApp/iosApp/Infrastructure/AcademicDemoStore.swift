import Foundation

/// Adaptador iOS para el mismo catálogo académico demo que usa Android.
///
/// Mantiene el modelo de UI independiente del JSON y deja un fallback hacia
/// `DemoAcademicData` para que previews y builds sin recursos sigan siendo
/// utilizables. No contiene credenciales ni realiza llamadas de red.
enum AcademicDemoStore {
    private static let snapshot = loadSnapshot()

    static let courses: [CourseItem] = {
        guard let snapshot else { return DemoAcademicData.courses }
        return makeCourses(from: snapshot)
    }()

    static let home: HomeData = {
        guard let snapshot else { return DemoAcademicData.home }
        return makeHome(from: snapshot)
    }()

    static let schedule: ScheduleData = {
        guard let snapshot else { return DemoAcademicData.schedule }
        return makeSchedule(from: snapshot)
    }()

    static let notifications: [NotificationItem] = {
        guard let snapshot else { return DemoAcademicData.notifications }
        return makeNotifications(from: snapshot)
    }()

    static let calendarTasks: [CalendarTask] = {
        guard let snapshot else { return DemoAcademicData.calendarTasks }
        return makeCalendarTasks(from: snapshot)
    }()

    static let procedures: [ProcedureItem] = loadProcedures()

    static var isLoaded: Bool { snapshot != nil }

    /// Serializa el escenario que la UI iOS muestra para enviarlo al prompt de MiniMax.
    /// La forma coincide con `AcademicContextSnapshot` de Android.
    static func minimaxContextJSON() -> String {
        let today = todayKey()
        let courses: [[String: Any]] = snapshot?.catalog.courses.map { course in
            let assignments = snapshot?.catalog.assignments.filter { $0.courseID == course.id } ?? []
            let grades = Dictionary(
                uniqueKeysWithValues: (snapshot?.scenario.grades ?? []).map { ($0.assignmentID, $0.score) }
            )
            let sessions = Dictionary(
                uniqueKeysWithValues: (snapshot?.catalog.classSessions ?? [])
                    .filter { $0.courseID == course.id }
                    .map { ($0.id, $0) }
            )
            let attendance = (snapshot?.scenario.attendance ?? []).compactMap { record -> [String: Any]? in
                guard let session = sessions[record.classSessionID] else { return nil }
                return [
                    "sesion_id": session.id,
                    "fecha": session.startAt ?? NSNull(),
                    "tipo": session.kind,
                    "estado": record.status == "absent" ? "falta" : "presente"
                ]
            }
            let tasks = assignments.map { assignment -> [String: Any] in
                let dueDay = assignment.dueAt?.prefix(10)
                let status: String
                if grades[assignment.id] != nil {
                    status = "calificada"
                } else if let dueDay, String(dueDay) < today {
                    status = "vencida"
                } else if assignment.dueAt == nil {
                    status = "sin_fecha"
                } else {
                    status = "pendiente"
                }

                return [
                    "id": assignment.id,
                    "nombre": assignment.name,
                    "tipo": assignment.kind ?? "other",
                    "fecha_entrega": assignment.dueAt ?? NSNull(),
                    "estado": status,
                    "nota": grades[assignment.id] ?? NSNull(),
                    "nota_maxima": assignment.pointsPossible
                ]
            }

            return [
                "codigo": course.code,
                "nombre": course.name,
                "faltas": attendance.filter { ($0["estado"] as? String) == "falta" }.count,
                "limite_faltas_demo": 8,
                "tareas_y_notas": tasks,
                "asistencia": attendance
            ]
        } ?? []

        let payload: [String: Any] = [
            "fuente": "Escenario académico de demostración asignado a esta cuenta de Clerk",
            "fecha_actual": today,
            "cursos": courses
        ]

        guard JSONSerialization.isValidJSONObject(payload),
              let data = try? JSONSerialization.data(withJSONObject: payload, options: []) else {
            return "{}"
        }
        return String(data: data, encoding: .utf8) ?? "{}"
    }

    static func detail(for course: CourseItem) -> CourseDetailData {
        guard let snapshot else { return DemoAcademicData.detail(for: course) }

        let assignments = snapshot.catalog.assignments.filter { $0.courseID == course.id }
        let gradeByAssignment = Dictionary(uniqueKeysWithValues: snapshot.scenario.grades.map {
            ($0.assignmentID, $0.score)
        })
        let evaluations = assignments.map { assignment in
            makeEvaluation(
                assignment: assignment,
                score: gradeByAssignment[assignment.id]
            )
        }

        return CourseDetailData(
            course: course,
            headline: "Tu promedio actual es \(course.projectedGrade)",
            projectionHint: "Consulta cada actividad para revisar su estado y puntaje.",
            practices: evaluations.filter { $0.category == .practice }.map(\.item),
            labs: evaluations.filter { $0.category == .laboratory }.map(\.item),
            forums: evaluations.filter { $0.category == .other }.map(\.item)
        )
    }

    private static func loadSnapshot() -> Snapshot? {
        guard
            let catalogURL = Bundle.main.url(
                forResource: "catalog",
                withExtension: "json"
            ),
            let scenarioURL = Bundle.main.url(
                forResource: "scenario-01",
                withExtension: "json"
            ),
            let catalogData = try? Data(contentsOf: catalogURL),
            let scenarioData = try? Data(contentsOf: scenarioURL),
            let catalog = try? JSONDecoder().decode(AcademicCatalog.self, from: catalogData),
            let scenario = try? JSONDecoder().decode(AcademicScenario.self, from: scenarioData)
        else {
            return nil
        }

        return Snapshot(catalog: catalog, scenario: scenario)
    }

    private static func loadProcedures() -> [ProcedureItem] {
        guard
            let url = Bundle.main.url(forResource: "procedures", withExtension: "json"),
            let data = try? Data(contentsOf: url),
            let catalog = try? JSONDecoder().decode(ProcedureCatalog.self, from: data)
        else {
            return DemoAcademicData.procedures
        }

        return catalog.procedures.map { procedure in
            ProcedureItem(
                id: procedure.id,
                code: procedure.code,
                title: procedure.title,
                category: procedure.category.localizedLabel,
                audience: procedure.audience,
                summary: procedure.summary,
                requirements: procedure.requirements,
                steps: procedure.steps,
                cost: procedure.cost,
                responseTime: procedure.responseTime,
                applicationWindow: procedure.applicationWindow,
                duration: procedure.duration,
                responsibleArea: procedure.responsibleArea,
                channels: procedure.channels.enumerated().map { index, channel in
                    ProcedureChannel(
                        id: "\(procedure.id)-channel-\(index)",
                        label: channel.label,
                        target: channel.target,
                        kind: channel.kind == "Email" ? .email : .web
                    )
                },
                notes: procedure.notes,
                sourcePage: procedure.sourcePage,
                source: ProcedureSource(
                    title: catalog.source.title,
                    url: catalog.source.url.flatMap(URL.init(string:)),
                    edition: catalog.source.edition,
                    reviewedOn: catalog.source.reviewedOn
                )
            )
        }
    }

    private static func makeCourses(from snapshot: Snapshot) -> [CourseItem] {
        snapshot.catalog.courses.map { course in
            let assignments = snapshot.catalog.assignments.filter { $0.courseID == course.id }
            let assignmentIDs = Set(assignments.map(\.id))
            let grades = snapshot.scenario.grades.filter { assignmentIDs.contains($0.assignmentID) }
            let average = grades.isEmpty ? nil : grades.map(\.score).reduce(0, +) / Double(grades.count)
            let sessionIDs = Set(snapshot.catalog.classSessions.filter { $0.courseID == course.id }.map(\.id))
            let absences = snapshot.scenario.attendance.filter {
                sessionIDs.contains($0.classSessionID) && $0.status == "absent"
            }.count
            let pendingAssignment = assignments
                .filter { assignment in
                    !grades.contains { grade in grade.assignmentID == assignment.id }
                }
                .compactMap { assignment -> (AcademicAssignment, Date)? in
                    guard let date = parseDate(assignment.dueAt) else { return nil }
                    return (assignment, date)
                }
                .min { $0.1 < $1.1 }

            return CourseItem(
                id: course.id,
                code: course.code,
                name: course.name,
                projectedGrade: average.map { String(format: "%.1f", $0) } ?? "—",
                trackHint: trackHint(for: average),
                absences: absences,
                absenceLimit: 8,
                nextDueLabel: pendingAssignment.map {
                    "\(assignmentKindLabel($0.0)) · \(dueLabel($0.1))"
                } ?? "Sin entregas pendientes"
            )
        }
    }

    private static func makeHome(from snapshot: Snapshot) -> HomeData {
        let base = DemoAcademicData.home
        let gradeIDs = Set(snapshot.scenario.grades.map(\.assignmentID))
        let courseNames = Dictionary(uniqueKeysWithValues: snapshot.catalog.courses.map { ($0.id, $0.name) })
        let pending = snapshot.catalog.assignments
            .filter { !gradeIDs.contains($0.id) }
            .compactMap { assignment -> (AcademicAssignment, Date)? in
                guard let date = parseDate(assignment.dueAt) else { return nil }
                return (assignment, date)
            }
            .sorted { $0.1 < $1.1 }

        let selected = Array(pending.prefix(8))
        let taskDates = Array(Set(selected.map { dayStart($0.1) })).sorted()
        let days = taskDates.map { date in
            let dayEvents = selected.filter { dayStart($0.1) == date }
            return WeekDayItem(
                id: String(Int(date.timeIntervalSince1970)),
                shortLabel: weekdayLabel(date),
                dateLabel: dayNumberLabel(date),
                isToday: dayStart(Date()) == date,
                hasTask: !dayEvents.isEmpty,
                isCritical: dayEvents.contains { $0.1 <= Date().addingTimeInterval(86_400) }
            )
        }
        let tasks = selected.map { assignment, date in
            let courseName = courseNames[assignment.courseID] ?? "Curso"
            let kind = assignmentKindLabel(assignment).uppercased()
            return TaskItem(
                id: "assignment-\(assignment.id)",
                label: "\(courseName) · \(kind)",
                title: assignment.name,
                timestamp: dueLabel(date),
                urgency: taskUrgency(for: date),
                dayID: String(Int(dayStart(date).timeIntervalSince1970))
            )
        }

        guard !days.isEmpty, !tasks.isEmpty else { return base }
        return HomeData(
            dateLabel: dateLabel(Date()),
            weekTitle: "\(snapshot.catalog.term) · próximas entregas",
            days: days,
            tasks: tasks,
            overallRanking: base.overallRanking,
            careerCode: base.careerCode,
            currentCycle: base.currentCycle,
            cycleRankings: base.cycleRankings,
            importantNotices: base.importantNotices,
            importantNoticeCount: base.importantNoticeCount
        )
    }

    private static func makeNotifications(from snapshot: Snapshot) -> [NotificationItem] {
        let gradeIDs = Set(snapshot.scenario.grades.map(\.assignmentID))
        let courseNames = Dictionary(uniqueKeysWithValues: snapshot.catalog.courses.map { ($0.id, $0.name) })
        let createdAt = Date()
        let pending = snapshot.catalog.assignments
            .filter { !gradeIDs.contains($0.id) }
            .compactMap { assignment -> (AcademicAssignment, Date)? in
                guard let date = parseDate(assignment.dueAt) else { return nil }
                return (assignment, date)
            }
            .sorted { $0.1 < $1.1 }

        let pendingNotifications = pending.prefix(6).map { assignment, date in
            let courseName = courseNames[assignment.courseID] ?? "Curso"
            let isUrgent = date <= Date().addingTimeInterval(86_400)
            return NotificationItem(
                id: "academic-assignment-\(assignment.id)",
                title: isUrgent ? "Entrega próxima" : "Nueva entrega pendiente",
                body: "\(assignment.name) de \(courseName) vence \(dueLabel(date).lowercased()).",
                timestamp: notificationTimestamp(for: date),
                severity: isUrgent ? .urgent : .info,
                category: assignment.kind == "laboratory" ? .laboratories : .deadlines,
                date: createdAt
            )
        }

        let laboratoryGrades = snapshot.scenario.grades.compactMap { grade -> NotificationItem? in
            guard
                let assignment = snapshot.catalog.assignments.first(where: { $0.id == grade.assignmentID }),
                assignment.kind == "laboratory"
            else { return nil }
            let courseName = courseNames[assignment.courseID] ?? "Curso"
            return NotificationItem(
                id: "academic-lab-grade-\(assignment.id)",
                title: "Laboratorio calificado",
                body: "\(assignment.name) de \(courseName) tiene una calificación disponible: \(numberLabel(grade.score))/\(numberLabel(assignment.pointsPossible)).",
                timestamp: "Reciente",
                severity: .info,
                category: .laboratories,
                date: createdAt
            )
        }

        let sessions = Dictionary(uniqueKeysWithValues: snapshot.catalog.classSessions.map { ($0.id, $0) })
        let attendanceNotices = snapshot.scenario.attendance.compactMap { record -> NotificationItem? in
            guard record.status == "absent", let session = sessions[record.classSessionID] else { return nil }
            let courseName = courseNames[session.courseID] ?? "Curso"
            let sessionDate = parseDate(session.endAt ?? session.startAt)
            return NotificationItem(
                id: "academic-absence-\(record.classSessionID)",
                title: "Inasistencia registrada",
                body: "Se registró una inasistencia en \(courseName). Revisa tu asistencia en PaquitoBot.",
                timestamp: sessionDate.map(notificationTimestamp(for:)) ?? "Reciente",
                severity: .urgent,
                category: .attendance,
                date: createdAt
            )
        }

        return (pendingNotifications + laboratoryGrades + attendanceNotices)
            .sorted { ($0.date ?? .distantPast) > ($1.date ?? .distantPast) }
    }

    private static func makeCalendarTasks(from snapshot: Snapshot) -> [CalendarTask] {
        let gradeIDs = Set(snapshot.scenario.grades.map(\.assignmentID))
        let courseNames = Dictionary(uniqueKeysWithValues: snapshot.catalog.courses.map { ($0.id, $0.name) })
        return snapshot.catalog.assignments
            .filter { !gradeIDs.contains($0.id) }
            .compactMap { assignment in
                guard let dueAt = assignment.dueAt, let date = parseDate(dueAt) else { return nil }
                return CalendarTask(
                    id: "academic-calendar-\(assignment.id)",
                    title: assignment.name,
                    course: courseNames[assignment.courseID] ?? "Curso",
                    dueLabel: dateTimeLabel(dueAt),
                    isPast: date < Date(),
                    dueDate: date,
                    notificationCategory: assignment.kind == "laboratory" ? .laboratories : .deadlines
                )
            }
            .sorted { $0.dueLabel < $1.dueLabel }
    }

    private static func makeSchedule(from snapshot: Snapshot) -> ScheduleData {
        let courseNames = Dictionary(uniqueKeysWithValues: snapshot.catalog.courses.map { ($0.id, $0.name) })
        let gradeIDs = Set(snapshot.scenario.grades.map(\.assignmentID))
        let sessions = snapshot.catalog.classSessions.compactMap { session -> AcademicScheduleEvent? in
            guard let date = parseDate(session.startAt), let courseName = courseNames[session.courseID] else {
                return nil
            }
            return AcademicScheduleEvent(
                date: dayStart(date),
                event: ScheduleEvent(
                    id: "session-\(session.id)",
                    time: timeLabel(date),
                    title: courseName,
                    subtitle: session.kind == "laboratory" ? "Laboratorio · Clase" : "Clase programada",
                    kind: .classSession
                )
            )
        }
        let assignments = snapshot.catalog.assignments.compactMap { assignment -> AcademicScheduleEvent? in
            guard
                !gradeIDs.contains(assignment.id),
                let date = parseDate(assignment.dueAt),
                let courseName = courseNames[assignment.courseID]
            else {
                return nil
            }
            return AcademicScheduleEvent(
                date: dayStart(date),
                event: ScheduleEvent(
                    id: "assignment-\(assignment.id)",
                    time: timeLabel(date),
                    title: assignment.name,
                    subtitle: "\(courseName) · Entrega",
                    kind: .assignment
                )
            )
        }

        let candidates = (sessions + assignments).sorted { $0.date < $1.date }
        let dates = selectedScheduleDates(from: candidates)
        let days = dates.map { date in
            ScheduleDay(
                id: String(Int(date.timeIntervalSince1970)),
                label: dayLabel(date),
                events: candidates
                    .filter { $0.date == date }
                    .map(\.event)
                    .sorted { $0.time < $1.time }
            )
        }

        return ScheduleData(
            weekLabel: "\(snapshot.catalog.term) · próximas actividades",
            days: days.isEmpty ? DemoAcademicData.schedule.days : days
        )
    }

    private static func selectedScheduleDates(from events: [AcademicScheduleEvent]) -> [Date] {
        let today = dayStart(Date())
        let future = Array(Set(events.map(\.date).filter { $0 >= today })).sorted()
        let dates = future.isEmpty ? Array(Set(events.map(\.date))).sorted() : future
        return Array(dates.prefix(6))
    }

    private static func makeEvaluation(
        assignment: AcademicAssignment,
        score: Double?
    ) -> AcademicEvaluation {
        let state: EvaluationState
        if score != nil {
            state = .graded
        } else if let dueAt = parseDate(assignment.dueAt), dueAt < Date() {
            state = .missing
        } else {
            state = .pending
        }

        let category: AcademicEvaluation.Category
        let normalizedName = assignment.name.lowercased()
        if normalizedName.contains("laboratorio") {
            category = .laboratory
        } else if normalizedName.contains("practica") || normalizedName.contains("práctica") {
            category = .practice
        } else {
            category = .other
        }

        return AcademicEvaluation(
            category: category,
            item: EvaluationItem(
                id: "assignment-\(assignment.id)",
                name: assignment.name,
                weightLabel: "\(numberLabel(assignment.pointsPossible)) pts",
                scoresLabel: score.map {
                    "\(numberLabel($0)) / \(numberLabel(assignment.pointsPossible))"
                } ?? (state == .missing ? "Sin entregar" : "Pendiente"),
                state: state
            )
        )
    }

    private static func trackHint(for average: Double?) -> String {
        guard let average else { return "Sin calificaciones" }
        if average >= 16 { return "Excelente" }
        if average >= 14 { return "Vas bien" }
        return "Puedes mejorar"
    }

    private static func assignmentKindLabel(_ assignment: AcademicAssignment) -> String {
        assignment.kind == "laboratory" ? "Laboratorio" : "Entrega"
    }

    private static func dueLabel(_ date: Date) -> String {
        let calendar = academicCalendar
        let today = calendar.startOfDay(for: Date())
        let dueDay = calendar.startOfDay(for: date)
        if dueDay == today { return "Hoy" }
        if let tomorrow = calendar.date(byAdding: .day, value: 1, to: today), dueDay == tomorrow {
            return "Mañana"
        }
        return shortDateLabel(date)
    }

    private static func parseDate(_ value: String?) -> Date? {
        guard let value else { return nil }
        return ISO8601DateFormatter().date(from: value)
    }

    private static func dayStart(_ date: Date) -> Date {
        academicCalendar.startOfDay(for: date)
    }

    private static func timeLabel(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = academicTimeZone
        formatter.dateFormat = "HH:mm"
        return formatter.string(from: date)
    }

    private static func dayLabel(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_PE")
        formatter.timeZone = academicTimeZone
        formatter.dateFormat = "EEEE d"
        return formatter.string(from: date).capitalized(with: formatter.locale)
    }

    private static func shortDateLabel(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_PE")
        formatter.timeZone = academicTimeZone
        formatter.dateFormat = "d MMM"
        return formatter.string(from: date)
    }

    private static func dateLabel(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_PE")
        formatter.timeZone = academicTimeZone
        formatter.dateFormat = "EEEE, d 'de' MMMM 'de' yyyy"
        let value = formatter.string(from: date)
        guard let first = value.first else { return value }
        return first.uppercased() + value.dropFirst()
    }

    private static func dayNumberLabel(_ date: Date) -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = academicTimeZone
        formatter.dateFormat = "dd"
        return formatter.string(from: date)
    }

    private static func weekdayLabel(_ date: Date) -> String {
        let labels = ["Do", "Lu", "Ma", "Mi", "Ju", "Vi", "Sa"]
        return labels[academicCalendar.component(.weekday, from: date) - 1]
    }

    private static func taskUrgency(for date: Date) -> TaskUrgency {
        let now = Date()
        if date <= now.addingTimeInterval(86_400) { return .urgent }
        if date <= now.addingTimeInterval(259_200) { return .normal }
        return .future
    }

    private static func notificationTimestamp(for date: Date) -> String {
        let calendar = academicCalendar
        let today = calendar.startOfDay(for: Date())
        let target = calendar.startOfDay(for: date)
        if target == today { return "Hoy" }
        if let tomorrow = calendar.date(byAdding: .day, value: 1, to: today), target == tomorrow {
            return "Mañana"
        }
        let days = calendar.dateComponents([.day], from: today, to: target).day ?? 0
        return days > 0 ? "En \(days) d" : "Pendiente"
    }

    private static func dateTimeLabel(_ value: String) -> String {
        guard let date = parseDate(value) else { return value }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "es_PE")
        formatter.timeZone = academicTimeZone
        formatter.dateFormat = "d MMM · HH:mm"
        return formatter.string(from: date)
    }

    private static func numberLabel(_ value: Double) -> String {
        value.rounded() == value ? String(format: "%.0f", value) : String(format: "%.1f", value)
    }

    private static func todayKey() -> String {
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = academicTimeZone
        formatter.dateFormat = "yyyy-MM-dd"
        return formatter.string(from: Date())
    }

    private static let academicTimeZone = TimeZone(identifier: "America/Lima")!
    private static let academicCalendar: Calendar = {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = academicTimeZone
        return calendar
    }()
}

private struct Snapshot {
    let catalog: AcademicCatalog
    let scenario: AcademicScenario
}

private struct AcademicCatalog: Decodable {
    let term: String
    let courses: [AcademicCourse]
    let assignments: [AcademicAssignment]
    let classSessions: [AcademicClassSession]

    enum CodingKeys: String, CodingKey {
        case term, courses, assignments
        case classSessions = "class_sessions"
    }
}

private struct AcademicCourse: Decodable {
    let id: Int
    let code: String
    let name: String
}

private struct AcademicAssignment: Decodable {
    let id: Int
    let courseID: Int
    let name: String
    let kind: String?
    let pointsPossible: Double
    let dueAt: String?

    enum CodingKeys: String, CodingKey {
        case id, name, kind
        case courseID = "course_id"
        case pointsPossible = "points_possible"
        case dueAt = "due_at"
    }
}

private struct AcademicClassSession: Decodable {
    let id: Int
    let courseID: Int
    let startAt: String?
    let endAt: String?
    let kind: String

    enum CodingKeys: String, CodingKey {
        case id, kind
        case courseID = "course_id"
        case startAt = "start_at"
        case endAt = "end_at"
    }
}

private struct AcademicScenario: Decodable {
    let grades: [AcademicGrade]
    let attendance: [AcademicAttendance]
}

private struct AcademicGrade: Decodable {
    let assignmentID: Int
    let score: Double

    enum CodingKeys: String, CodingKey {
        case score
        case assignmentID = "assignment_id"
    }
}

private struct AcademicAttendance: Decodable {
    let classSessionID: Int
    let status: String

    enum CodingKeys: String, CodingKey {
        case status
        case classSessionID = "class_session_id"
    }
}

private struct AcademicScheduleEvent {
    let date: Date
    let event: ScheduleEvent
}

private struct AcademicEvaluation {
    enum Category {
        case practice
        case laboratory
        case other
    }

    let category: Category
    let item: EvaluationItem
}

private struct ProcedureCatalog: Decodable {
    let source: ProcedureCatalogSource
    let procedures: [ProcedureRecord]
}

private struct ProcedureCatalogSource: Decodable {
    let title: String
    let url: String?
    let edition: String
    let reviewedOn: String
}

private struct ProcedureRecord: Decodable {
    let id: String
    let code: String
    let title: String
    let category: String
    let audience: String
    let summary: String
    let requirements: [String]
    let steps: [String]
    let cost: String
    let responseTime: String
    let sourcePage: Int?
    let responsibleArea: String
    let channels: [ProcedureChannelRecord]
    let applicationWindow: String?
    let notes: [String]
    let duration: String?
}

private struct ProcedureChannelRecord: Decodable {
    let label: String
    let target: String
    let kind: String
}

private extension String {
    var localizedLabel: String {
        switch self {
        case "Admission": return "Admisión"
        case "Documents": return "Documentos"
        case "Enrollment": return "Matrícula"
        case "Finance": return "Finanzas"
        case "Graduation": return "Graduación"
        case "Practices": return "Prácticas"
        case "Transfers": return "Traslados"
        default: return self
        }
    }
}
