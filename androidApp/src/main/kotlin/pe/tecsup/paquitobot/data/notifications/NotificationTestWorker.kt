package pe.tecsup.paquitobot.data.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.clerk.api.Clerk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull
import pe.tecsup.paquitobot.auth.authorizedAcademicUserId
import pe.tecsup.paquitobot.domain.notifications.AcademicNotice

/** Prueba explícita: usa el mismo permiso, canal, presentación e historial que los avisos académicos. */
class NotificationTestWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        withTimeoutOrNull(30_000) { Clerk.isInitialized.first { it } } ?: return Result.failure(workDataOf("status" to "session_unavailable"))
        val user = authorizedAcademicUserId() ?: return Result.failure(workDataOf("status" to "session_changed"))
        val owner = AcademicNotificationPublisher.ownerKey(user)
        if (owner != inputData.getString("owner")) return Result.failure(workDataOf("status" to "session_changed"))
        val notice = AcademicNotice("test:background:preview", "Aviso de prueba",
            "PaquitoBot puede mostrar avisos con la app en segundo plano. Toca para abrir tu bandeja. Este aviso no es una entrega real.")
        val published = AcademicNotificationPublisher(applicationContext).publish(owner, notice, System.currentTimeMillis())
        return if (published) Result.success(workDataOf("status" to "published"))
            else Result.failure(workDataOf("status" to "notifications_blocked"))
    }
    companion object { const val WORK_NAME = "notification-preview" }
}
