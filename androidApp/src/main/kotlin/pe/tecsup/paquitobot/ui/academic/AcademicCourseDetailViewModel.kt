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
import pe.tecsup.paquitobot.ui.courses.AttendanceEntry
import pe.tecsup.paquitobot.ui.courses.CourseCardData
import pe.tecsup.paquitobot.ui.courses.CourseDetailData
import pe.tecsup.paquitobot.ui.courses.EvaluationItem
import pe.tecsup.paquitobot.ui.courses.EvaluationState
import pe.tecsup.paquitobot.util.AcademicDateUtils

/**
 * Arma [CourseDetailData] REAL a partir de `canvas-mock` para UN curso
 * especifico (ver skill `canvas-mock-backend`).
 *
 * Nota honesta sobre "Practicas"/"Laboratorios" vs "Pendientes": canvas-mock
 * NO categoriza sus assignments (confirmado leyendo el seed: nombres como
 * "Variables and Types", "Limits" - temas academicos, no "Practica N"/
 * "Lab N"). Adivinar la categoria por el nombre seria inventar una
 * estructura que el backend no tiene. Por eso con datos reales las 2
 * secciones categorizadas quedan vacias (y no se muestran) y TODAS las
 * entregas del curso van en una sola seccion ([pendingTitle] = "Entregas",
 * no "Pendientes" - incluye tanto calificadas como sin calificar).
 */
class AcademicCourseDetailViewModel(
    keyStore: CanvasMockKeyStore,
    private val course: CourseCardData,
    private val repository: AcademicRepository = createDefaultAcademicRepository(keyStore),
) : ViewModel() {

    private val courseId: Int? = course.id.toIntOrNull()

    private val _uiState = MutableStateFlow(
        CourseDetailData(
            course = course,
            headline = "Cargando...",
            projectionHint = "",
            attendance = emptyList(),
            practices = emptyList(),
            labs = emptyList(),
            pending = emptyList(),
            pendingTitle = "Entregas",
        ),
    )
    val uiState: StateFlow<CourseDetailData> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        load()
    }

    fun load() {
        val id = courseId ?: return
        viewModelScope.launch {
            val sessions = repository.courseClassSessions(id).getOrDefault(emptyList())
            val attendance = repository.courseAttendance(id).getOrDefault(emptyList())
            val assignments = repository.courseAssignments(id)
                .onSuccess { _errorMessage.update { null } }
                .onFailure { error -> _errorMessage.update { errorMessageFor(error) } }
                .getOrDefault(emptyList())
            val grades = repository.courseGrades(id).getOrDefault(emptyList())
            val gradeByAssignment = grades.associateBy { it.assignmentId }

            val sessionDateById = sessions.associate { it.id to it.startAt?.take(10) }
            val attendanceEntries = attendance
                .mapNotNull { record ->
                    val dateKey = sessionDateById[record.classSessionId] ?: return@mapNotNull null
                    dateKey to record.isPresent
                }
                .sortedBy { it.first }
                .map { (dateKey, isPresent) -> AttendanceEntry(AcademicDateUtils.shortDateLabel(dateKey), isPresent) }

            val entregas = assignments.map { assignment ->
                val grade = gradeByAssignment[assignment.id]
                val dueAt = assignment.dueAt
                val scoresLabel = when {
                    grade != null -> "${"%.0f".format(grade.score)} / ${"%.0f".format(assignment.pointsPossible)}"
                    dueAt != null -> "Vence ${AcademicDateUtils.shortDateLabel(dueAt.take(10))}"
                    else -> "Sin fecha"
                }
                EvaluationItem(
                    name = assignment.name,
                    weightLabel = "${assignment.pointsPossible.toInt()} pts",
                    scoresLabel = scoresLabel,
                    state = if (grade != null) EvaluationState.Graded else EvaluationState.Pending,
                    detail = assignment.dueAt
                        ?.let { "Vence: ${AcademicDateUtils.shortDateLabel(it.take(10))}" }
                        ?: "Sin fecha de entrega definida en canvas-mock.",
                )
            }

            val used = attendanceEntries.count { !it.isPresent }
            val pendingCount = entregas.count { it.state == EvaluationState.Pending }

            _uiState.update {
                CourseDetailData(
                    course = course.copy(absences = used, absenceLimit = ASSUMED_ABSENCE_LIMIT),
                    headline = "${assignments.size} entrega(s) · canvas-mock",
                    projectionHint = if (pendingCount > 0) {
                        "Tenés $pendingCount entrega(s) sin calificar en este curso."
                    } else {
                        "Todas las entregas de este curso están calificadas."
                    },
                    attendance = attendanceEntries,
                    practices = emptyList(),
                    labs = emptyList(),
                    pending = entregas,
                    pendingTitle = "Entregas",
                )
            }
        }
    }

    private fun errorMessageFor(error: Throwable): String =
        "No se pudo cargar canvas-mock: ${error.message ?: "error desconocido"}"

    private companion object {
        // canvas-mock no modela "limite de faltas" - mismo valor de
        // referencia usado en AcademicViewModel/AcademicHomeViewModel.
        const val ASSUMED_ABSENCE_LIMIT = 8
    }
}
