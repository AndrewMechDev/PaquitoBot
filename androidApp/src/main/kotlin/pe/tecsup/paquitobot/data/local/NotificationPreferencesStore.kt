package pe.tecsup.paquitobot.data.local

import android.content.Context
import pe.tecsup.paquitobot.domain.notifications.NotificationCategory

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
        prefs.edit().putStringSet(KEY_ENABLED, categories.map { it.storageKey }.toSet()).apply()
    }

    private companion object {
        const val KEY_ENABLED = "enabled_categories"
    }
}
