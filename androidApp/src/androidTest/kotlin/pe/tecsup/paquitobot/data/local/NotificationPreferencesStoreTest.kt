package pe.tecsup.paquitobot.data.local

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import pe.tecsup.paquitobot.domain.notifications.NotificationCategory
import pe.tecsup.paquitobot.domain.notifications.NotificationSchedule

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

    @Test
    fun oldCategorySelectionUsesDefaultSchedule() {
        context.getSharedPreferences("paquitobot_notification_preferences", Context.MODE_PRIVATE)
            .edit().putStringSet("enabled_categories", setOf("deadlines")).commit()
        val store = NotificationPreferencesStore(context)
        assertEquals(setOf(NotificationCategory.Deadlines), store.enabledCategories())
        assertEquals(NotificationSchedule(), store.schedule())
    }

    @Test
    fun scheduleSurvivesRecreationAndCategoryOnlyUpdates() {
        val schedule = NotificationSchedule(reminderHours = 6, dailySummaryEnabled = false, summaryHour = 19, summaryMinute = 30)
        NotificationPreferencesStore(context).savePreferences(setOf(NotificationCategory.Deadlines), schedule)
        val recreated = NotificationPreferencesStore(context)
        assertEquals(schedule, recreated.schedule())
        assertEquals(setOf(NotificationCategory.Deadlines), recreated.enabledCategories())
        recreated.saveEnabledCategories(emptySet())
        assertEquals(schedule, NotificationPreferencesStore(context).schedule())
        assertEquals(emptySet<NotificationCategory>(), NotificationPreferencesStore(context).enabledCategories())
    }

    @Test
    fun invalidStoredTimesFallBackToDefaults() {
        context.getSharedPreferences("paquitobot_notification_preferences", Context.MODE_PRIVATE)
            .edit().putInt("reminder_hours", -1).putInt("summary_hour", 99).putInt("summary_minute", -2).commit()
        assertEquals(NotificationSchedule(), NotificationPreferencesStore(context).schedule())
    }
}
