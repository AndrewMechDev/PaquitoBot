package pe.tecsup.paquitobot.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.clerk.api.Clerk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.delay
import pe.tecsup.paquitobot.auth.authorizedAcademicUserId
import pe.tecsup.paquitobot.domain.notifications.AcademicNotice
import pe.tecsup.paquitobot.PaquitoBotApp
import pe.tecsup.paquitobot.domain.academic.AcademicRiskEngine
import pe.tecsup.paquitobot.domain.academic.AcademicAssignmentKind
import pe.tecsup.paquitobot.domain.notifications.AcademicNotificationPlanner
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** Prueba explícita: usa el mismo permiso, canal, presentación e historial que los avisos académicos. */
class NotificationTestWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        withTimeoutOrNull(30_000) { Clerk.isInitialized.first { it } } ?: return Result.failure(workDataOf("status" to "session_unavailable"))
        val user = authorizedAcademicUserId() ?: return Result.failure(workDataOf("status" to "session_changed"))
        val owner = AcademicNotificationPublisher.ownerKey(user)
        if (owner != inputData.getString("owner")) return Result.failure(workDataOf("status" to "session_changed"))
        val notices = run {
            val repository = (applicationContext as PaquitoBotApp).academicRepositoryFor(user)
            val deadlines = mutableListOf<AcademicNotice>()
            val labs = mutableListOf<AcademicNotice>()
            val now = Instant.now()
            val alerts = repository.courses().getOrElse { return Result.retry() }.flatMap { course ->
                val courseGrades = repository.courseGrades(course.id).getOrElse { return Result.retry() }
                repository.courseAssignments(course.id).getOrElse { return Result.retry() }.forEach { assignment ->
                    val grade = courseGrades.find { it.assignmentId == assignment.id }
                    val due = AcademicNotificationPlanner.parseDueAt(assignment.dueAt)
                    if (grade == null && due != null && due.isAfter(now)) {
                        val dateLabel = due.atZone(ZoneId.of("America/Lima")).format(DateTimeFormatter.ofPattern("dd/MM · HH:mm"))
                        deadlines += AcademicNotice("reminder:${course.id}:${assignment.id}:${due.toEpochMilli()}",
                            if (due.isBefore(now.plusSeconds(172800))) "Trabajo por vencer" else "Entrega programada",
                            "${course.code} · ${assignment.name}. Entrega hasta el $dateLabel. Revisa los detalles y organiza tu tiempo.")
                    }
                    if (grade != null && assignment.kind == AcademicAssignmentKind.Laboratory) {
                        labs += AcademicNotice("lab-grade:${course.id}:${assignment.id}:${grade.score}", "Laboratorio calificado",
                            "${course.code} · ${assignment.name}: obtuviste ${grade.score} de ${assignment.pointsPossible}. Revisa cómo influye en tu promedio.")
                    }
                }
                AcademicRiskEngine.assess(course,
                    courseGrades,
                    repository.courseAttendance(course.id).getOrElse { return Result.retry() },
                    repository.courseClassSessions(course.id).getOrElse { return Result.retry() }).alerts
            }
            val ordered = alerts.sortedByDescending { it.level.ordinal }
            val risks = ordered.map { alert ->
                AcademicNotice("risk:${alert.kind.name.lowercase()}:${alert.courseId}:${alert.level.name.lowercase()}", alert.title, alert.message)
            }
            val attendance = risks.filter { it.key.startsWith("risk:attendance:") }
            val grades = risks.filter { it.key.startsWith("risk:grade:") }
            val summary = AcademicNotice("summary:${java.time.LocalDate.now()}", "Tu resumen académico",
                "${deadlines.size} entregas pendientes, ${attendance.size} avisos de asistencia y ${grades.size} avisos de notas. Revisa tus cursos y prioriza lo más importante.")
            // Primero una muestra de cada tipo; luego el resto, sin repetir avisos.
            (listOfNotNull(attendance.firstOrNull(), grades.firstOrNull(), deadlines.firstOrNull(), labs.firstOrNull(), summary) +
                risks + deadlines + labs).distinctBy { it.displayKey }.take(12)
        }
        val publisher = AcademicNotificationPublisher(applicationContext)
        if (authorizedAcademicUserId() != user) return Result.failure(workDataOf("status" to "session_changed"))
        setProgress(workDataOf("sent" to 0, "total" to notices.size))
        for ((index, notice) in notices.withIndex()) {
            delay(if (index == 0) 15_000L else 5_000L)
            if (isStopped || authorizedAcademicUserId() != user) return Result.failure(workDataOf("status" to "session_changed"))
            if (!publisher.publish(owner, notice, System.currentTimeMillis(), demoPresentation = true)) {
                return Result.failure(workDataOf("status" to "notifications_blocked"))
            }
            setProgress(workDataOf("sent" to index + 1, "total" to notices.size))
        }
        return Result.success(workDataOf("status" to "published", "sent" to notices.size))
    }
    companion object { const val WORK_NAME = "notification-preview" }
}
