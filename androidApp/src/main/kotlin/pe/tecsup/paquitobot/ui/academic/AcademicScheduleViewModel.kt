package pe.tecsup.paquitobot.ui.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.CanvasMockKeyStore
import pe.tecsup.paquitobot.data.repository.createDefaultAcademicRepository
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.util.AcademicDateUtils
import pe.tecsup.paquitobot.ui.schedule.ScheduleDay
import pe.tecsup.paquitobot.ui.schedule.ScheduleEvent
import pe.tecsup.paquitobot.ui.schedule.ScheduleKind
import pe.tecsup.paquitobot.ui.schedule.ScheduleScreenData

/**
 * Arma [ScheduleScreenData] REAL a partir de `canvas-mock` (ver skill
 * `canvas-mock-backend`) - combina sesiones de clase, entregas
 * (assignments con `due_at`) y faltas (asistencia real) de TODOS los
 * cursos del estudiante en una sola linea de tiempo por dia, igual que
 * el mock hardcodeado pero con datos reales.
 */
class AcademicScheduleViewModel(
    keyStore: CanvasMockKeyStore,
    private val repository: AcademicRepository = createDefaultAcademicRepository(keyStore),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        ScheduleScreenData(weekLabel = "Cargando...", pendingCount = 0, days = emptyList()),
    )
    val uiState: StateFlow<ScheduleScreenData> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val courses = repository.courses()
                .onSuccess { _errorMessage.update { null } }
                .onFailure { error -> _errorMessage.update { errorMessageFor(error) } }
                .getOrDefault(emptyList())
            val rawEvents = mutableListOf<RawEvent>()

            for (course in courses) {
                val sessions = repository.courseClassSessions(course.id).getOrDefault(emptyList())
                val attendance = repository.courseAttendance(course.id).getOrDefault(emptyList())
                val assignments = repository.courseAssignments(course.id).getOrDefault(emptyList())
                val attendanceBySession = attendance.associateBy { it.classSessionId }

                sessions.forEach { session ->
                    val startAt = session.startAt ?: return@forEach
                    val isAbsent = attendanceBySession[session.id]?.isPresent == false
                    rawEvents += RawEvent(
                        dateKey = startAt.take(10),
                        time = timeFrom(startAt),
                        title = course.name,
                        subtitle = if (isAbsent) "Falta marcada" else course.code,
                        kind = if (isAbsent) ScheduleKind.Falta else ScheduleKind.Clase,
                    )
                }
                assignments.forEach { assignment ->
                    val dueAt = assignment.dueAt ?: return@forEach
                    rawEvents += RawEvent(
                        dateKey = dueAt.take(10),
                        time = timeFrom(dueAt),
                        title = assignment.name,
                        subtitle = course.name,
                        kind = ScheduleKind.Entrega,
                    )
                }
            }

            val days = rawEvents
                .groupBy { it.dateKey }
                .toSortedMap()
                .map { (dateKey, events) ->
                    ScheduleDay(
                        label = AcademicDateUtils.weekdayWithDay(dateKey),
                        events = events.sortedBy { it.time }.map { ScheduleEvent(it.time, it.title, it.subtitle, it.kind) },
                    )
                }

            _uiState.update {
                ScheduleScreenData(
                    weekLabel = "canvas-mock · ${courses.size} curso(s)",
                    pendingCount = rawEvents.count { it.kind == ScheduleKind.Entrega },
                    days = days,
                )
            }
        }
    }

    private data class RawEvent(
        val dateKey: String,
        val time: String,
        val title: String,
        val subtitle: String,
        val kind: ScheduleKind,
    )

    private fun timeFrom(isoDateTime: String): String =
        isoDateTime.drop(11).take(5).ifBlank { "--:--" }

    private fun errorMessageFor(error: Throwable): String =
        "No se pudo cargar canvas-mock: ${error.message ?: "error desconocido"}"
}
