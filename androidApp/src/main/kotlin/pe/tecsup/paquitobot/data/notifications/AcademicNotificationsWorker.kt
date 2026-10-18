package pe.tecsup.paquitobot.data.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.clerk.api.Clerk
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.flow.first
import pe.tecsup.paquitobot.MainActivity
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.PaquitoBotApp
import pe.tecsup.paquitobot.auth.authorizedAcademicUserId
import pe.tecsup.paquitobot.data.local.NotificationPreferencesStore
import pe.tecsup.paquitobot.domain.notifications.AcademicNotificationPlanner
import pe.tecsup.paquitobot.domain.notifications.NotificationAssignment
import pe.tecsup.paquitobot.domain.notifications.NotificationLabGrade
import pe.tecsup.paquitobot.domain.notifications.NotificationAbsence
import pe.tecsup.paquitobot.domain.notifications.NotificationCategory
import pe.tecsup.paquitobot.domain.academic.AcademicAssignmentKind
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class AcademicNotificationsWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        deliveryMutex.withLock {
            try {
                deliver()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                // No registrar credenciales ni el cuerpo de respuestas del backend.
                if (runAttemptCount < 3) Result.retry()
                else Result.success(workDataOf("status" to "refresh_failed"))
            }
        }
    }

    private suspend fun deliver(): Result {
        val context = applicationContext
        withTimeoutOrNull(30_000) { Clerk.isInitialized.first { it } } ?: return Result.retry()
        if (!AndroidAcademicNotificationScheduler.isEligible(context)) return Result.success()
        val preferences = NotificationPreferencesStore(context)
        val categories = preferences.enabledCategories()
        val schedule = preferences.schedule()
        val originalUser = authorizedAcademicUserId() ?: return Result.success()
        val repository = (context as PaquitoBotApp).academicRepository
        val profile = repository.profile().getOrThrow()
        val courses = repository.courses().getOrThrow()
        val assignments = mutableListOf<NotificationAssignment>()
        val absences = mutableListOf<NotificationAbsence>()
        val labGrades = mutableListOf<NotificationLabGrade>()
        // Ante un fallo parcial no enviar un resumen incompleto ni asumir datos vacíos.
        for (course in courses) {
            val grades = repository.courseGrades(course.id).getOrThrow().associateBy { it.assignmentId }
            val graded = grades.keys
            repository.courseAssignments(course.id).getOrThrow().forEach { assignment ->
                if (assignment.kind == AcademicAssignmentKind.Laboratory) {
                    grades[assignment.id]?.let { grade ->
                        labGrades += NotificationLabGrade(course.id, assignment.id, assignment.name, grade.score)
                    }
                }
                val due = AcademicNotificationPlanner.parseDueAt(assignment.dueAt)
                if (due != null && assignment.id !in graded) {
                    assignments += NotificationAssignment(course.id, assignment.id, assignment.name, due,
                        if (assignment.kind == AcademicAssignmentKind.Laboratory) NotificationCategory.Laboratories else NotificationCategory.Deadlines)
                }
            }
            if (NotificationCategory.Attendance in categories) {
                val sessions = repository.courseClassSessions(course.id).getOrThrow().associateBy { it.id }
                repository.courseAttendance(course.id).getOrThrow().filter { !it.isPresent }.forEach { record ->
                    val ended = AcademicNotificationPlanner.parseDueAt(sessions[record.classSessionId]?.endAt)
                    if (ended != null) absences += NotificationAbsence(course.id, record.classSessionId, course.name, ended)
                }
            }
        }
        val now = Instant.now()
        val notices = AcademicNotificationPlanner.plan(assignments, categories, schedule, now, ZoneId.systemDefault(), absences, labGrades)
        val ledger = context.getSharedPreferences("academic_notice_delivery", Context.MODE_PRIVATE)
        // Separa cuentas de Clerk sin publicar el identificador en tags o nombres de trabajo.
        val scope = MessageDigest.getInstance("SHA-256").digest(originalUser.toByteArray())
            .joinToString("") { "%02x".format(it) }.take(24) + ":${profile.id}:"
        val oldest = now.toEpochMilli() - TimeUnit.DAYS.toMillis(90)
        val cleanup = ledger.edit()
        ledger.all.forEach { (key, value) -> if (value is Long && value < oldest) cleanup.remove(key) }
        cleanup.apply()
        val manager = context.getSystemService(NotificationManager::class.java)
        AndroidAcademicNotificationScheduler.createChannel(context)
        var published = 0
        for (notice in notices.sortedBy { if (it.key.startsWith("summary:")) 0 else 1 }) {
            if (isStopped || authorizedAcademicUserId() != originalUser || preferences.enabledCategories() != categories ||
                preferences.schedule() != schedule || !AndroidAcademicNotificationScheduler.isEligible(context)) return Result.success()
            val eventKey = scope + notice.key
            if (ledger.contains(eventKey)) continue
            if (published == 10) break
            val intent = Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            val pending = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Notification.Builder(context, AndroidAcademicNotificationScheduler.CHANNEL_ID)
            } else {
                @Suppress("DEPRECATION")
                Notification.Builder(context)
            }
            val notification = builder.setSmallIcon(R.drawable.ic_paquito_docs_default)
                .setContentTitle("Demo · ${notice.title}").setContentText(notice.body)
                .setStyle(Notification.BigTextStyle().bigText(notice.body))
                .setContentIntent(pending).setAutoCancel(true).setOnlyAlertOnce(true)
                .setVisibility(Notification.VISIBILITY_PRIVATE).build()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return Result.success()
            try {
                manager.notify(scope + notice.displayKey, 0, notification)
            } catch (_: SecurityException) {
                return Result.success(workDataOf("status" to "notifications_blocked"))
            }
            // El tag estable actualiza el mismo aviso si el proceso muere antes de registrar el envío.
            if (!ledger.edit().putLong(eventKey, now.toEpochMilli()).commit()) return Result.retry()
            published++
        }
        return Result.success(workDataOf("status" to "synced", "candidate_count" to notices.size))
    }

    private companion object {
        val deliveryMutex = Mutex()
    }
}
