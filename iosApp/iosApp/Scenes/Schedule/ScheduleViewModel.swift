import Foundation
import Observation

@MainActor
@Observable
final class ScheduleViewModel {
    private let bridge: SharedLogicBridge

    var data = ScheduleScreenData.demo
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
                var events: [ScheduleRawEvent] = []
                for course in courses {
                    let sessions = (try? await bridge.courseClassSessions(courseID: course.id)) ?? []
                    let attendance = (try? await bridge.courseAttendance(courseID: course.id)) ?? []
                    let assignments = (try? await bridge.courseAssignments(courseID: course.id)) ?? []
                    let attendanceBySession = Dictionary(uniqueKeysWithValues: attendance.map { ($0.classSessionId, $0) })

                    for session in sessions {
                        guard let startAt = session.startAt else { continue }
                        let absent = attendanceBySession[session.id]?.isPresent == false
                        events.append(ScheduleRawEvent(
                            dateKey: AcademicDateFormatter.dateKey(from: startAt),
                            time: AcademicDateFormatter.time(from: startAt),
                            title: course.name,
                            subtitle: absent ? "Falta marcada" : course.code,
                            kind: absent ? .falta : .clase
                        ))
                    }
                    for assignment in assignments {
                        guard let dueAt = assignment.dueAt else { continue }
                        events.append(ScheduleRawEvent(
                            dateKey: AcademicDateFormatter.dateKey(from: dueAt),
                            time: AcademicDateFormatter.time(from: dueAt),
                            title: assignment.name,
                            subtitle: course.name,
                            kind: .entrega
                        ))
                    }
                }

                let grouped = Dictionary(grouping: events, by: \.dateKey)
                let days = grouped.keys.sorted().map { key in
                    ScheduleDay(
                        label: AcademicDateFormatter.weekdayWithDay(key),
                        events: grouped[key, default: []].sorted { $0.time < $1.time }.map {
                            ScheduleEvent(time: $0.time, title: $0.title, subtitle: $0.subtitle, kind: $0.kind)
                        }
                    )
                }
                data = ScheduleScreenData(
                    weekLabel: "canvas-mock · \(courses.count) curso(s)",
                    pendingCount: events.filter { $0.kind == .entrega }.count,
                    days: days
                )
                errorMessage = nil
            } catch {
                errorMessage = "No se pudo cargar canvas-mock: \(error.localizedDescription)"
            }
        }
    }
}

private struct ScheduleRawEvent {
    let dateKey: String
    let time: String
    let title: String
    let subtitle: String
    let kind: ScheduleKind
}
