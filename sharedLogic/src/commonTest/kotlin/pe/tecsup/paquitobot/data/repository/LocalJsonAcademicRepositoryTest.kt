package pe.tecsup.paquitobot.data.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import pe.tecsup.paquitobot.domain.academic.AcademicRepository

class LocalJsonAcademicRepositoryTest {

    private val fixture = """
        {
          "term": "Ciclo de prueba",
          "profile": { "id": 7, "name": "Estudiante Demo", "email": "demo@example.com" },
          "courses": [
            { "id": 1, "code": "AAA-I", "name": "Curso A", "enrollment_count": 20 },
            { "id": 2, "code": "BBB-I", "name": "Curso B", "enrollment_count": 25 }
          ],
          "assignments": [
            { "id": 11, "course_id": 1, "name": "Practica 1", "points_possible": 20.0, "due_at": "2026-07-10T23:59:00" },
            { "id": 12, "course_id": 1, "name": "Sin fecha", "points_possible": 10.0 },
            { "id": 21, "course_id": 2, "name": "Lab 1", "points_possible": 20.0, "due_at": "2026-08-10T23:59:00" }
          ],
          "grades": [
            { "assignment_id": 11, "score": 16.5 },
            { "assignment_id": 21, "score": 12.0 }
          ],
          "class_sessions": [
            { "id": 101, "course_id": 1, "start_at": "2026-07-06T08:00:00", "end_at": "2026-07-06T10:00:00" },
            { "id": 201, "course_id": 2, "start_at": "2026-07-07T10:00:00", "end_at": "2026-07-07T12:00:00" }
          ],
          "attendance": [
            { "class_session_id": 101, "status": "present" },
            { "class_session_id": 201, "status": "absent" }
          ]
        }
    """.trimIndent()

    private fun repo(json: String = fixture): AcademicRepository =
        LocalJsonAcademicRepository(readJson = { json })

    @Test
    fun profileIsMapped() = runTest {
        val profile = repo().profile().getOrThrow()
        assertEquals(7, profile.id)
        assertEquals("Estudiante Demo", profile.name)
        assertEquals("demo@example.com", profile.email)
    }

    @Test
    fun coursesAreMapped() = runTest {
        val courses = repo().courses().getOrThrow()
        assertEquals(listOf(1, 2), courses.map { it.id })
        assertEquals("AAA-I", courses.first().code)
        assertEquals(25, courses.last().enrollmentCount)
    }

    @Test
    fun courseAssignmentsAreFilteredByCourseAndKeepNullDueDate() = runTest {
        val assignments = repo().courseAssignments(1).getOrThrow()
        assertEquals(listOf(11, 12), assignments.map { it.id })
        assertEquals("2026-07-10T23:59:00", assignments.first().dueAt)
        assertEquals(null, assignments.last().dueAt)
        assertTrue(repo().courseAssignments(99).getOrThrow().isEmpty())
    }

    @Test
    fun gradesAreGlobalAndPerCourse() = runTest {
        val repo = repo()
        assertEquals(listOf(11, 21), repo.grades().getOrThrow().map { it.assignmentId })
        assertEquals(listOf(11), repo.courseGrades(1).getOrThrow().map { it.assignmentId })
        assertEquals(12.0, repo.courseGrades(2).getOrThrow().single().score)
    }

    @Test
    fun classSessionsAreFilteredByCourse() = runTest {
        val sessions = repo().courseClassSessions(2).getOrThrow()
        assertEquals(listOf(201), sessions.map { it.id })
        assertEquals("2026-07-07T10:00:00", sessions.single().startAt)
        assertEquals("2026-07-07T12:00:00", sessions.single().endAt)
    }

    @Test
    fun attendanceMapsStatusAndFiltersByCourse() = runTest {
        val repo = repo()
        val all = repo.attendance().getOrThrow()
        assertEquals(listOf(true, false), all.map { it.isPresent })
        val course1 = repo.courseAttendance(1).getOrThrow()
        assertEquals(listOf(101), course1.map { it.classSessionId })
        assertTrue(course1.single().isPresent)
        assertFalse(repo.courseAttendance(2).getOrThrow().single().isPresent)
    }

    @Test
    fun malformedJsonReturnsFailureInsteadOfEmptyList() = runTest {
        val repo = repo("{ esto no es json")
        assertTrue(repo.courses().isFailure)
        assertTrue(repo.profile().isFailure)
    }

    @Test
    fun unknownAttendanceStatusReturnsFailure() = runTest {
        val broken = fixture.replace("\"absent\"", "\"tarde\"")
        assertTrue(repo(broken).attendance().isFailure)
    }

