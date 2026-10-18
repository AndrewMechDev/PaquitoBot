package pe.tecsup.paquitobot.ui.notifications

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.data.notifications.NotificationHistoryStore
import pe.tecsup.paquitobot.data.notifications.StoredAcademicNotice
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

class NotificationsInboxViewModel(context: Context, owner: String) : ViewModel() {
    private val store = NotificationHistoryStore(context.applicationContext, owner)
    private val deleted = mutableMapOf<String, StoredAcademicNotice>()
    private val _data = MutableStateFlow(snapshot())
    val data = _data.asStateFlow()
    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> _data.value = snapshot() }
    init { store.observe(listener) }
    private fun snapshot() = NotificationsInboxData(store.read().map {
        NotificationInboxItem(it.id, it.title, it.body,
            Instant.ofEpochMilli(it.time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd MMM · HH:mm", Locale.forLanguageTag("es-PE"))),
            NotificationSeverity.Info,
            when {
                it.id.startsWith("lab-grade:") -> R.drawable.ic_paquito_lab_profile
                it.id.startsWith("absence:") -> R.drawable.ic_paquito_frame_person
                else -> R.drawable.ic_paquito_bell_outline
            }, createdAtMillis = it.time)
    })
    fun delete(item: NotificationInboxItem) { store.remove(item.id)?.let { deleted[it.id] = it } }
    fun restore(item: NotificationInboxItem) { deleted.remove(item.id)?.let(store::restore) }
    fun deleteAll() { deleted.clear(); store.clear() }
    override fun onCleared() { store.stopObserving(listener) }
}
