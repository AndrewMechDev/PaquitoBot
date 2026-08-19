package pe.tecsup.paquitobot.domain.academic

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

data class AcademicAssignment(
    val id: Int,
    val courseId: Int,
    val name: String,
    val pointsPossible: Double,
    val dueAt: String?,
)

/**
 * Puerto de dominio para datos academicos - hoy implementado por
 * `canvas-mock` (ver skill `canvas-mock-backend`), a futuro por la API
 * real de Canvas sin cambiar este contrato.
 */
interface AcademicRepository {
    suspend fun profile(): Result<AcademicProfile>
    suspend fun courses(): Result<List<AcademicCourse>>
    suspend fun grades(): Result<List<AcademicGrade>>
    suspend fun attendance(days: Int = 30): Result<List<AcademicAttendance>>
    suspend fun courseAssignments(courseId: Int): Result<List<AcademicAssignment>>
    suspend fun courseGrades(courseId: Int): Result<List<AcademicGrade>>
    suspend fun courseAttendance(courseId: Int): Result<List<AcademicAttendance>>
}
