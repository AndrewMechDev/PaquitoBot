package pe.tecsup.paquitobot.ui.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.auth.CanvasMockKeyStore
import pe.tecsup.paquitobot.data.repository.createDefaultAcademicRepository
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.ui.home.CycleRanking
import pe.tecsup.paquitobot.ui.home.HomeScreenData
import pe.tecsup.paquitobot.ui.home.TaskEntry
import pe.tecsup.paquitobot.ui.home.TaskUrgency
import pe.tecsup.paquitobot.ui.home.WeekDayData
import pe.tecsup.paquitobot.util.AcademicDateUtils

/**
 * Arma [HomeScreenData] REAL a partir de `canvas-mock` (ver skill
 * `canvas-mock-backend`) - reusa el mismo patron de agregacion por curso que
 * [AcademicViewModel]/[AcademicScheduleViewModel]: sesiones de clase para el
 * calendario semanal, assignments con `due_at` para las tareas pendientes.
 *
 * Las cards de ranking (`overallRanking`/`careerCode`/`currentCycle`/
 * `cycleRankings`) son DATO DEMO fijo, NO vienen de canvas-mock - ese
 * backend no tiene ningun concepto de ranking, ciclo academico ni codigo
 * de carrera en sus 8 endpoints (confirmado con el usuario 2026-08-19,
 * ver comentario largo en `PainSnapshotRow` de `HomeScreen.kt`). Los
 * endpoints rollup `GET /users/self/grades` y `GET /users/self/attendance`
 * quedan SIN USAR aca a proposito por el mismo motivo por el que se
 * sacaron las cards de promedio/faltas: ese dato ahora vive solo en
 * Cursos/Horarios, no en el snapshot de Home.
 *
 * Nota sobre "hoy"/urgencia: el proyecto evita deliberadamente una libreria
 * de fechas (ver `AcademicDateUtils`), asi que no hay una nocion confiable
 * de "ahora" mas alla del reloj del dispositivo. En vez de fingir precision
 * con relojes ("12 h", "2 d"), el timestamp de cada tarea muestra la fecha
 * real de entrega, y la urgencia se deriva por orden relativo entre las
 * fechas de entrega reales (el grupo de fecha mas próxima = Urgent).
 */
class AcademicHomeViewModel(
    keyStore: CanvasMockKeyStore,
    private val repository: AcademicRepository = createDefaultAcademicRepository(keyStore),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        HomeScreenData(
            greeting = "¡Bienvenido!",
            dateLabel = "Cargando...",
            weekTitle = "Cargando...",
            days = emptyList(),
            tasksHeader = "Tareas Pendientes",
            tasks = emptyList(),
            pendingCount = 0,
            overallRanking = DEMO_OVERALL_RANKING,
            careerCode = DEMO_CAREER_CODE,
            currentCycle = DEMO_CURRENT_CYCLE,
            cycleRankings = DEMO_CYCLE_RANKINGS,
        ),
    )
    val uiState: StateFlow<HomeScreenData> = _uiState.asStateFlow()

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
            val coursesById = courses.associateBy { it.id }
            val rawEvents = mutableListOf<RawEvent>()

            for (course in courses) {
                val sessions = repository.courseClassSessions(course.id).getOrDefault(emptyList())
                val attendance = repository.courseAttendance(course.id).getOrDefault(emptyList())
                val assignments = repository.courseAssignments(course.id).getOrDefault(emptyList())
                val attendanceBySession = attendance.associateBy { it.classSessionId }

                sessions.forEach { session ->
                    val startAt = session.startAt ?: return@forEach
                    val isAbsent = attendanceBySession[session.id]?.isPresent == false
                    if (isAbsent) {
                        rawEvents += RawEvent(dateKey = startAt.take(10), isAbsence = true)
                    }
                }
                assignments.forEach { assignment ->
                    val dueAt = assignment.dueAt ?: return@forEach
                    rawEvents += RawEvent(
                        dateKey = dueAt.take(10),
                        isAssignment = true,
                        courseId = course.id,
                        assignmentName = assignment.name,
                    )
                }
            }

            val dueDateKeys = rawEvents.filter { it.isAssignment }.map { it.dateKey }.distinct().sorted()
            val earliestDueDateKey = dueDateKeys.firstOrNull()

            val allDateKeys = rawEvents.map { it.dateKey }.distinct().sorted()
            val days = allDateKeys.mapIndexed { index, dateKey ->
                WeekDayData(
                    dayOfWeek = AcademicDateUtils.weekdayName(dateKey),
                    dayNumber = AcademicDateUtils.dayNumber(dateKey),
                    dateLabel = AcademicDateUtils.shortDateLabel(dateKey),
                    isToday = index == 0,
                    isCritical = rawEvents.any { it.dateKey == dateKey && it.isAbsence },
                )
            }

            val tasks = rawEvents.filter { it.isAssignment }.map { event ->
                val course = coursesById[event.courseId]
                TaskEntry(
                    label = course?.let { "${it.code} · tarea" } ?: "Tarea",
                    title = event.assignmentName.orEmpty(),
                    timestamp = AcademicDateUtils.shortDateLabel(event.dateKey),
                    urgency = if (event.dateKey == earliestDueDateKey) TaskUrgency.Urgent else TaskUrgency.Normal,
                    dayNumber = AcademicDateUtils.dayNumber(event.dateKey),
                    iconRes = R.drawable.ic_paquito_docs_default,
                )
            }

            val dueThisWeek = rawEvents.count { it.isAssignment }

            _uiState.update { current ->
                current.copy(
                    dateLabel = allDateKeys.firstOrNull()?.let { AcademicDateUtils.fullDateLabel(it) }
                        ?: "Sin eventos programados",
                    weekTitle = "canvas-mock · ${courses.size} curso(s)",
                    days = days,
                    tasks = tasks,
                    pendingCount = dueThisWeek,
                )
            }
        }
    }

    private fun errorMessageFor(error: Throwable): String =
        "No se pudo cargar canvas-mock: ${error.message ?: "error desconocido"}"

    private data class RawEvent(
        val dateKey: String,
        val isAbsence: Boolean = false,
        val isAssignment: Boolean = false,
        val courseId: Int? = null,
        val assignmentName: String? = null,
    )

    private companion object {
        // Dato demo fijo - ver comentario de la clase. Mismos valores que
        // `HomeScreenData.default()` para que se vea igual con o sin
        // canvas-mock conectado (el ranking no depende de esa conexion).
        const val DEMO_OVERALL_RANKING = 15.0
        const val DEMO_CAREER_CODE = "C-24"
        const val DEMO_CURRENT_CYCLE = 6
        val DEMO_CYCLE_RANKINGS = listOf(
            CycleRanking(cycle = 1, ranking = 14.2),
            CycleRanking(cycle = 2, ranking = 15.6),
            CycleRanking(cycle = 3, ranking = 13.8),
            CycleRanking(cycle = 4, ranking = 16.4),
            CycleRanking(cycle = 5, ranking = 15.1),
        )
    }
}
