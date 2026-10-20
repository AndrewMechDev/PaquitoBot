package pe.tecsup.paquitobot.domain.academic

const val ACADEMIC_ABSENCE_LIMIT = 8

/** Modelos de dominio para datos academicos (cursos, notas, asistencia, asignaciones). */
data class AcademicProfile(
    val id: Int,
    val name: String,
    val email: String,
)

data class AcademicCourse(
    val id: Int,
    val code: String,
    val name: String,
    val enrollmentCount: Int,
)

data class AcademicGrade(
    val assignmentId: Int,
    val score: Double,
)

data class AcademicAttendance(
    val classSessionId: Int,
    val isPresent: Boolean,
)

enum class AcademicAssignmentKind { Delivery, Laboratory }

data class AcademicAssignment(
    val id: Int,
    val courseId: Int,
    val name: String,
    val pointsPossible: Double,
    val dueAt: String?,
    val kind: AcademicAssignmentKind = AcademicAssignmentKind.Delivery,
)

data class AcademicClassSession(
    val id: Int,
    val courseId: Int,
    val startAt: String?,
    val endAt: String?,
    val kind: AcademicSessionKind = AcademicSessionKind.Class,
)

enum class AcademicSessionKind { Class, Laboratory }

/**
 * Puerto de dominio para datos academicos - hoy implementado leyendo un
 * JSON propio incluido en la app, sin backend. Otra fuente (por ejemplo
 * una API) puede reemplazarlo sin cambiar este contrato.
 */
interface AcademicRepository {
    suspend fun profile(): Result<AcademicProfile>
    suspend fun courses(): Result<List<AcademicCourse>>
    suspend fun grades(): Result<List<AcademicGrade>>
    suspend fun attendance(days: Int = 30): Result<List<AcademicAttendance>>
    suspend fun courseAssignments(courseId: Int): Result<List<AcademicAssignment>>
    suspend fun courseGrades(courseId: Int): Result<List<AcademicGrade>>
    suspend fun courseAttendance(courseId: Int): Result<List<AcademicAttendance>>
    suspend fun courseClassSessions(courseId: Int): Result<List<AcademicClassSession>>
}
