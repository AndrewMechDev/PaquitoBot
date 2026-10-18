package pe.tecsup.paquitobot.domain.calendar

/** Tarea académica independiente de la plataforma o del proveedor de calendario. */
data class CalendarTask(val profileId: Int, val courseId: Int, val assignmentId: Int,
    val title: String, val course: String, val dueAt: String)
data class CalendarEvent(val id: String, val sourceKey: String, val title: String,
    val description: String, val start: String, val end: String, val allDay: Boolean, val timeZone: String)
enum class CalendarWrite { Created, Updated, Unchanged }
enum class CalendarError { Expired, Permission, Configuration, RateLimit, Network, Conflict, Deleted, Unknown }
class CalendarServiceException(val kind: CalendarError) : Exception(kind.name)
interface CalendarGateway { suspend fun upsert(event: CalendarEvent): CalendarWrite }
data class CalendarSyncReport(val created: Int = 0, val updated: Int = 0, val unchanged: Int = 0,
    val failed: Int = 0, val remaining: Int = 0, val error: CalendarError? = null)

/** Una escritura por tarea; las excepciones de cancelación no se capturan. */
class CalendarSyncEngine(private val gateway: CalendarGateway) {
    suspend fun sync(events: List<CalendarEvent>, stillAuthorized: () -> Boolean): CalendarSyncReport {
        var report = CalendarSyncReport()
        val unique = events.distinctBy { it.id }
        for ((index, event) in unique.withIndex()) {
            if (!stillAuthorized()) return report.copy(remaining = unique.size - index, error = CalendarError.Expired)
            try {
                report = when (gateway.upsert(event)) {
                    CalendarWrite.Created -> report.copy(created = report.created + 1)
                    CalendarWrite.Updated -> report.copy(updated = report.updated + 1)
                    CalendarWrite.Unchanged -> report.copy(unchanged = report.unchanged + 1)
                }
            } catch (error: CalendarServiceException) {
                val failed = report.copy(failed = report.failed + 1, error = error.kind)
                if (error.kind in listOf(CalendarError.Expired, CalendarError.Permission, CalendarError.Configuration,
                        CalendarError.RateLimit, CalendarError.Network)) return failed.copy(remaining = unique.size - index - 1)
                report = failed
            }
        }
        return report
    }
}
