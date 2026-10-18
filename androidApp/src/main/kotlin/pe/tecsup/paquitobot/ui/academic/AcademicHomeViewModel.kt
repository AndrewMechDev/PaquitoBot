package pe.tecsup.paquitobot.ui.academic

import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.ui.home.HomeScreenData
import pe.tecsup.paquitobot.ui.home.TaskEntry
import pe.tecsup.paquitobot.ui.home.TaskUrgency
import pe.tecsup.paquitobot.ui.home.WeekDayData
import pe.tecsup.paquitobot.util.AcademicDateUtils

/**
 * Arma [HomeScreenData] a partir del [AcademicRepository]: el calendario
 * semanal es la semana (lunes a domingo) que contiene [today], y las tareas
 * son las entregas PENDIENTES (sin nota y con fecha >= hoy) de esa semana.
 *
 * Las cards de ranking (`overallRanking`/`careerCode`/`currentCycle`/
 * `cycleRankings`) son DATO DEMO fijo que se hereda de
 * [HomeScreenData.default]: el JSON academico local no tiene ningun concepto
 * de ranking, ciclo academico ni codigo de carrera.
 *
 * Nota sobre la urgencia: el proyecto evita deliberadamente una libreria de
 * fechas (ver `AcademicDateUtils`), asi que en vez de fingir precision con
 * relojes ("12 h", "2 d") el timestamp de cada tarea muestra la fecha real de
 * entrega, y la urgencia se deriva por orden relativo entre las fechas de
 * entrega pendientes (el grupo de fecha mas proxima = Urgent).
 */
class AcademicHomeViewModel(
    private val repository: AcademicRepository,
    private val today: () -> String = ::deviceToday,
) : AcademicLoadingViewModel<HomeScreenData>(initialHome()) {

    init {
        load()
    }

    override suspend fun fetch(current: HomeScreenData): Result<HomeScreenData> =
        repository.loadAllCourseRecords().map { buildHomeData(current, it, today()) }
}

private fun initialHome(): HomeScreenData = HomeScreenData.default().copy(
    greeting = "¡Bienvenido!",
    dateLabel = "Cargando...",
    weekTitle = "Cargando...",
    days = emptyList(),
    tasks = emptyList(),
    pendingCount = 0,
)

internal fun buildHomeData(base: HomeScreenData, all: List<CourseWithRecords>, today: String): HomeScreenData {
    val week = weekOf(today)
    val weekDates = week.toSet()

    // Dias con al menos una falta (sesion de clase con asistencia en `false`).
    val absenceDates = all.flatMap { (_, records) ->
        val sessionById = records.sessions.associateBy { it.id }
        records.attendance
            .filter { !it.isPresent }
            .mapNotNull { sessionById[it.classSessionId]?.startAt?.let(::dateKeyOf) }
    }.toSet()

    val days = week.map { dateKey ->
        WeekDayData(
            dayOfWeek = AcademicDateUtils.weekdayName(dateKey),
            dayNumber = AcademicDateUtils.dayNumber(dateKey),
            dateLabel = AcademicDateUtils.shortDateLabel(dateKey),
            isToday = dateKey == today,
            isCritical = dateKey in absenceDates,
        )
    }

    val pending = all
        .flatMap { (course, records) ->
            pendingAssignments(records.assignments, records.grades, today).map { course to it }
        }
        .sortedBy { (_, item) -> item.dueDate }
    val earliestDueDate = pending.firstOrNull()?.second?.dueDate

    val tasks = pending
        .filter { (_, item) -> item.dueDate in weekDates }
        .map { (course, item) ->
            TaskEntry(
                label = "${course.code} · tarea",
                title = item.assignment.name,
                timestamp = AcademicDateUtils.shortDateLabel(item.dueDate),
                urgency = if (item.dueDate == earliestDueDate) TaskUrgency.Urgent else TaskUrgency.Normal,
                dayNumber = AcademicDateUtils.dayNumber(item.dueDate),
                iconRes = R.drawable.ic_paquito_docs_default,
            )
        }

    return base.copy(
        dateLabel = AcademicDateUtils.fullDateLabel(today),
        weekTitle = if (week.isEmpty()) {
            "Semana actual"
        } else {
            "Semana del ${AcademicDateUtils.shortDateLabel(week.first())} al ${AcademicDateUtils.shortDateLabel(week.last())}"
        },
        days = days,
        tasks = tasks,
        pendingCount = pending.size,
    )
}
