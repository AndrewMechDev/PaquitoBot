import Foundation
import Observation

@MainActor
@Observable
final class CoursesViewModel {
    private let bridge: SharedLogicBridge

    var data = CoursesScreenData.demo
    var isLoading = false
    var errorMessage: String?

    init(bridge: SharedLogicBridge) {
        self.bridge = bridge
    }

    func load() {
        guard !isLoading else { return }
        isLoading = true
        Task { @MainActor in
            defer { isLoading = false }
            do {
                let courses = try await bridge.courses()
                var cards: [CourseCardData] = []
                for course in courses {
                    let grades = (try? await bridge.courseGrades(courseID: course.id)) ?? []
                    let attendance = (try? await bridge.courseAttendance(courseID: course.id)) ?? []
                    let assignments = (try? await bridge.courseAssignments(courseID: course.id)) ?? []
                    let grade = grades.isEmpty ? "—" : String(format: "%.1f", grades.map(\.score).reduce(0, +) / Double(grades.count))
                    let nextDue = assignments
                        .compactMap { assignment -> (String, String)? in
                            guard let dueAt = assignment.dueAt else { return nil }
                            return (dueAt, "\(assignment.name) · \(AcademicDateFormatter.shortDateLabel(AcademicDateFormatter.dateKey(from: dueAt)))")
                        }
                        .sorted { $0.0 < $1.0 }
                        .first?.1 ?? "Sin pendientes"
                    cards.append(CourseCardData(
                        id: String(course.id),
                        code: course.code,
                        name: course.name,
                        projectedGrade: grade,
                        trackHint: grades.isEmpty ? "Sin notas registradas todavía" : "Promedio real de canvas-mock",
                        absences: attendance.filter { !$0.isPresent }.count,
                        absenceLimit: 8,
                        nextDueLabel: nextDue
                    ))
                }
                data = CoursesScreenData(termLabel: "canvas-mock · \(courses.count) curso(s)", pendingCount: cards.filter { $0.nextDueLabel != "Sin pendientes" }.count, courses: cards)
                errorMessage = nil
            } catch {
                errorMessage = "No se pudo cargar canvas-mock: \(error.localizedDescription)"
            }
        }
    }
}
