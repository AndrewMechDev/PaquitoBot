package pe.tecsup.paquitobot.data.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.clerk.api.Clerk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.auth.authorizedAcademicUserId
import pe.tecsup.paquitobot.domain.notifications.AcademicNotice
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AcademicNotificationsDeviceTest {
    private val context = InstrumentationRegistry.getInstrumentation().targetContext

    @Test fun historyIsIsolatedAndUpdatesSameNoticeInsteadOfDuplicating() {
        val owner = "qa-${UUID.randomUUID()}"
        val store = NotificationHistoryStore(context, owner)
        val other = NotificationHistoryStore(context, "$owner-other")
        try {
            store.save(AcademicNotice("reminder:1:2:date1", "Aviso", "Primer texto"), 1000)
            store.save(AcademicNotice("reminder:1:2:date2", "Aviso", "Texto actualizado"), 2000)
            assertEquals(1, store.read().size)
            assertEquals("Texto actualizado", store.read().single().body)
            assertTrue(other.read().isEmpty())
            val removed = store.remove("reminder:1:2")!!
            assertTrue(store.read().isEmpty())
            store.restore(removed)
            assertEquals(2000L, store.read().single().time)
        } finally { store.clear(); other.clear() }
    }

    @Test fun nativePresentationHasBrandIconExpandedTextAndPrivateLockScreen() {
        AndroidAcademicNotificationScheduler.createChannel(context)
        val notification = AcademicNotificationPublisher(context).build(AcademicNotice("test:visual:1", "Aviso de prueba", "Detalle completo"), 1234)
        assertEquals(R.drawable.ic_paquito_bell_outline, notification.smallIcon.resId)
        assertEquals(0xFF00C9FB.toInt(), notification.color)
        assertEquals(Notification.VISIBILITY_PRIVATE, notification.visibility)
        assertEquals("Detalle completo", notification.extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString())
        assertEquals("Datos de demostración", notification.extras.getCharSequence(Notification.EXTRA_SUB_TEXT).toString())
        assertNotNull(notification.publicVersion)
        assertFalse(notification.publicVersion.extras.getCharSequence(Notification.EXTRA_TEXT).toString().contains("Detalle completo"))
        assertNotNull(notification.contentIntent)
        if (Build.VERSION.SDK_INT >= 26) assertEquals(AndroidAcademicNotificationScheduler.CHANNEL_ID, notification.channelId)
    }

    @Test fun workerPublishesInBackgroundAndRepeatingItKeepsOneVisibleNotice() = runBlocking {
        withTimeout(40_000) { Clerk.isInitialized.first { it } }
        val user = authorizedAcademicUserId()
        assertNotNull("Inicia sesión institucional antes de ejecutar la prueba del worker", user)
        assertTrue("Activa las notificaciones del dispositivo", AndroidAcademicNotificationScheduler.notificationsAllowed(context))
        if (Build.VERSION.SDK_INT >= 33) assertEquals(PackageManager.PERMISSION_GRANTED, context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS))
        val owner = AcademicNotificationPublisher.ownerKey(user!!)
        val manager = WorkManager.getInstance(context)
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(home)
        repeat(2) {
            val request = OneTimeWorkRequestBuilder<NotificationTestWorker>().setInputData(workDataOf("owner" to owner)).build()
            manager.enqueue(request).result.get()
            val info = withTimeout(45_000) { manager.getWorkInfoByIdFlow(request.id).first { it?.state?.isFinished == true } }!!
            assertEquals(WorkInfo.State.SUCCEEDED, info.state)
            assertEquals("published", info.outputData.getString("status"))
        }
        val active = context.getSystemService(NotificationManager::class.java).activeNotifications.filter { it.tag == owner + "test:background" }
        assertEquals(1, active.size)
        assertTrue(NotificationHistoryStore(context, owner).read().any { it.id == "test:background" })
    }

    @Test fun academicWorkerReconcilesSelectedCategoriesWithoutResendingDeliveredEvents() = runBlocking {
        withTimeout(40_000) { Clerk.isInitialized.first { it } }
        assertTrue("Completa onboarding y activa alguna categoría antes de esta prueba", AndroidAcademicNotificationScheduler.isEligible(context))
        val owner = AcademicNotificationPublisher.ownerKey(authorizedAcademicUserId()!!)
        val manager = WorkManager.getInstance(context)
        val ledger = context.getSharedPreferences("academic_notice_delivery", Context.MODE_PRIVATE)
        suspend fun reconcile() {
            val request = OneTimeWorkRequestBuilder<AcademicNotificationsWorker>().build()
            manager.enqueue(request).result.get()
            val info = withTimeout(45_000) { manager.getWorkInfoByIdFlow(request.id).first { it?.state?.isFinished == true } }!!
            assertEquals(WorkInfo.State.SUCCEEDED, info.state)
            assertEquals("synced", info.outputData.getString("status"))
        }
        reconcile()
        val delivered = ledger.all.filterKeys { it.startsWith(owner) }
        reconcile()
        assertEquals(delivered, ledger.all.filterKeys { it.startsWith(owner) })
    }
}
