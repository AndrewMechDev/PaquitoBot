package pe.tecsup.paquitobot.domain.notifications

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

data class NotificationAssignment(val courseId: Int, val id: Int, val title: String, val due: Instant)
data class AcademicNotice(val key: String, val title: String, val body: String) {
    // La versión distingue envíos; el identificador visual reemplaza el aviso de la misma tarea.
    val displayKey: String get() = key.substringBeforeLast(':')
}

/** Reglas deterministas; no accede a Android, red ni almacenamiento. */
object AcademicNotificationPlanner {
    fun parseDueAt(value: String?): Instant? = try {
        value?.let { OffsetDateTime.parse(it).toInstant().also { instant -> instant.toEpochMilli() } }
    } catch (_: DateTimeParseException) {
        null // Una fecha sin zona/offset no permite inferir un instante de entrega.
    } catch (_: ArithmeticException) {
        null // Fechas fuera del rango representable no bloquean las demás asignaciones.
    }

    fun plan(
        assignments: List<NotificationAssignment>,
        categories: Set<NotificationCategory>,
        schedule: NotificationSchedule,
        now: Instant,
        zone: ZoneId,
    ): List<AcademicNotice> {
        if (NotificationCategory.Deadlines !in categories) return emptyList()
        val upcoming = assignments.distinctBy { it.courseId to it.id }.filter { it.due.isAfter(now) }
        val reminders = upcoming.filter {
            !it.due.minus(Duration.ofHours(schedule.reminderHours.toLong())).isAfter(now)
        }.map {
            AcademicNotice("reminder:${it.courseId}:${it.id}:${it.due.toEpochMilli()}",
                "Entrega próxima", "${it.title}. Revisa la fecha de entrega en PaquitoBot.")
        }
        if (!schedule.dailySummaryEnabled) return reminders
        val today = now.atZone(zone).toLocalDate()
        val summaryTime = today.atTime(schedule.summaryHour, schedule.summaryMinute).atZone(zone).toInstant()
        val todayCount = upcoming.count { it.due.atZone(zone).toLocalDate() == today }
        if (now.isBefore(summaryTime) || todayCount == 0) return reminders
        return reminders + AcademicNotice("summary:$today", "Tu resumen académico",
            if (todayCount == 1) "Tienes una entrega con vencimiento hoy. Revisa tus cursos."
            else "Tienes $todayCount entregas con vencimiento hoy. Revisa tus cursos.")
    }
}
