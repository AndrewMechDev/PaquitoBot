import Foundation
import Observation

@MainActor
@Observable
final class CourseDetailViewModel {
    private let bridge: SharedLogicBridge
    private let course: CourseCardData

    var data: CourseDetailData
    var isLoading = false
    var errorMessage: String?

    init(bridge: SharedLogicBridge, course: CourseCardData) {
        self.bridge = bridge
        self.course = course
        data = .forCourse(course)
    }

    func load() {
        guard let courseID = Int(course.id), !isLoading else { return }
        isLoading = true
        Task { @MainActor in
            defer { isLoading = false }
            let sessions = (try? await bridge.courseClassSessions(courseID: courseID)) ?? []
            let attendance = (try? await bridge.courseAttendance(courseID: courseID)) ?? []
            let assignments: [SharedAcademicAssignment]
            do {
                assignments = try await bridge.courseAssignments(courseID: courseID)
                errorMessage = nil
            } catch {
                errorMessage = "No se pudo cargar canvas-mock: \(error.localizedDescription)"
                return
            }
            let grades = (try? await bridge.courseGrades(courseID: courseID)) ?? []
            let sessionDates = Dictionary(uniqueKeysWithValues: sessions.compactMap { session in
                session.startAt.map { (session.id, AcademicDateFormatter.dateKey(from: $0)) }
            })
            let attendanceEntries = attendance.compactMap { record -> AttendanceEntry? in
                guard let dateKey = sessionDates[record.classSessionId] else { return nil }
                return AttendanceEntry(dateLabel: AcademicDateFormatter.shortDateLabel(dateKey), isPresent: record.isPresent)
            }.sorted { $0.dateLabel < $1.dateLabel }
            let gradesByID = Dictionary(uniqueKeysWithValues: grades.map { ($0.assignmentId, $0) })
            let evaluations = assignments.map { assignment in
                let grade = gradesByID[assignment.id]
                let dueKey = assignment.dueAt.map(AcademicDateFormatter.dateKey(from:))
                let score = grade.map { String(format: "%.0f / %.0f", $0.score, assignment.pointsPossible) }
                    ?? dueKey.map { "Vence \(AcademicDateFormatter.shortDateLabel($0))" }
                    ?? "Sin fecha"
                return EvaluationItem(
                    name: assignment.name,
                    weightLabel: "\(Int(assignment.pointsPossible)) pts",
                    scoresLabel: score,
                    state: grade == nil ? .pending : .graded,
                    detail: dueKey.map { "Vence: \(AcademicDateFormatter.shortDateLabel($0))" } ?? "Sin fecha de entrega definida en canvas-mock."
                )
            }
            let pendingCount = evaluations.filter { $0.state == .pending }.count
            let updatedCourse = CourseCardData(
                id: course.id,
                code: course.code,
                name: course.name,
                projectedGrade: course.projectedGrade,
                trackHint: course.trackHint,
                absences: attendanceEntries.filter { !$0.isPresent }.count,
                absenceLimit: 8,
                nextDueLabel: course.nextDueLabel
            )
            data = CourseDetailData(
                course: updatedCourse,
                headline: "\(assignments.count) entrega(s) · canvas-mock",
                projectionHint: pendingCount > 0 ? "Tenés \(pendingCount) entrega(s) sin calificar en este curso." : "Todas las entregas de este curso están calificadas.",
                attendance: attendanceEntries,
                practices: [],
                labs: [],
                pending: evaluations,
                pendingTitle: "Entregas"
            )
        }
    }
}
