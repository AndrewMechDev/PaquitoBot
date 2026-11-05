package pe.tecsup.paquitobot.data.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import pe.tecsup.paquitobot.MainActivity
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.domain.notifications.AcademicNotice
import java.security.MessageDigest

/** Presentación nativa: conserva accesibilidad y preferencias de Android. */
class AcademicNotificationPublisher(private val context: Context) {
    fun build(notice: AcademicNotice, time: Long, silent: Boolean = false, demoPresentation: Boolean = false): Notification {
        val intent = Intent(context, MainActivity::class.java).setAction(OPEN_INBOX)
            .putExtra(NOTICE_KEY, notice.key)
            .setData(android.net.Uri.parse("paquitobot://notice/${android.net.Uri.encode(notice.key)}"))
            .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pending = PendingIntent.getActivity(context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val redacted = NotificationCompat.Builder(context, AndroidAcademicNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_paquito_bell_outline).setContentTitle("PaquitoBot")
            .setContentText("Tienes un aviso académico. Abre la app para consultarlo.").build()
        return NotificationCompat.Builder(context, if (demoPresentation) DEMO_CHANNEL else AndroidAcademicNotificationScheduler.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_paquito_bell_outline)
            .setColor(0xFF00C9FB.toInt())
            .setContentTitle(notice.title).setContentText(notice.body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notice.body))
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(pending).setWhen(time).setShowWhen(true)
            .setAutoCancel(true).setOnlyAlertOnce(!demoPresentation).setSilent(silent)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE).setPublicVersion(redacted)
            .build()
    }

    fun publish(owner: String, notice: AcademicNotice, time: Long, silent: Boolean = false, demoPresentation: Boolean = false): Boolean {
        AndroidAcademicNotificationScheduler.createChannel(context)
        if (demoPresentation && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.getSystemService(NotificationManager::class.java).createNotificationChannel(
                NotificationChannel(DEMO_CHANNEL, "Avisos destacados", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Presentación de alertas de cursos, notas y entregas"
                },
            )
        }
        if (!AndroidAcademicNotificationScheduler.notificationsAllowed(context) ||
            (Build.VERSION.SDK_INT >= 33 && context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)) return false
        return try {
            context.getSystemService(NotificationManager::class.java)
                .notify(owner + notice.displayKey, 0, build(notice, time, silent, demoPresentation))
            NotificationHistoryStore(context, owner).save(notice, time)
            true
        } catch (_: SecurityException) { false }
    }

    companion object {
        private const val DEMO_CHANNEL = "academic-demo-highlights"
        const val OPEN_INBOX = "pe.tecsup.paquitobot.OPEN_NOTIFICATIONS"
        const val NOTICE_KEY = "academic_notice_key"
        fun ownerKey(userId: String): String = MessageDigest.getInstance("SHA-256").digest(userId.toByteArray())
            .joinToString("") { "%02x".format(it) }.take(24)
    }
}
