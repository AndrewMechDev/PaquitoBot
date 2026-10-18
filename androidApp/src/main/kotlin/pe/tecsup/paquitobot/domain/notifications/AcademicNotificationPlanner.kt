package pe.tecsup.paquitobot.domain.notifications

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException

data class NotificationAssignment(val courseId: Int, val id: Int, val title: String, val due: Instant,
    val category: NotificationCategory = NotificationCategory.Deadlines)
data class NotificationLabGrade(val courseId: Int, val assignmentId: Int, val title: String, val score: Double)
data class NotificationAbsence(val courseId: Int, val sessionId: Int, val courseName: String, val endedAt: Instant)
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
        absences: List<NotificationAbsence> = emptyList(),
        labGrades: List<NotificationLabGrade> = emptyList(),
    ): List<AcademicNotice> {
        val upcoming = assignments.distinctBy { it.courseId to it.id }.filter { it.due.isAfter(now) && it.category in categories }
        // Solo faltas registradas de clases finalizadas en las últimas 24 horas.
        // No se infiere una falta a partir de la ausencia de un registro.
        val recentAbsences = if (NotificationCategory.Attendance in categories) absences
            .distinctBy { it.courseId to it.sessionId }
            .filter { !it.endedAt.isAfter(now) && it.endedAt.isAfter(now.minus(Duration.ofDays(1))) }
            else emptyList()
        val attendanceNotices = recentAbsences.map {
            AcademicNotice("absence:${it.courseId}:${it.sessionId}:recorded", "Inasistencia registrada",
                "${it.courseName}: revisa tu asistencia en PaquitoBot.")
        }
        val reminders = upcoming.filter {
            !it.due.minus(Duration.ofHours(schedule.reminderHours.toLong())).isAfter(now)
        }.map {
            AcademicNotice("reminder:${it.courseId}:${it.id}:${it.due.toEpochMilli()}",
                if (it.category == NotificationCategory.Laboratories) "Laboratorio próximo" else "Entrega próxima", "${it.title}. Revisa la fecha de entrega en PaquitoBot.")
        }
        val gradeNotices = if (NotificationCategory.Laboratories in categories) labGrades
            .filter { it.score.isFinite() }.distinctBy { it.courseId to it.assignmentId }.map {
                AcademicNotice("lab-grade:${it.courseId}:${it.assignmentId}:${it.score}", "Laboratorio calificado",
                    "${it.title}: tienes una calificación disponible. Revísala en tus cursos.")
            } else emptyList()
        val notices = reminders + attendanceNotices + gradeNotices
        if (!schedule.dailySummaryEnabled) return notices
        val today = now.atZone(zone).toLocalDate()
        val summaryTime = today.atTime(schedule.summaryHour, schedule.summaryMinute).atZone(zone).toInstant()
        val todayCount = upcoming.count { it.due.atZone(zone).toLocalDate() == today }
        if (now.isBefore(summaryTime) || (todayCount == 0 && recentAbsences.isEmpty())) return notices
        return notices + AcademicNotice("summary:$today", "Tu resumen académico",
            "$todayCount entregas o laboratorios pendientes para hoy; ${recentAbsences.size} inasistencias recientes. Revisa tus cursos.")
    }
}
