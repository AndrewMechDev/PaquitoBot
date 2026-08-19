package pe.tecsup.paquitobot.data.repository

import pe.tecsup.paquitobot.data.remote.CanvasMockApi
import pe.tecsup.paquitobot.data.remote.TokenProvider
import pe.tecsup.paquitobot.data.remote.sharedCanvasMockHttpClient
import pe.tecsup.paquitobot.domain.academic.AcademicAssignment
import pe.tecsup.paquitobot.domain.academic.AcademicAttendance
import pe.tecsup.paquitobot.domain.academic.AcademicCourse
import pe.tecsup.paquitobot.domain.academic.AcademicGrade
import pe.tecsup.paquitobot.domain.academic.AcademicProfile
import pe.tecsup.paquitobot.domain.academic.AcademicRepository

/** Implementacion real de [AcademicRepository]: consume `canvas-mock` via [CanvasMockApi]. */
class RemoteAcademicRepository(
    private val api: CanvasMockApi,
) : AcademicRepository {
    override suspend fun profile(): Result<AcademicProfile> =
        api.profile().map { AcademicProfile(id = it.id, name = it.name, email = it.email) }

    override suspend fun courses(): Result<List<AcademicCourse>> =
        api.courses().map { list ->
            list.map { AcademicCourse(it.id, it.courseCode, it.name, it.enrollmentCount) }
        }

    override suspend fun grades(): Result<List<AcademicGrade>> =
        api.grades().map { list -> list.map { AcademicGrade(it.assignmentId, it.score) } }

    override suspend fun attendance(days: Int): Result<List<AcademicAttendance>> =
        api.attendance(days).map { list ->
            list.map { AcademicAttendance(it.classSessionId, it.status == "present") }
        }

    override suspend fun courseAssignments(courseId: Int): Result<List<AcademicAssignment>> =
        api.courseAssignments(courseId).map { list ->
            list.map { AcademicAssignment(it.id, it.courseId, it.name, it.pointsPossible, it.dueAt) }
        }

    override suspend fun courseGrades(courseId: Int): Result<List<AcademicGrade>> =
        api.courseGrades(courseId).map { list -> list.map { AcademicGrade(it.assignmentId, it.score) } }

    override suspend fun courseAttendance(courseId: Int): Result<List<AcademicAttendance>> =
        api.courseAttendance(courseId).map { list ->
            list.map { AcademicAttendance(it.classSessionId, it.status == "present") }
        }
}

/** Arma el [AcademicRepository] real. [tokenProvider] provee la API key mock (`stu_001`, etc.). */
fun createDefaultAcademicRepository(tokenProvider: TokenProvider): AcademicRepository =
    RemoteAcademicRepository(
        api = CanvasMockApi(
            httpClient = sharedCanvasMockHttpClient(),
            tokenProvider = tokenProvider,
        ),
    )
