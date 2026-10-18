package pe.tecsup.paquitobot.data.local

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pe.tecsup.paquitobot.domain.notifications.NotificationCategory

@RunWith(AndroidJUnit4::class)
class NotificationPreferencesStoreTest {
    // El contexto del APK de pruebas mantiene intactas las preferencias del usuario.
    private val context = InstrumentationRegistry.getInstrumentation().context

    @Before
    fun resetTestPreferences() {
        context.getSharedPreferences("paquitobot_notification_preferences", Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun firstLaunchUsesOnboardingDefaults() {
        assertEquals(NotificationCategory.defaults, NotificationPreferencesStore(context).enabledCategories())
    }

    @Test
    fun selectionSurvivesStoreRecreationAndCanBeReplaced() {
        NotificationPreferencesStore(context).saveEnabledCategories(setOf(NotificationCategory.Attendance))
        val recreated = NotificationPreferencesStore(context)
        assertEquals(setOf(NotificationCategory.Attendance), recreated.enabledCategories())
        recreated.saveEnabledCategories(setOf(NotificationCategory.Deadlines))
        assertEquals(setOf(NotificationCategory.Deadlines), NotificationPreferencesStore(context).enabledCategories())
    }

    @Test
    fun disablingAllDoesNotRestoreDefaults() {
        NotificationPreferencesStore(context).saveEnabledCategories(emptySet())
        assertEquals(emptySet<NotificationCategory>(), NotificationPreferencesStore(context).enabledCategories())
    }

    @Test
    fun unknownStoredCategoriesAreIgnored() {
        context.getSharedPreferences("paquitobot_notification_preferences", Context.MODE_PRIVATE)
            .edit().putStringSet("enabled_categories", setOf("attendance", "unknown_future_category")).commit()
        assertEquals(setOf(NotificationCategory.Attendance), NotificationPreferencesStore(context).enabledCategories())
    }
}
