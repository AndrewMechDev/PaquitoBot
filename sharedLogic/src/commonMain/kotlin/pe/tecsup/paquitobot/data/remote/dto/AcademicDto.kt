package pe.tecsup.paquitobot.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** `GET /users/self/profile`. */
@Serializable
data class UserProfileDto(
    val id: Int,
    val name: String,
    val email: String,
    val role: String,
)

/** Item de `GET /users/self/courses` (y `/favorites/courses`). */
@Serializable
data class CourseDto(
    val id: Int,
    @SerialName("course_code") val courseCode: String,
    val name: String,
    @SerialName("workflow_state") val workflowState: String,
    @SerialName("enrollment_count") val enrollmentCount: Int,
)

/** Item de `GET /users/self/grades` y `/users/self/courses/{id}/grades`. */
@Serializable
data class GradeDto(
    @SerialName("assignment_id") val assignmentId: Int,
    @SerialName("user_id") val userId: Int,
    val score: Double,
    val grade: String? = null,
    @SerialName("graded_at") val gradedAt: String? = null,
)

/** Item de `GET /users/self/attendance` y `/users/self/courses/{id}/attendance`. */
@Serializable
data class AttendanceRecordDto(
    @SerialName("class_session_id") val classSessionId: Int,
    @SerialName("user_id") val userId: Int,
    val status: String, // "present" | "absent"
)

/** Item de `GET /users/self/courses/{id}/assignments` y `GET /users/self/assignments/{id}`. */
@Serializable
data class AssignmentDto(
    val id: Int,
    @SerialName("course_id") val courseId: Int,
    val name: String,
    val description: String? = null,
    @SerialName("points_possible") val pointsPossible: Double,
    @SerialName("due_at") val dueAt: String? = null,
    @SerialName("workflow_state") val workflowState: String,
)

/** Item de `GET /users/self/courses/{id}/class_sessions`. */
@Serializable
data class ClassSessionDto(
    val id: Int,
    @SerialName("course_id") val courseId: Int,
    @SerialName("start_at") val startAt: String? = null,
    @SerialName("end_at") val endAt: String? = null,
)
