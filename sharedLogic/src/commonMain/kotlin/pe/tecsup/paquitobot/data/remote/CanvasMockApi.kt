package pe.tecsup.paquitobot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException
import pe.tecsup.paquitobot.data.remote.dto.AssignmentDto
import pe.tecsup.paquitobot.data.remote.dto.AttendanceRecordDto
import pe.tecsup.paquitobot.data.remote.dto.ClassSessionDto
import pe.tecsup.paquitobot.data.remote.dto.CourseDto
import pe.tecsup.paquitobot.data.remote.dto.GradeDto
import pe.tecsup.paquitobot.data.remote.dto.UserProfileDto

/**
 * Cliente delgado de `canvas-mock` - ver skill `canvas-mock-backend`.
 *
 * Solo GET, todo bajo `/api/v1/users/self/...`. Autenticacion: header
 * `X-Api-Key` unicamente ([tokenProvider] provee la key mock, ej.
 * `stu_001`). El backend tambien exige `Authorization: Bearer <jwt>` en
 * escrituras (rutas bajo `/admin`), pero esta app de estudiante NUNCA
 * escribe - no se implementa manejo de JWT aca a proposito.
 */
class CanvasMockApi(
    private val httpClient: HttpClient,
    private val tokenProvider: TokenProvider,
    private val baseUrl: String = CANVAS_MOCK_BASE_URL,
) {
    suspend fun profile(): Result<UserProfileDto> =
        get("/api/v1/users/self/profile")

    suspend fun courses(): Result<List<CourseDto>> =
        get("/api/v1/users/self/courses")

    suspend fun grades(): Result<List<GradeDto>> =
        get("/api/v1/users/self/grades")

    suspend fun attendance(days: Int = 30): Result<List<AttendanceRecordDto>> =
        get("/api/v1/users/self/attendance?days=$days")

    suspend fun courseAssignments(courseId: Int): Result<List<AssignmentDto>> =
        get("/api/v1/users/self/courses/$courseId/assignments")

    suspend fun courseClassSessions(courseId: Int): Result<List<ClassSessionDto>> =
        get("/api/v1/users/self/courses/$courseId/class_sessions")

    suspend fun courseGrades(courseId: Int): Result<List<GradeDto>> =
        get("/api/v1/users/self/courses/$courseId/grades")

    suspend fun courseAttendance(courseId: Int): Result<List<AttendanceRecordDto>> =
        get("/api/v1/users/self/courses/$courseId/attendance")

    private suspend inline fun <reified T> get(path: String): Result<T> {
        val key = tokenProvider.getToken()
            ?: return Result.failure(CanvasMockApiError.NotConnected)

        return try {
            val response = httpClient.get("$baseUrl$path") {
                header("X-Api-Key", key)
            }
            mapResponse(response)
        } catch (exc: HttpRequestTimeoutException) {
            Result.failure(CanvasMockApiError.NetworkFailure(exc))
        } catch (exc: IOException) {
            Result.failure(CanvasMockApiError.NetworkFailure(exc))
        }
    }

    private suspend inline fun <reified T> mapResponse(response: HttpResponse): Result<T> =
        when (response.status) {
            HttpStatusCode.OK -> Result.success(response.body())
            HttpStatusCode.Unauthorized -> Result.failure(CanvasMockApiError.InvalidApiKey)
            HttpStatusCode.Forbidden -> Result.failure(CanvasMockApiError.NotEnrolled)
            HttpStatusCode.NotFound -> Result.failure(CanvasMockApiError.NotFound)
            else -> Result.failure(
                CanvasMockApiError.Unknown(
                    statusCode = response.status.value,
                    detail = runCatching { response.body<String>() }.getOrNull(),
                ),
            )
        }
}
