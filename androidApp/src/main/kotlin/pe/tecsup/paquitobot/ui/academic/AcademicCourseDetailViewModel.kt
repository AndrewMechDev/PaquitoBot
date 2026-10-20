package pe.tecsup.paquitobot.ui.academic

import pe.tecsup.paquitobot.domain.academic.AcademicAssignment
import pe.tecsup.paquitobot.domain.academic.AcademicGrade
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.domain.academic.AcademicSessionKind
import pe.tecsup.paquitobot.ui.courses.AttendanceEntry
import pe.tecsup.paquitobot.ui.courses.CourseCardData
import pe.tecsup.paquitobot.ui.courses.CourseDetailData
import pe.tecsup.paquitobot.ui.courses.EvaluationItem
import pe.tecsup.paquitobot.ui.courses.EvaluationState
import pe.tecsup.paquitobot.util.AcademicDateUtils

/**
 * Arma [CourseDetailData] a partir del [AcademicRepository] para UN curso
 * especifico.
 *
 * Nota honesta sobre "Practicas"/"Laboratorios" vs "Pendientes": el JSON
 * academico NO categoriza sus entregas, y adivinar la categoria por el
 * nombre seria inventar una estructura que el dato no tiene. Por eso las 2
 * secciones categorizadas quedan vacias (y no se muestran) y TODAS las
 * entregas del curso van en una sola seccion ([CourseDetailData.pendingTitle]
 * = "Entregas", no "Pendientes" - incluye tanto calificadas como sin
 * calificar).
 */
class AcademicCourseDetailViewModel(
    private val repository: AcademicRepository,
    private val course: CourseCardData,
    private val today: () -> String = ::deviceToday,
) : AcademicLoadingViewModel<CourseDetailData>(initialDetail(course)) {

    private val courseId: Int? = course.id.toIntOrNull()

    init {
        load()
    }

    override suspend fun fetch(current: CourseDetailData): Result<CourseDetailData> {
        val id = courseId ?: return Result.failure(IllegalArgumentException("Curso sin id numerico: ${course.id}"))
        return repository.loadCourseRecords(id).map { buildCourseDetail(course, it, today()) }
    }
}

private fun initialDetail(course: CourseCardData) = CourseDetailData(
    course = course,
    headline = "Cargando...",
    projectionHint = "",
    attendance = emptyList(),
    practices = emptyList(),
    labs = emptyList(),
    pending = emptyList(),
    pendingTitle = "Entregas",
)

internal fun buildCourseDetail(course: CourseCardData, records: CourseRecords, today: String): CourseDetailData {
    val gradeByAssignment = records.grades.associateBy { it.assignmentId }
    val sessionById = records.sessions.associateBy { it.id }

    val attendanceEntries = records.attendance
        .mapNotNull { record ->
            val session = sessionById[record.classSessionId] ?: return@mapNotNull null
            val dateKey = session.startAt?.let(::dateKeyOf) ?: return@mapNotNull null
            Triple(dateKey, record.isPresent, session.kind)
        }
        .sortedBy { it.first }
        .map { (dateKey, isPresent, kind) -> AttendanceEntry(AcademicDateUtils.shortDateLabel(dateKey), isPresent,
            if (kind == AcademicSessionKind.Laboratory) "Laboratorio" else null) }

    val pendingCount = pendingAssignments(records.assignments, records.grades, today).size

    return CourseDetailData(
        course = course.copy(absences = absenceCount(records.attendance), absenceLimit = ASSUMED_ABSENCE_LIMIT),
        headline = "${records.assignments.size} entrega(s)",
        projectionHint = if (pendingCount > 0) {
            "Tienes $pendingCount entrega(s) pendiente(s) en este curso."
        } else {
            "No tienes entregas pendientes en este curso."
        },
        attendance = attendanceEntries,
        practices = emptyList(),
        labs = emptyList(),
        pending = records.assignments.map { evaluationItemOf(it, gradeByAssignment[it.id], today) },
        pendingTitle = "Entregas",
    )
}

private fun evaluationItemOf(assignment: AcademicAssignment, grade: AcademicGrade?, today: String): EvaluationItem {
    val dueDate = assignment.dueAt?.let(::dateKeyOf)
    val dueLabel = dueDate?.let { AcademicDateUtils.shortDateLabel(it) }
    val isOverdue = dueDate != null && dueDate < today
    val scoresLabel = when {
        grade != null -> "${"%.0f".format(grade.score)} / ${"%.0f".format(assignment.pointsPossible)}"
        dueLabel == null -> "Sin fecha"
        isOverdue -> "Venció $dueLabel"
        else -> "Vence $dueLabel"
    }
    return EvaluationItem(
        name = assignment.name,
        weightLabel = "${assignment.pointsPossible.toInt()} pts",
        scoresLabel = scoresLabel,
        state = if (grade != null) EvaluationState.Graded else EvaluationState.Pending,
        detail = when {
            dueLabel == null -> "Sin fecha de entrega definida."
            isOverdue -> "Venció: $dueLabel"
            else -> "Vence: $dueLabel"
        },
    )
}
