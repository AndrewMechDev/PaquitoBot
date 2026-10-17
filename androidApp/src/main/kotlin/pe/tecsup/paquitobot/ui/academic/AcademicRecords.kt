package pe.tecsup.paquitobot.ui.academic

import pe.tecsup.paquitobot.domain.academic.AcademicAssignment
import pe.tecsup.paquitobot.domain.academic.AcademicAttendance
import pe.tecsup.paquitobot.domain.academic.AcademicClassSession
import pe.tecsup.paquitobot.domain.academic.AcademicCourse
import pe.tecsup.paquitobot.domain.academic.AcademicGrade
import pe.tecsup.paquitobot.domain.academic.AcademicRepository

/** Todo lo que el repositorio sabe de UN curso, ya cargado. */
internal data class CourseRecords(
    val sessions: List<AcademicClassSession>,
    val attendance: List<AcademicAttendance>,
    val assignments: List<AcademicAssignment>,
    val grades: List<AcademicGrade>,
)

internal data class CourseWithRecords(
    val course: AcademicCourse,
    val records: CourseRecords,
)

/**
 * Carga los registros de un curso. Si CUALQUIER consulta falla, devuelve ese
 * fallo: nunca se sustituye por una lista vacia (eso ocultaba el error y
 * mostraba datos incompletos como si fueran validos).
 */
internal suspend fun AcademicRepository.loadCourseRecords(courseId: Int): Result<CourseRecords> {
    val sessions = courseClassSessions(courseId).getOrElse { return Result.failure(it) }
    val attendance = courseAttendance(courseId).getOrElse { return Result.failure(it) }
    val assignments = courseAssignments(courseId).getOrElse { return Result.failure(it) }
    val grades = courseGrades(courseId).getOrElse { return Result.failure(it) }
    return Result.success(CourseRecords(sessions, attendance, assignments, grades))
}

/** Carga todos los cursos con sus registros; mismo criterio: un fallo cancela toda la carga. */
internal suspend fun AcademicRepository.loadAllCourseRecords(): Result<List<CourseWithRecords>> {
    val courses = courses().getOrElse { return Result.failure(it) }
    val all = courses.map { course ->
        val records = loadCourseRecords(course.id).getOrElse { return Result.failure(it) }
        CourseWithRecords(course, records)
    }
    return Result.success(all)
}
