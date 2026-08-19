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
import pe.tecsup.paquitobot.ui.courses.CourseCardData
import pe.tecsup.paquitobot.ui.courses.CoursesScreenData

/**
 * Trae cursos/notas/asistencia REALES de `canvas-mock` y los mapea al
 * modelo que ya consume `CoursesScreen` (`CourseCardData`) - ver skill
 * `canvas-mock-backend`.
 *
 * Nota honesta sobre el mapeo: canvas-mock no modela "limite de faltas"
 * ni "promedio ponderado" como conceptos de negocio (son ideas del
 * frontend, no del backend mock) - se derivan de datos reales donde se
 * puede (nota promedio real, faltas reales contadas) y se usa un limite
 * fijo razonable donde el backend no tiene ese dato. Si un curso no tiene
 * notas cargadas todavia (la seed de canvas-mock no trae notas por
 * default), se muestra "—" en vez de inventar un numero.
 */
class AcademicViewModel(
    keyStore: CanvasMockKeyStore,
    private val repository: AcademicRepository = createDefaultAcademicRepository(keyStore),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoursesScreenData(termLabel = "Cargando...", pendingCount = 0, courses = emptyList()))
    val uiState: StateFlow<CoursesScreenData> = _uiState.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            repository.courses()
                .onSuccess { courses ->
                    val cards = courses.map { course -> loadCourseCard(course.id, course.code, course.name) }
                    _uiState.update {
                        CoursesScreenData(
                            termLabel = "canvas-mock · ${courses.size} curso(s)",
                            pendingCount = cards.count { it.nextDueLabel != NO_DUE_LABEL },
                            courses = cards,
                        )
                    }
                    _errorMessage.update { null }
                }
                .onFailure { error ->
                    _errorMessage.update { errorMessageFor(error) }
                }
        }
    }

    private suspend fun loadCourseCard(courseId: Int, code: String, name: String): CourseCardData {
        val grades = repository.courseGrades(courseId).getOrDefault(emptyList())
        val attendance = repository.courseAttendance(courseId).getOrDefault(emptyList())
        val assignments = repository.courseAssignments(courseId).getOrDefault(emptyList())

        val projectedGrade = if (grades.isEmpty()) {
            "—"
        } else {
            val avg = grades.map { it.score }.average()
            "%.1f".format(avg)
        }
        val absences = attendance.count { !it.isPresent }
        val nextDue = assignments
            .filter { it.dueAt != null }
            .minByOrNull { it.dueAt!! }
            ?.let { "${it.name} · ${it.dueAt?.take(10)}" }
            ?: NO_DUE_LABEL

        return CourseCardData(
            id = courseId.toString(),
            code = code,
            name = name,
            projectedGrade = projectedGrade,
            trackHint = if (grades.isEmpty()) "Sin notas registradas todavía" else "Promedio real de canvas-mock",
            absences = absences,
            absenceLimit = ASSUMED_ABSENCE_LIMIT,
            nextDueLabel = nextDue,
        )
    }

    private fun errorMessageFor(error: Throwable): String =
        "No se pudo cargar canvas-mock: ${error.message ?: "error desconocido"}"

    private companion object {
        const val NO_DUE_LABEL = "Sin pendientes"
        // canvas-mock no modela un limite de faltas por curso (es una regla
        // de negocio del frontend/institucion, no del backend mock) - 8 es
        // un valor de referencia razonable, no un dato real del backend.
        const val ASSUMED_ABSENCE_LIMIT = 8
    }
}
