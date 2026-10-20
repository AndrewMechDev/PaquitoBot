package pe.tecsup.paquitobot.domain.calendar

import java.security.MessageDigest
import java.time.*
import java.time.format.DateTimeParseException

data class CalendarCandidate(val task: CalendarTask, val start: String, val end: String,
    val allDay: Boolean, val past: Boolean) {
    val key: String get() = "${task.profileId}:${task.courseId}:${task.assignmentId}"
}
/** Los datetimes locales de Tecsup se interpretan en Perú; un offset explícito se respeta. */
object CalendarEventPlanner {
    val academicZone: ZoneId = ZoneId.of("America/Lima")
    fun candidate(task: CalendarTask, now: Instant): CalendarCandidate? = try {
        if (task.dueAt.length == 10) {
            val date = LocalDate.parse(task.dueAt)
            CalendarCandidate(task, date.toString(), date.plusDays(1).toString(), true,
                date.isBefore(now.atZone(academicZone).toLocalDate()))
        } else {
            val due = try { OffsetDateTime.parse(task.dueAt).toInstant() }
                catch (_: DateTimeParseException) { LocalDateTime.parse(task.dueAt).atZone(academicZone).toInstant() }
            CalendarCandidate(task, due.toString(), due.plusSeconds(900).toString(), false, due.isBefore(now))
        }
    } catch (_: DateTimeException) { null } catch (_: ArithmeticException) { null }

    fun event(candidate: CalendarCandidate, clerkOwner: String, googleSubject: String): CalendarEvent {
        val scope = digest("paquitobot-calendar-v1|$clerkOwner|$googleSubject|${candidate.key}")
        return CalendarEvent("paquito$scope", scope, "${candidate.task.course} · ${candidate.task.title}",
            "Recordatorio de entrega de PaquitoBot. Fuente académica actual: datos de demostración. Verifica el plazo en tu plataforma académica.",
            candidate.start, candidate.end, candidate.allDay, academicZone.id)
    }
    private fun digest(value: String): String = MessageDigest.getInstance("SHA-256")
        .digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }
}
