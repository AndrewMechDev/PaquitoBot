package pe.tecsup.paquitobot.data.notifications

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import pe.tecsup.paquitobot.domain.notifications.AcademicNotice

data class StoredAcademicNotice(val id: String, val title: String, val body: String, val time: Long)

/** Historial local por cuenta. El registro de entrega se conserva aparte al borrar la bandeja. */
class NotificationHistoryStore(context: Context, private val owner: String) {
    private val prefs = context.getSharedPreferences("academic_notice_history", Context.MODE_PRIVATE)
    fun read(): List<StoredAcademicNotice> = synchronized(lock) {
        val array = try { JSONArray(prefs.getString(owner, "[]")) } catch (_: org.json.JSONException) { JSONArray() }
        (0 until array.length()).mapNotNull { index ->
            val item = array.optJSONObject(index) ?: return@mapNotNull null
            StoredAcademicNotice(item.optString("id"), item.optString("title"), item.optString("body"), item.optLong("time"))
                .takeIf { it.id.isNotBlank() && it.time > 0 }
        }.sortedByDescending { it.time }.take(100)
    }
    fun save(notice: AcademicNotice, time: Long) = synchronized(lock) {
        replace(listOf(StoredAcademicNotice(notice.displayKey, notice.title, notice.body, time)) + read().filter { it.id != notice.displayKey })
    }
    fun replace(items: List<StoredAcademicNotice>) = synchronized(lock) {
        val array = JSONArray()
        items.distinctBy { it.id }.sortedByDescending { it.time }.take(100).forEach {
            array.put(JSONObject().put("id", it.id).put("title", it.title).put("body", it.body).put("time", it.time))
        }
        prefs.edit().putString(owner, array.toString()).apply()
    }
    fun remove(id: String): StoredAcademicNotice? = synchronized(lock) {
        val items = read()
        val removed = items.firstOrNull { it.id == id }
        replace(items.filter { it.id != id })
        removed
    }
    fun restore(item: StoredAcademicNotice) = synchronized(lock) {
        replace(read().filter { it.id != item.id } + item)
    }
    fun clear() = synchronized(lock) { replace(emptyList()) }
    fun observe(listener: SharedPreferences.OnSharedPreferenceChangeListener) = prefs.registerOnSharedPreferenceChangeListener(listener)
    fun stopObserving(listener: SharedPreferences.OnSharedPreferenceChangeListener) = prefs.unregisterOnSharedPreferenceChangeListener(listener)
    private companion object { val lock = Any() }
}
