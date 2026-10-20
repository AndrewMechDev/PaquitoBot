package pe.tecsup.paquitobot.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json
import pe.tecsup.paquitobot.data.local.AcademicJsonDto
import pe.tecsup.paquitobot.data.local.AttendanceJson
import pe.tecsup.paquitobot.domain.academic.AcademicAssignmentKind
import pe.tecsup.paquitobot.domain.academic.AcademicAssignment
import pe.tecsup.paquitobot.domain.academic.AcademicAttendance
import pe.tecsup.paquitobot.domain.academic.AcademicClassSession
import pe.tecsup.paquitobot.domain.academic.AcademicCourse
import pe.tecsup.paquitobot.domain.academic.AcademicGrade
import pe.tecsup.paquitobot.domain.academic.AcademicProfile
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.domain.academic.AcademicSessionKind

/**
 * Implementacion de [AcademicRepository] que lee un JSON propio incluido en
 * la app (sin HTTP ni backend).
 *
 * [readJson] entrega el JSON ensamblado; cada plataforma decide de donde sale
 * (en Android, catálogo + escenario de la cuenta), asi `commonMain` no depende de APIs de
 * plataforma. El JSON se lee y parsea una sola vez con exito; si la lectura o el
 * parseo fallan, el error se devuelve en el [Result] y la siguiente llamada
 * vuelve a intentarlo (nunca se devuelve una lista vacia para ocultar el fallo).
 */
class LocalJsonAcademicRepository(
    private val readJson: suspend () -> String,
) : AcademicRepository {

    private val json = Json { ignoreUnknownKeys = true }
    private val loadMutex = Mutex()
    private var cached: AcademicJsonDto? = null

    override suspend fun profile(): Result<AcademicProfile> = query { dto ->
        AcademicProfile(id = dto.profile.id, name = dto.profile.name, email = dto.profile.email)
    }

    override suspend fun courses(): Result<List<AcademicCourse>> = query { dto ->
        dto.courses.map { AcademicCourse(it.id, it.code, it.name, it.enrollmentCount) }
    }

    override suspend fun grades(): Result<List<AcademicGrade>> = query { dto ->
        dto.grades.map { AcademicGrade(it.assignmentId, it.score) }
    }

    /**
     * Ignora [days] a proposito: sin libreria de fechas ni reloj inyectado no se
     * puede calcular "los ultimos N dias" de forma confiable, y el JSON ya trae
     * solo la asistencia del ciclo vigente.
     */
    override suspend fun attendance(days: Int): Result<List<AcademicAttendance>> = query { dto ->
        dto.attendance.map { it.toDomain() }
    }

    override suspend fun courseAssignments(courseId: Int): Result<List<AcademicAssignment>> = query { dto ->
        dto.assignments
            .filter { it.courseId == courseId }
            .map { AcademicAssignment(it.id, it.courseId, it.name, it.pointsPossible, it.dueAt,
                when (it.kind) {
                    "laboratory" -> AcademicAssignmentKind.Laboratory
                    "delivery" -> AcademicAssignmentKind.Delivery
                    else -> error("Tipo de actividad académica desconocido")
                }) }
    }

    override suspend fun courseGrades(courseId: Int): Result<List<AcademicGrade>> = query { dto ->
        val assignmentIds = dto.assignments.filter { it.courseId == courseId }.map { it.id }.toSet()
        dto.grades
            .filter { it.assignmentId in assignmentIds }
            .map { AcademicGrade(it.assignmentId, it.score) }
    }

    override suspend fun courseAttendance(courseId: Int): Result<List<AcademicAttendance>> = query { dto ->
        val sessionIds = dto.classSessions.filter { it.courseId == courseId }.map { it.id }.toSet()
        dto.attendance
            .filter { it.classSessionId in sessionIds }
            .map { it.toDomain() }
    }

    override suspend fun courseClassSessions(courseId: Int): Result<List<AcademicClassSession>> = query { dto ->
        dto.classSessions
            .filter { it.courseId == courseId }
            .map { AcademicClassSession(it.id, it.courseId, it.startAt, it.endAt,
                when (it.kind) {
                    "class" -> AcademicSessionKind.Class
                    "laboratory" -> AcademicSessionKind.Laboratory
                    else -> error("Tipo de sesión académica desconocido")
                }) }
    }

    /**
     * Carga el documento y aplica [block]. Equivale a `runCatching`, pero
     * relanza [CancellationException] para no romper la cancelacion de corrutinas.
     */
    private suspend fun <T> query(block: (AcademicJsonDto) -> T): Result<T> =
        try {
            Result.success(block(document()))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }

    private suspend fun document(): AcademicJsonDto = loadMutex.withLock {
        cached ?: json.decodeFromString<AcademicJsonDto>(readJson()).also { cached = it }
    }

    private fun AttendanceJson.toDomain(): AcademicAttendance = AcademicAttendance(
        classSessionId = classSessionId,
        isPresent = when (status) {
            STATUS_PRESENT -> true
            STATUS_ABSENT -> false
            else -> throw IllegalStateException(
                "Estado de asistencia desconocido '$status' en la sesion $classSessionId",
            )
        },
    )

    private companion object {
        const val STATUS_PRESENT = "present"
        const val STATUS_ABSENT = "absent"
    }
}
