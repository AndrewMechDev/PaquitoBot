package pe.tecsup.paquitobot.ui.calendar

import android.app.PendingIntent
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.identity.AuthorizationResult
import com.google.android.gms.common.api.ApiException
import com.clerk.api.Clerk
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.data.calendar.*
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.domain.calendar.*
import java.time.Instant

enum class CalendarPhase { Loading, Ready, Authorizing, Connecting, Syncing, Revoking }
data class CalendarUiState(val phase: CalendarPhase = CalendarPhase.Loading,
    val candidates: List<CalendarCandidate> = emptyList(), val selected: Set<String> = emptySet(),
    val showPast: Boolean = false, val skipped: Int = 0, val email: String? = null,
    val resolution: PendingIntent? = null, val message: String? = null, val report: CalendarSyncReport? = null) {
    val busy: Boolean get() = phase != CalendarPhase.Ready
}
/** Credenciales de Google solo en memoria, ligadas al usuario de Clerk que abrió la pantalla. */
internal class CalendarViewModel(private val academic: AcademicRepository,
    private val authorization: GoogleCalendarAuthorization, private val owner: String,
    private val stillAuthorized: () -> Boolean) : ViewModel() {
    private val state = MutableStateFlow(CalendarUiState())
    val uiState = state.asStateFlow()
    private var token: String? = null
    private var account: GoogleCalendarAccount? = null
    private var client: GoogleCalendarClient? = null
    @Volatile private var generation = 0
    private var job: Job? = null
    private var sessionWasLost = false
    init {
        refresh()
        viewModelScope.launch {
            combine(Clerk.isInitialized, Clerk.userFlow, Clerk.sessionFlow) { _, _, _ -> stillAuthorized() }
                .distinctUntilChanged().collect { allowed ->
                    if (!allowed) {
                        sessionWasLost = true
                        generation++; job?.cancel(); token = null; client = null; account = null
                        state.value = CalendarUiState(phase = CalendarPhase.Ready, message = "La sesión cambió. Vuelve a iniciar sesión.")
                    } else if (sessionWasLost) {
                        sessionWasLost = false
                        refresh()
                    }
                }
        }
    }

    fun refresh() {
        if (state.value.busy && job?.isActive == true) return
        job = viewModelScope.launch {
            state.value = state.value.copy(phase = CalendarPhase.Loading, message = null)
            try {
                val profile = academic.profile().getOrThrow()
                val now = Instant.now()
                val candidates = mutableListOf<CalendarCandidate>()
                var skipped = 0
                for (course in academic.courses().getOrThrow()) {
                    val graded = academic.courseGrades(course.id).getOrThrow().map { it.assignmentId }.toSet()
                    for (task in academic.courseAssignments(course.id).getOrThrow().filter { it.id !in graded }) {
                        val candidate = task.dueAt?.let { CalendarEventPlanner.candidate(CalendarTask(profile.id, course.id, task.id, task.name, course.code, it), now) }
                        if (candidate == null) skipped++ else candidates += candidate
                    }
                }
                val sorted = candidates.distinctBy { it.key }.sortedBy { it.start }
                state.value = state.value.copy(phase = CalendarPhase.Ready, candidates = sorted,
                    selected = sorted.filter { !it.past }.map { it.key }.toSet(), skipped = skipped, report = null)
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) { state.value = state.value.copy(phase = CalendarPhase.Ready, message = "No pudimos cargar las tareas. Pulsa actualizar para reintentar.") }
        }
    }
    fun showPast(value: Boolean) {
        if (state.value.busy) return
        val selected = if (value) state.value.selected else state.value.selected - state.value.candidates.filter { it.past }.map { it.key }.toSet()
        state.value = state.value.copy(showPast = value, selected = selected)
    }
    fun select(key: String) {
        if (state.value.busy || state.value.candidates.none { it.key == key && (!it.past || state.value.showPast) }) return
        val current = state.value.selected
        state.value = state.value.copy(selected = if (key in current) current - key else current + key)
    }
    fun authorize() {
        if (state.value.busy || !stillAuthorized()) return
        generation++
        val attempt = generation
        token = null; account = null; client = null
        state.value = state.value.copy(phase = CalendarPhase.Authorizing, email = null, message = null, report = null)
        job = viewModelScope.launch {
            try {
                val result = authorization.authorize()
                if (attempt != generation || !stillAuthorized()) return@launch
                if (result.hasResolution()) {
                    val resolution = result.pendingIntent ?: throw CalendarServiceException(CalendarError.Unknown)
                    state.value = state.value.copy(resolution = resolution)
                } else connect(result, attempt)
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) { fail(error) }
        }
    }
    fun resolutionLaunched() { state.value = state.value.copy(resolution = null) }
    fun resolve(data: Intent?) {
        if (state.value.phase != CalendarPhase.Authorizing || !stillAuthorized()) return
        if (data == null) { state.value = state.value.copy(phase = CalendarPhase.Ready, resolution = null, message = "Autorización cancelada. No se enviaron tareas."); return }
        job = viewModelScope.launch {
            try { connect(authorization.resolve(data), generation)
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) { fail(error) }
        }
    }
    fun resolutionFailed() { state.value = state.value.copy(phase = CalendarPhase.Ready, resolution = null, message = "No pudimos abrir la autorización de Google. Inténtalo de nuevo.") }
    private suspend fun connect(result: AuthorizationResult, attempt: Int) {
        if (!result.grantedScopes.contains(GoogleCalendarAuthorization.CALENDAR_SCOPE)) throw CalendarServiceException(CalendarError.Permission)
        val access = result.accessToken?.takeIf { it.isNotBlank() } ?: throw CalendarServiceException(CalendarError.Expired)
        state.value = state.value.copy(phase = CalendarPhase.Connecting, resolution = null)
        token = access
        val connected = GoogleCalendarClient(access) { stillAuthorized() && generation == attempt }
        val google = connected.account()
        if (!stillAuthorized() || generation != attempt) throw CalendarServiceException(CalendarError.Expired)
        token = access; client = connected; account = google
        state.value = state.value.copy(phase = CalendarPhase.Ready, email = google.email,
            message = "Cuenta conectada. Revisa las tareas y pulsa sincronizar para enviarlas.")
    }
    fun sync() {
        val gateway = client ?: return
        val google = account ?: return
        if (state.value.busy || state.value.selected.isEmpty() || !stillAuthorized()) return
        val snapshot = state.value
        val events = snapshot.candidates.filter { it.key in snapshot.selected }.map { CalendarEventPlanner.event(it, owner, google.subject) }
        state.value = state.value.copy(phase = CalendarPhase.Syncing, message = null, report = null)
        job = viewModelScope.launch {
            try {
                val report = CalendarSyncEngine(gateway).sync(events, stillAuthorized)
                if (report.error == CalendarError.Expired) invalidate()
                state.value = state.value.copy(phase = CalendarPhase.Ready, report = report,
                    message = report.error?.let(::calendarErrorMessage))
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (error: Exception) { fail(error) }
        }
    }
    fun disconnect() {
        val google = account ?: return
        if (state.value.busy) return
        state.value = state.value.copy(phase = CalendarPhase.Revoking, message = null)
        job = viewModelScope.launch {
            try {
                authorization.revoke(google.email)
                generation++; token = null; client = null; account = null
                state.value = state.value.copy(phase = CalendarPhase.Ready, email = null, report = null,
                    message = "Acceso revocado. Los eventos ya creados permanecen en Google Calendar.")
            } catch (cancelled: CancellationException) { throw cancelled
            } catch (_: Exception) { state.value = state.value.copy(phase = CalendarPhase.Ready, message = "No pudimos revocar el acceso. Revisa internet y vuelve a intentarlo.") }
        }
    }
    private suspend fun invalidate() {
        val previous = token
        generation++; token = null; client = null; account = null
        state.value = state.value.copy(email = null)
        if (previous != null) try { authorization.clearToken(previous) } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { /* Reautorizar al próximo intento. */ }
    }
    private suspend fun fail(error: Exception) {
        val kind = when (error) {
            is CalendarServiceException -> error.kind
            is ApiException -> when (error.statusCode) { 10 -> CalendarError.Configuration; 7 -> CalendarError.Network; else -> CalendarError.Permission }
            else -> CalendarError.Unknown
        }
        if (kind == CalendarError.Expired) invalidate()
        state.value = state.value.copy(phase = CalendarPhase.Ready, resolution = null, message = calendarErrorMessage(kind))
    }
    override fun onCleared() { generation++; token = null; client = null; account = null; super.onCleared() }
}
internal fun calendarErrorMessage(kind: CalendarError): String = when (kind) {
    CalendarError.Expired -> "La autorización venció o cambió la sesión. Vuelve a conectar Google."
    CalendarError.Permission -> "No se concedió acceso a Calendar o tu organización lo restringe. Puedes volver a conectar."
    CalendarError.Configuration -> "La conexión de Google no está disponible para esta versión. Contacta al equipo de PaquitoBot."
    CalendarError.RateLimit -> "Google alcanzó el límite de solicitudes. Espera antes de volver a sincronizar."
    CalendarError.Network -> "No pudimos comunicarnos con Google. Revisa tu conexión y vuelve a intentarlo."
    CalendarError.Conflict -> "Un evento cambió o no pertenece a esta integración. No se sobrescribió; revisa Calendar y reintenta."
    CalendarError.Deleted -> "Un evento fue eliminado en Google Calendar. Restáuralo allí antes de volver a sincronizarlo."
    CalendarError.Unknown -> "No se pudo completar la operación. Puedes volver a intentarlo."
}