    @Test
    fun readFailureIsReportedAndNotCached() = runTest {
        var calls = 0
        val repo = LocalJsonAcademicRepository(readJson = {
            calls++
            if (calls == 1) error("asset no disponible") else fixture
        })
        assertTrue(repo.courses().isFailure)
        assertEquals(2, repo.courses().getOrThrow().size)
        assertEquals(2, calls)
    }

    @Test
    fun jsonIsReadOnlyOnceAfterSuccess() = runTest {
        var calls = 0
        val repo = LocalJsonAcademicRepository(readJson = {
            calls++
            fixture
        })
        repo.profile().getOrThrow()
        repo.courses().getOrThrow()
        repo.attendance().getOrThrow()
        assertEquals(1, calls)
    }

    // --- Asset real (androidApp/src/main/assets/academic.json) ---

    private fun readRealAsset(): String {
        val relative = "androidApp/src/main/assets/academic.json"
        // Gradle ejecuta los tests con el directorio del modulo (sharedLogic) como cwd.
        val path = listOf("../$relative", relative)
            .map { Path(it) }
            .firstOrNull { SystemFileSystem.exists(it) }
            ?: error("No se encontro $relative desde el directorio de trabajo del test")
        return SystemFileSystem.source(path).buffered().use { it.readString() }
    }

    @Test
    fun realAssetParsesAndIsReferentiallyConsistent() = runTest {
        val repo = LocalJsonAcademicRepository(readJson = { readRealAsset() })

        val profile = repo.profile().getOrThrow()
        assertTrue(profile.name.isNotBlank())

        val courses = repo.courses().getOrThrow()
        assertTrue(courses.size in 4..5, "se esperaban 4 o 5 cursos, hay ${courses.size}")
        val courseIds = courses.map { it.id }.toSet()
        assertEquals(courses.size, courseIds.size, "ids de curso duplicados")
        // Mismos codigos que las pantallas mock (CoursesScreenData.default()).
        assertTrue(listOf("CALC-II", "FIS-I", "ALG-I").all { code -> courses.any { it.code == code } })

        val assignments = courses.flatMap { repo.courseAssignments(it.id).getOrThrow() }
        assertTrue(assignments.all { it.courseId in courseIds })
        val assignmentIds = assignments.map { it.id }
        assertEquals(assignmentIds.size, assignmentIds.toSet().size, "ids de asignacion duplicados")

        val grades = repo.grades().getOrThrow()
        assertTrue(grades.isNotEmpty())
        assertTrue(grades.all { it.assignmentId in assignmentIds }, "nota sin asignacion")
        assertEquals(grades.size, courses.sumOf { repo.courseGrades(it.id).getOrThrow().size })

        val sessions = courses.flatMap { repo.courseClassSessions(it.id).getOrThrow() }
        assertTrue(sessions.all { it.courseId in courseIds })
        val sessionIds = sessions.map { it.id }
        assertEquals(sessionIds.size, sessionIds.toSet().size, "ids de sesion duplicados")
        assertTrue(sessions.all { it.startAt != null && it.endAt != null && it.startAt < it.endAt })

        val attendance = repo.attendance().getOrThrow()
        assertTrue(attendance.isNotEmpty())
        assertTrue(attendance.all { it.classSessionId in sessionIds }, "asistencia sin sesion")
        assertEquals(attendance.size, courses.sumOf { repo.courseAttendance(it.id).getOrThrow().size })
    }

    @Test
    fun realAssetHasGradedUpcomingAndOverdueAssignments() = runTest {
        val repo = LocalJsonAcademicRepository(readJson = { readRealAsset() })
        val gradedIds = repo.grades().getOrThrow().map { it.assignmentId }.toSet()
        val assignments = repo.courses().getOrThrow().flatMap { repo.courseAssignments(it.id).getOrThrow() }

        // Referencia fija para verificar variedad del dataset: lunes 2 de noviembre de 2026.
        val today = "2026-11-02T00:00:00"
        assertTrue(assignments.any { it.id in gradedIds }, "falta una asignacion calificada")
        assertTrue(
            assignments.any { it.id !in gradedIds && it.dueAt != null && it.dueAt >= today },
            "falta una asignacion pendiente",
        )
        assertTrue(
            assignments.any { it.id !in gradedIds && it.dueAt != null && it.dueAt < today },
            "falta una asignacion vencida",
        )

        // Sesiones repartidas en la semana del 2 al 8 de noviembre.
        val sessionDays = repo.courses().getOrThrow()
            .flatMap { repo.courseClassSessions(it.id).getOrThrow() }
            .mapNotNull { it.startAt?.take(10) }
            .filter { it in "2026-11-02".."2026-11-08" }
            .toSet()
        assertTrue(sessionDays.size >= 4, "las sesiones de la semana cubren solo ${sessionDays.size} dias")
    }
}
