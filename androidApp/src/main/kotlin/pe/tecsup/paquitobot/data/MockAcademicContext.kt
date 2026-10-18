package pe.tecsup.paquitobot.data

import org.json.JSONArray
import org.json.JSONObject
import pe.tecsup.paquitobot.ui.courses.CourseDetailData
import pe.tecsup.paquitobot.ui.courses.CoursesScreenData
import pe.tecsup.paquitobot.ui.home.HomeScreenData
import pe.tecsup.paquitobot.ui.notifications.NotificationsInboxData
import pe.tecsup.paquitobot.ui.schedule.ScheduleScreenData

/** JSON de los mismos modelos mock que consumen las pantallas Android. */
object MockAcademicContext {
    fun snapshot(): JSONObject {
        val home = HomeScreenData.default()
        val courses = CoursesScreenData.default()
        val schedule = ScheduleScreenData.default()
        val notifications = NotificationsInboxData.default()

        return JSONObject()
            .put("source", "PaquitoBot: datos de demostración, no datos reales del estudiante")
            .put("mock_period", courses.termLabel)
            .put("home", JSONObject()
                .put("date_label", home.dateLabel)
                .put("week_title", home.weekTitle)
                .put("average", home.trackValue)
                .put("absences_used", home.absencesUsed)
                .put("absences_limit", home.absencesLimit)
                .put("due_this_week", home.dueThisWeek)
                .put("pending_count", home.pendingCount)
                .put("days", JSONArray(home.days.map { day ->
                    JSONObject()
                        .put("day", day.dayOfWeek)
                        .put("date", day.dateLabel)
                        .put("is_today_in_mock", day.isToday)
                }))
                .put("tasks", JSONArray(home.tasks.map { task ->
                    JSONObject()
                        .put("course_and_type", task.label)
                        .put("title", task.title)
                        .put("relative_time_in_mock", task.timestamp)
                        .put("urgency", task.urgency.name)
                        .put("day_number", task.dateKey.takeLast(2))
                        .put("due_date", task.dateKey)
                })))
            .put("courses", JSONObject()
                .put("term", courses.termLabel)
                .put("pending_count", courses.pendingCount)
                .put("items", JSONArray(courses.courses.map { course ->
                    JSONObject()
                        .put("id", course.id)
                        .put("code", course.code)
                        .put("name", course.name)
                        .put("projected_grade", course.projectedGrade)
                        .put("track_hint", course.trackHint)
                        .put("absences", course.absences)
                        .put("absence_limit", course.absenceLimit)
                        .put("next_due_label_in_mock", course.nextDueLabel)
                })))
            .put("course_details", JSONArray(courses.courses.map { course ->
                val detail = CourseDetailData.forCourse(course)
                JSONObject()
                    .put("course_id", course.id)
                    .put("headline", detail.headline)
                    .put("projection_hint", detail.projectionHint)
                    .put("practices", evaluations(detail.practices))
                    .put("labs", evaluations(detail.labs))
                    .put("forums", evaluations(detail.forums))
            }))
            .put("schedule", JSONObject()
                .put("week_label", schedule.weekLabel)
                .put("days", JSONArray(schedule.days.map { day ->
                    JSONObject()
                        .put("label", day.label)
                        .put("events", JSONArray(day.events.map { event ->
                            JSONObject()
                                .put("time", event.time)
                                .put("title", event.title)
                                .put("subtitle", event.subtitle)
                                .put("kind", event.kind.name)
                        }))
                })))
            .put("notifications", JSONArray(notifications.items.map { item ->
                JSONObject()
                    .put("id", item.id)
                    .put("title", item.title)
                    .put("body", item.body)
                    .put("relative_time_in_mock", item.timestamp)
                    .put("severity", item.severity.name)
            }))
    }

    private fun evaluations(items: List<pe.tecsup.paquitobot.ui.courses.EvaluationItem>): JSONArray =
        JSONArray(items.map { item ->
            JSONObject()
                .put("name", item.name)
                .put("weight", item.weightLabel)
                .put("scores", item.scoresLabel)
                .put("state", item.state.name)
        })
}
