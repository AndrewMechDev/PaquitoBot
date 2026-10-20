package pe.tecsup.paquitobot.data.local

import android.content.Context
import pe.tecsup.paquitobot.domain.notifications.NotificationCategory
import pe.tecsup.paquitobot.domain.notifications.NotificationSchedule

/** Preferencias locales de esta instalación; no contiene credenciales ni programa avisos. */
class NotificationPreferencesStore(context: Context) {
    private val prefs = context.getSharedPreferences(
        "paquitobot_notification_preferences",
        Context.MODE_PRIVATE,
    )

    fun enabledCategories(): Set<NotificationCategory> {
        // Un conjunto vacío guardado significa que el usuario desactivó todos los avisos.
        val stored = prefs.getStringSet(KEY_ENABLED, null) ?: return NotificationCategory.defaults
        return NotificationCategory.entries.filterTo(mutableSetOf()) { it.storageKey in stored }
    }

    fun saveEnabledCategories(categories: Set<NotificationCategory>) {
        savePreferences(categories, schedule())
    }

    fun hasCompletedSetup(): Boolean = prefs.getBoolean("setup_completed", false)

    fun markSetupCompleted() {
        prefs.edit().putBoolean("setup_completed", true).apply()
    }

    fun schedule(): NotificationSchedule = NotificationSchedule(
        reminderHours = prefs.getInt("reminder_hours", 24)
            .takeIf { it in NotificationSchedule.reminderOptions } ?: 24,
        dailySummaryEnabled = prefs.getBoolean("daily_summary_enabled", true),
        summaryHour = prefs.getInt("summary_hour", 8).takeIf { it in 0..23 } ?: 8,
        summaryMinute = prefs.getInt("summary_minute", 0).takeIf { it in 0..59 } ?: 0,
    )

    fun savePreferences(categories: Set<NotificationCategory>, schedule: NotificationSchedule) {
        prefs.edit()
            .putStringSet(KEY_ENABLED, categories.map { it.storageKey }.toSet())
            .putInt("reminder_hours", schedule.reminderHours)
            .putBoolean("daily_summary_enabled", schedule.dailySummaryEnabled)
            .putInt("summary_hour", schedule.summaryHour)
            .putInt("summary_minute", schedule.summaryMinute)
            .apply()
    }

    private companion object {
        const val KEY_ENABLED = "enabled_categories"
    }
}
