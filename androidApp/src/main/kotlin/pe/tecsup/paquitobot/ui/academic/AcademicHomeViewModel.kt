package pe.tecsup.paquitobot.ui.academic

import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.domain.academic.AcademicRiskEngine
import pe.tecsup.paquitobot.domain.academic.AcademicRiskKind
import pe.tecsup.paquitobot.ui.home.HomeScreenData
import pe.tecsup.paquitobot.ui.home.HomeFocusItem
import pe.tecsup.paquitobot.ui.home.TaskEntry
import pe.tecsup.paquitobot.ui.home.TaskUrgency
import pe.tecsup.paquitobot.ui.home.WeekDayData
import pe.tecsup.paquitobot.util.AcademicDateUtils

/**
 * Arma [HomeScreenData] a partir del [AcademicRepository]: el calendario
 * semanal es la semana (lunes a domingo) que contiene [today], y las tareas
 * son todas las entregas pendientes sin nota desde [today], incluidas las
 * de semanas posteriores. El calendario permite filtrar la lista por dia.
 *
 * El promedio se obtiene promediando las medias de los cursos con notas.
 * Se muestra como promedio, no como puesto entre alumnos. Los cursos sin
 * calificaciones no aportan ceros al cálculo ni se consideran desaprobados.
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
    importantNotices = emptyList(),
    importantNoticeCount = 0,
)

internal fun buildHomeData(base: HomeScreenData, all: List<CourseWithRecords>, today: String): HomeScreenData {
    val week = weekOf(today)
    val courseAverages = all.mapNotNull { (_, records) ->
        records.grades.takeIf { it.isNotEmpty() }?.map { it.score }?.average()
    }

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
            dateKey = dateKey,
        )
    }

    val pending = all
        .flatMap { (course, records) ->
            pendingAssignments(records.assignments, records.grades, today).map { course to it }
        }
        .sortedBy { (_, item) -> item.dueDate }
    val earliestDueDate = pending.firstOrNull()?.second?.dueDate

    val tasks = pending.map { (course, item) ->
        TaskEntry(
            label = "${course.code} · tarea",
            title = item.assignment.name,
            timestamp = AcademicDateUtils.shortDateLabel(item.dueDate),
            urgency = if (item.dueDate == earliestDueDate) {
                TaskUrgency.Urgent
            } else {
                TaskUrgency.Normal
            },
            dateKey = item.dueDate,
            iconRes = R.drawable.ic_paquito_docs_default,
        )
    }

    val riskNotices = all.flatMap { (course, records) ->
        AcademicRiskEngine.assess(course, records.grades, records.attendance, records.sessions).alerts
    }.sortedByDescending { it.level.ordinal }
        .map { risk ->
            HomeFocusItem(
                title = if (risk.kind == AcademicRiskKind.Grade) "Riesgo de nota" else "Asistencia a vigilar",
                detail = risk.title,
            )
        }
    val allImportantNotices = buildList {
        addAll(riskNotices)
    }
    val importantNotices = allImportantNotices.take(2)

    return base.copy(
        overallRanking = courseAverages.takeIf { it.isNotEmpty() }?.average() ?: 0.0,
        cycleRankings = emptyList(),
        gradedCourseCount = courseAverages.size,
        passingCourseCount = courseAverages.count { it >= 13.0 },
        courseCount = all.size,
        dateLabel = AcademicDateUtils.fullDateLabel(today),
        weekTitle = if (week.isEmpty()) {
            "Semana actual"
        } else {
            "Semana del ${AcademicDateUtils.shortDateLabel(week.first())} al ${AcademicDateUtils.shortDateLabel(week.last())}"
        },
        days = days,
        tasks = tasks,
        pendingCount = pending.size,
        importantNotices = importantNotices,
        importantNoticeCount = allImportantNotices.size,
    )
}
