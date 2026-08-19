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

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val courses = repository.courses().getOrDefault(emptyList())
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
                        label = dayLabel(dateKey),
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

    /** "2026-08-17" -> "Lunes 17". Sin libreria de fechas: Zeller's congruence (Gregoriano). */
    private fun dayLabel(dateKey: String): String {
        val parts = dateKey.split("-")
        if (parts.size != 3) return dateKey
        val year = parts[0].toIntOrNull() ?: return dateKey
        val month = parts[1].toIntOrNull() ?: return dateKey
        val day = parts[2].toIntOrNull() ?: return dateKey

        var y = year
        var m = month
        if (m < 3) {
            m += 12
            y -= 1
        }
        val k = y % 100
        val j = y / 100
        val h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
        // h: 0=Sabado, 1=Domingo, 2=Lunes, 3=Martes, 4=Miercoles, 5=Jueves, 6=Viernes
        val weekdayIndex = ZELLER_TO_WEEKDAY_INDEX[h]
        return "${WEEKDAY_NAMES[weekdayIndex]} $day"
    }

    private companion object {
        val WEEKDAY_NAMES = listOf("Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado")
        val ZELLER_TO_WEEKDAY_INDEX = listOf(6, 0, 1, 2, 3, 4, 5)
    }
}
