package pe.tecsup.paquitobot.data.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import pe.tecsup.paquitobot.auth.authorizedAcademicUserId
import pe.tecsup.paquitobot.data.local.NotificationPreferencesStore
import pe.tecsup.paquitobot.domain.notifications.AcademicNotificationScheduler
import pe.tecsup.paquitobot.domain.notifications.NotificationCategory
import java.util.concurrent.TimeUnit

class AndroidAcademicNotificationScheduler(context: Context) : AcademicNotificationScheduler {
    private val context = context.applicationContext

    override fun reconcile() {
        createChannel(context)
        val manager = WorkManager.getInstance(context)
        if (!isEligible(context)) {
            manager.cancelUniqueWork(WORK_NAME)
            return
        }
        val request = PeriodicWorkRequestBuilder<AcademicNotificationsWorker>(15, TimeUnit.MINUTES)
            .build()
        // UPDATE conserva el período y migra restricciones antiguas del backend de red.
        manager.enqueueUniquePeriodicWork(WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    companion object {
        const val WORK_NAME = "academic-notifications"
        const val CHANNEL_ID = "academic-reminders"

        fun createChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(CHANNEL_ID, "Avisos académicos", NotificationManager.IMPORTANCE_DEFAULT)
                channel.description = "Recordatorios de entregas y resumen diario de PaquitoBot"
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }
        }

        fun isEligible(context: Context): Boolean =
            NotificationPreferencesStore(context).hasCompletedSetup() &&
                authorizedAcademicUserId() != null &&
                NotificationCategory.Deadlines in NotificationPreferencesStore(context).enabledCategories() &&
                notificationsAllowed(context)

        fun notificationsAllowed(context: Context): Boolean =
            context.getSystemService(NotificationManager::class.java).areNotificationsEnabled() &&
                (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                    context.getSystemService(NotificationManager::class.java).getNotificationChannel(CHANNEL_ID)?.importance != NotificationManager.IMPORTANCE_NONE)
    }
}
