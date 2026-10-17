package pe.tecsup.paquitobot.ui.academic

import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.ui.courses.CourseCardData
import pe.tecsup.paquitobot.ui.courses.CoursesScreenData
import pe.tecsup.paquitobot.util.AcademicDateUtils

/**
 * Trae cursos/notas/asistencia del [AcademicRepository] y los mapea al
 * modelo que ya consume `CoursesScreen` (`CourseCardData`).
 *
 * Nota honesta sobre el mapeo: el JSON academico no modela "limite de
 * faltas" ni "promedio ponderado" como conceptos de negocio - se derivan de
 * datos reales donde se puede (nota promedio real, faltas reales contadas) y
 * se usa un limite fijo razonable ([ASSUMED_ABSENCE_LIMIT]) donde el dato no
 * existe. Si un curso no tiene notas cargadas todavia, se muestra "—" en vez
 * de inventar un numero.
 *
 * [today] (`yyyy-MM-dd`) decide que entregas siguen "pendientes"; por
 * defecto es la fecha del dispositivo, y se inyecta para poder fijarla.
 */
class AcademicViewModel(
    private val repository: AcademicRepository,
    private val today: () -> String = ::deviceToday,
) : AcademicLoadingViewModel<CoursesScreenData>(
    CoursesScreenData(termLabel = "Cargando...", pendingCount = 0, courses = emptyList()),
) {

    init {
        load()
    }

    override suspend fun fetch(current: CoursesScreenData): Result<CoursesScreenData> =
        repository.loadAllCourseRecords().map { buildCoursesData(it, today()) }
}

internal fun buildCoursesData(all: List<CourseWithRecords>, today: String): CoursesScreenData =
    CoursesScreenData(
        termLabel = courseCountLabel(all.size),
        // Solo entregas sin nota y con fecha >= hoy (ver `pendingAssignments`).
        pendingCount = all.sumOf { pendingAssignments(it.records.assignments, it.records.grades, today).size },
        courses = all.map { buildCourseCard(it, today) },
    )

private fun buildCourseCard(item: CourseWithRecords, today: String): CourseCardData {
    val (course, records) = item
    val nextDue = pendingAssignments(records.assignments, records.grades, today).firstOrNull()
    return CourseCardData(
        id = course.id.toString(),
        code = course.code,
        name = course.name,
        projectedGrade = averageScoreLabel(records.grades),
        trackHint = if (records.grades.isEmpty()) "Sin notas registradas todavía" else "Promedio de tus notas registradas",
        absences = absenceCount(records.attendance),
        absenceLimit = ASSUMED_ABSENCE_LIMIT,
        nextDueLabel = nextDue
            ?.let { "${it.assignment.name} · ${AcademicDateUtils.shortDateLabel(it.dueDate)}" }
            ?: NO_DUE_LABEL,
    )
}
