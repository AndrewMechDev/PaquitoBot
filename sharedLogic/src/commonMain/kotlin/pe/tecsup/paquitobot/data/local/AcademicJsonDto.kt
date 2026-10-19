package pe.tecsup.paquitobot.data.local

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Forma del JSON academico ensamblado desde catalogo y escenario. Son tablas
 * planas relacionadas por id: las notas apuntan a una asignacion y la
 * asistencia a una sesion de clase; el repository deriva el curso de cada una.
 * Solo es un detalle de la capa de datos: nunca sale de `data/`.
 */
@Serializable
data class AcademicJsonDto(
    val term: String? = null,
    val profile: ProfileJson,
    val courses: List<CourseJson>,
    val assignments: List<AssignmentJson>,
    val grades: List<GradeJson>,
    @SerialName("class_sessions") val classSessions: List<ClassSessionJson>,
    val attendance: List<AttendanceJson>,
)

@Serializable
data class ProfileJson(
    val id: Int,
    val name: String,
    val email: String,
)

@Serializable
data class CourseJson(
    val id: Int,
    val code: String,
    val name: String,
    @SerialName("enrollment_count") val enrollmentCount: Int,
)

@Serializable
data class AssignmentJson(
    val id: Int,
    @SerialName("course_id") val courseId: Int,
    val name: String,
    @SerialName("points_possible") val pointsPossible: Double,
    /** ISO `yyyy-MM-dd'T'HH:mm:ss`; `null` si la asignacion no tiene fecha limite. */
    @SerialName("due_at") val dueAt: String? = null,
    val kind: String = "delivery",
)

@Serializable
data class GradeJson(
    @SerialName("assignment_id") val assignmentId: Int,
    val score: Double,
)

@Serializable
data class ClassSessionJson(
    val id: Int,
    @SerialName("course_id") val courseId: Int,
    @SerialName("start_at") val startAt: String? = null,
    @SerialName("end_at") val endAt: String? = null,
    val kind: String = "class",
)

@Serializable
data class AttendanceJson(
    @SerialName("class_session_id") val classSessionId: Int,
    /** `"present"` o `"absent"`; cualquier otro valor se trata como dato invalido. */
    val status: String,
)
