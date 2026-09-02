import Foundation
import Observation

@MainActor
@Observable
final class HomeViewModel {
    private let bridge: SharedLogicBridge

    var data: HomeScreenData
    var isLoading = false
    var errorMessage: String?
    var userName: String?

    init(bridge: SharedLogicBridge, userName: String? = nil) {
        self.bridge = bridge
        self.userName = userName
        var initial = HomeScreenData.demo
        if let userName, !userName.isEmpty {
            initial.greeting = "¡Bienvenido, \(userName)!"
        }
        data = initial
    }

    func updateUserName(_ name: String?) {
        userName = name
        if let name, !name.isEmpty {
            data.greeting = "¡Bienvenido, \(name)!"
        }
    }

    func load() {
        guard !isLoading else { return }
        isLoading = true
        Task { @MainActor in
            defer { isLoading = false }
            do {
                let courses = try await bridge.courses()
                var rawEvents: [HomeRawEvent] = []
                var coursesByID: [Int: SharedAcademicCourse] = [:]

                for course in courses {
                    coursesByID[course.id] = course
                    let sessions = (try? await bridge.courseClassSessions(courseID: course.id)) ?? []
                    let attendance = (try? await bridge.courseAttendance(courseID: course.id)) ?? []
                    let assignments = (try? await bridge.courseAssignments(courseID: course.id)) ?? []
                    let attendanceBySession = Dictionary(uniqueKeysWithValues: attendance.map { ($0.classSessionId, $0) })

                    for session in sessions {
                        guard let startAt = session.startAt else { continue }
                        if attendanceBySession[session.id]?.isPresent == false {
                            rawEvents.append(HomeRawEvent(dateKey: AcademicDateFormatter.dateKey(from: startAt), isAbsence: true))
                        }
                    }
                    for assignment in assignments {
                        guard let dueAt = assignment.dueAt else { continue }
                        rawEvents.append(HomeRawEvent(
                            dateKey: AcademicDateFormatter.dateKey(from: dueAt),
                            isAssignment: true,
                            courseID: course.id,
                            assignmentName: assignment.name
                        ))
                    }
                }

                let dateKeys = Array(Set(rawEvents.map(\.dateKey))).sorted()
                let earliestDue = rawEvents.filter(\.isAssignment).map(\.dateKey).min()
                let days = dateKeys.map { key in
                    WeekDayData(
                        dayOfWeek: AcademicDateFormatter.weekdayName(key),
                        dayNumber: String(key.dropFirst(8)),
                        dateLabel: AcademicDateFormatter.shortDateLabel(key),
                        isToday: key == dateKeys.first,
                        isCritical: rawEvents.contains { $0.dateKey == key && $0.isAbsence }
                    )
                }
                let tasks = rawEvents.filter(\.isAssignment).map { event in
                    let course = event.courseID.flatMap { coursesByID[$0] }
                    return TaskEntry(
                        label: course.map { "\($0.code) · tarea" } ?? "Tarea",
                        title: event.assignmentName ?? "Tarea",
                        timestamp: AcademicDateFormatter.shortDateLabel(event.dateKey),
                        urgency: event.dateKey == earliestDue ? .urgent : .normal,
                        dayNumber: String(event.dateKey.dropFirst(8)),
                        systemImage: "doc.text"
                    )
                }

                var updated = data
                updated.dateLabel = dateKeys.first.map(AcademicDateFormatter.fullDateLabel) ?? "Sin eventos programados"
                updated.weekTitle = "canvas-mock · \(courses.count) curso(s)"
                updated.days = days.isEmpty ? data.days : days
                updated.tasks = tasks
                updated.pendingCount = tasks.count
                data = updated
                errorMessage = nil
            } catch {
                errorMessage = "No se pudo cargar canvas-mock: \(error.localizedDescription)"
            }
        }
    }
}

private struct HomeRawEvent {
    let dateKey: String
    var isAbsence = false
    var isAssignment = false
    var courseID: Int?
    var assignmentName: String?
}
