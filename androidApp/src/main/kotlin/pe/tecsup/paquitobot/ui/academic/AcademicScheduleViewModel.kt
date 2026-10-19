package pe.tecsup.paquitobot.ui.academic

import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.domain.academic.AcademicSessionKind
import pe.tecsup.paquitobot.ui.schedule.ScheduleDay
import pe.tecsup.paquitobot.ui.schedule.ScheduleEvent
import pe.tecsup.paquitobot.ui.schedule.ScheduleKind
import pe.tecsup.paquitobot.ui.schedule.ScheduleScreenData
import pe.tecsup.paquitobot.util.AcademicDateUtils

/**
 * Arma [ScheduleScreenData] a partir del [AcademicRepository] - combina
 * sesiones de clase, entregas (assignments con fecha) y faltas (asistencia)
 * de TODOS los cursos del estudiante en una sola linea de tiempo por dia.
 *
 * El dia de [today] se marca con `isToday`; el contador de pendientes cuenta
 * entregas sin nota con fecha de hoy o posterior.
 */
class AcademicScheduleViewModel(
    private val repository: AcademicRepository,
    private val today: () -> String = ::deviceToday,
) : AcademicLoadingViewModel<ScheduleScreenData>(
    ScheduleScreenData(weekLabel = "Cargando...", pendingCount = 0, days = emptyList()),
) {

    init {
        load()
    }

    override suspend fun fetch(current: ScheduleScreenData): Result<ScheduleScreenData> =
        repository.loadAllCourseRecords().map { buildScheduleData(it, today()) }
}

private data class RawEvent(
    val dateKey: String,
    val time: String,
    val title: String,
    val subtitle: String,
    val kind: ScheduleKind,
)

internal fun buildScheduleData(all: List<CourseWithRecords>, today: String): ScheduleScreenData {
    val rawEvents = mutableListOf<RawEvent>()

    for ((course, records) in all) {
        val attendanceBySession = records.attendance.associateBy { it.classSessionId }

        records.sessions.forEach { session ->
            val startAt = session.startAt ?: return@forEach
            val isAbsent = attendanceBySession[session.id]?.isPresent == false
            rawEvents += RawEvent(
                dateKey = dateKeyOf(startAt),
                time = timeOf(startAt),
                title = course.name,
                subtitle = when {
                    isAbsent && session.kind == AcademicSessionKind.Laboratory -> "Falta en laboratorio"
                    isAbsent -> "Falta marcada"
                    session.kind == AcademicSessionKind.Laboratory -> "Laboratorio · ${course.code}"
                    else -> course.code
                },
                kind = if (isAbsent) ScheduleKind.Falta else ScheduleKind.Clase,
            )
        }
        records.assignments.forEach { assignment ->
            val dueAt = assignment.dueAt ?: return@forEach
            rawEvents += RawEvent(
                dateKey = dateKeyOf(dueAt),
                time = timeOf(dueAt),
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
                isToday = dateKey == today,
            )
        }

    return ScheduleScreenData(
        weekLabel = courseCountLabel(all.size),
        pendingCount = all.sumOf { pendingAssignments(it.records.assignments, it.records.grades, today).size },
        days = days,
    )
}
