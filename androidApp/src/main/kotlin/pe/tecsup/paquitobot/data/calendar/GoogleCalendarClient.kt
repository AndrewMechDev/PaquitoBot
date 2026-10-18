package pe.tecsup.paquitobot.data.calendar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import pe.tecsup.paquitobot.domain.calendar.*
import java.io.IOException
import java.net.URL
import java.net.HttpURLConnection
import java.time.OffsetDateTime

internal data class GoogleCalendarAccount(val subject: String, val email: String)
/** Token de corta duración solo en memoria. No se incluyen cuerpos de error en excepciones o logs. */
internal class GoogleCalendarClient(private val token: String, private val stillAuthorized: () -> Boolean) : CalendarGateway {
    suspend fun account(): GoogleCalendarAccount = withContext(Dispatchers.IO) {
        val response = request("GET", "https://www.googleapis.com/oauth2/v3/userinfo")
        if (response.code != 200) fail(response)
        val json = response.json
        val subject = json.optString("sub")
        val email = json.optString("email")
        if (subject.isBlank() || email.isBlank() || !json.optBoolean("email_verified")) throw CalendarServiceException(CalendarError.Permission)
        GoogleCalendarAccount(subject, email)
    }
    override suspend fun upsert(event: CalendarEvent): CalendarWrite = withContext(Dispatchers.IO) {
        val url = "https://www.googleapis.com/calendar/v3/calendars/primary/events"
        var existing = request("GET", "$url/${event.id}")
        if (existing.code == 404) {
            val created = request("POST", "$url?sendUpdates=none", payload(event, JSONObject()).put("id", event.id))
            if (created.code in 200..299) return@withContext CalendarWrite.Created
            if (created.code != 409) fail(created)
            // Puede ser el reintento de una creación cuya respuesta se perdió.
            existing = request("GET", "$url/${event.id}")
        }
        if (existing.code == 410 || existing.json.optString("status") == "cancelled") throw CalendarServiceException(CalendarError.Deleted)
        if (existing.code != 200) fail(existing)
        val json = existing.json
        val marker = json.optJSONObject("extendedProperties")?.optJSONObject("private")
        if (marker?.optString("paquitobotSource") != event.sourceKey || marker.optString("paquitobotManaged") != "v1") {
            throw CalendarServiceException(CalendarError.Conflict)
        }
        if (matches(json, event)) return@withContext CalendarWrite.Unchanged
        val etag = json.optString("etag").takeIf { it.isNotBlank() } ?: throw CalendarServiceException(CalendarError.Conflict)
        // GET + PUT conserva campos no administrados por PaquitoBot y usa ETag contra cambios simultáneos.
        val updated = request("PUT", "$url/${event.id}?sendUpdates=none&conferenceDataVersion=1&supportsAttachments=true", payload(event, json), etag)
        if (updated.code !in 200..299) fail(updated)
        CalendarWrite.Updated
    }
    private fun matches(json: JSONObject, event: CalendarEvent): Boolean =
        json.optString("summary") == event.title && json.optString("description") == event.description &&
            sameTime(json.optJSONObject("start"), event.start, event.allDay) && sameTime(json.optJSONObject("end"), event.end, event.allDay)
    private fun sameTime(json: JSONObject?, expected: String, allDay: Boolean): Boolean = try {
        if (allDay) json?.optString("date") == expected
        else json?.optString("dateTime")?.let { OffsetDateTime.parse(it).toInstant() } == OffsetDateTime.parse(expected).toInstant()
    } catch (_: Exception) { false }
    private fun payload(event: CalendarEvent, base: JSONObject): JSONObject = base.apply {
        put("summary", event.title); put("description", event.description)
        val field = if (event.allDay) "date" else "dateTime"
        put("start", JSONObject().put(field, event.start).apply { if (!event.allDay) put("timeZone", event.timeZone) })
        put("end", JSONObject().put(field, event.end).apply { if (!event.allDay) put("timeZone", event.timeZone) })
        val properties = optJSONObject("extendedProperties") ?: JSONObject()
        val private = properties.optJSONObject("private") ?: JSONObject()
        private.put("paquitobotSource", event.sourceKey).put("paquitobotManaged", "v1")
        put("extendedProperties", properties.put("private", private))
    }
    private data class Response(val code: Int, val json: JSONObject)
    private fun request(method: String, target: String, body: JSONObject? = null, etag: String? = null): Response {
        if (!stillAuthorized()) throw CalendarServiceException(CalendarError.Expired)
        val connection = URL(target).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = method
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 15_000; connection.readTimeout = 20_000
            connection.setRequestProperty("Authorization", "Bearer $token")
            connection.setRequestProperty("Accept", "application/json")
            etag?.let { connection.setRequestProperty("If-Match", it) }
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            }
            val code = connection.responseCode
            val text = (if (code in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader()?.use { it.readText() }.orEmpty()
            val json = try { JSONObject(text) } catch (_: Exception) { JSONObject() }
            return Response(code, json)
        } catch (_: IOException) { throw CalendarServiceException(CalendarError.Network)
        } finally { connection.disconnect() }
    }
    private fun fail(response: Response): Nothing {
        val error = response.json.optJSONObject("error")
        val reason = error?.optJSONArray("errors")?.optJSONObject(0)?.optString("reason")
        val kind = when {
            response.code == 401 -> CalendarError.Expired
            reason in listOf("accessNotConfigured", "serviceDisabled") -> CalendarError.Configuration
            response.code == 429 || reason in listOf("rateLimitExceeded", "userRateLimitExceeded", "quotaExceeded") -> CalendarError.RateLimit
            response.code == 403 -> CalendarError.Permission
            response.code == 409 || response.code == 412 -> CalendarError.Conflict
            response.code == 410 -> CalendarError.Deleted
            response.code >= 500 -> CalendarError.Network
            else -> CalendarError.Unknown
        }
        throw CalendarServiceException(kind)
    }
}
