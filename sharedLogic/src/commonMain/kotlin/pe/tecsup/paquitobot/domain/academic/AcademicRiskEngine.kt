package pe.tecsup.paquitobot.domain.academic

/**
 * Politicas de alerta temprana usadas por la demostracion. Se mantienen
 * separadas de los datos del estudiante para poder reemplazarlas por la
 * reglamentacion institucional o por reglas entregadas por el backend.
 */
object AcademicRiskPolicy {
    const val PASSING_GRADE = 13.0
    const val RECOVERY_GRADE = 12.0
    const val EVALUATIONS_PER_TERM = 8
    const val SINGLE_COMPONENT_ABSENCE_LIMIT = 4
    const val DUAL_COMPONENT_ABSENCE_LIMIT = 8
}

enum class AcademicRiskLevel { Stable, Watch, Urgent, Critical }

enum class AcademicRiskKind { Grade, Attendance }

data class AcademicRiskAlert(
    val courseId: Int,
    val courseCode: String,
    val courseName: String,
    val kind: AcademicRiskKind,
    val level: AcademicRiskLevel,
    val title: String,
    val message: String,
    val actionLabel: String,
    val currentValue: Double,
    val limitValue: Double,
)

data class AcademicCourseRisk(
    val grade: AcademicRiskAlert?,
    val attendance: AcademicRiskAlert?,
) {
    val alerts: List<AcademicRiskAlert> = listOfNotNull(grade, attendance)
}

/**
 * Motor puro: no depende de Android, UI ni almacenamiento. La "proyeccion"
 * es el promedio actual sostenido hasta las ocho evaluaciones del ciclo; no
 * afirma una nota final real ni sustituye la decision de un docente.
 */
object AcademicRiskEngine {
    fun attendanceLimit(sessions: List<AcademicClassSession>): Int =
        if (sessions.any { it.kind == AcademicSessionKind.Laboratory }) {
            AcademicRiskPolicy.DUAL_COMPONENT_ABSENCE_LIMIT
        } else {
            AcademicRiskPolicy.SINGLE_COMPONENT_ABSENCE_LIMIT
        }

    fun assess(
        course: AcademicCourse,
        grades: List<AcademicGrade>,
        attendance: List<AcademicAttendance>,
        sessions: List<AcademicClassSession>,
    ): AcademicCourseRisk = AcademicCourseRisk(
        grade = gradeAlert(course, grades),
        attendance = attendanceAlert(course, attendance, sessions),
    )

    private fun gradeAlert(course: AcademicCourse, grades: List<AcademicGrade>): AcademicRiskAlert? {
        if (grades.isEmpty()) return null
        val evaluated = grades.take(AcademicRiskPolicy.EVALUATIONS_PER_TERM)
        val average = evaluated.map { it.score }.average()
        val assessed = evaluated.size
        val remaining = (AcademicRiskPolicy.EVALUATIONS_PER_TERM - assessed).coerceAtLeast(0)
        val neededAverage = if (remaining == 0) null else
            ((AcademicRiskPolicy.PASSING_GRADE * AcademicRiskPolicy.EVALUATIONS_PER_TERM) - evaluated.sumOf { it.score }) / remaining

        return when {
            average < AcademicRiskPolicy.RECOVERY_GRADE -> AcademicRiskAlert(
                course.id, course.code, course.name, AcademicRiskKind.Grade, AcademicRiskLevel.Critical,
                "Riesgo de desaprobar ${course.code}",
                gradeMessage(average, assessed, neededAverage, "Tu proyección está por debajo de 12."),
                "Contactar a mi tutora", average, AcademicRiskPolicy.PASSING_GRADE,
            )
            average < AcademicRiskPolicy.PASSING_GRADE -> AcademicRiskAlert(
                course.id, course.code, course.name, AcademicRiskKind.Grade, AcademicRiskLevel.Urgent,
                "Mejora tu promedio en ${course.code}",
                gradeMessage(average, assessed, neededAverage, "Aún estás por debajo de la meta de 13. La recuperación se determina al cierre del curso."),
                "Contactar a mi tutora", average, AcademicRiskPolicy.PASSING_GRADE,
            )
            average < AcademicRiskPolicy.PASSING_GRADE + 1 -> AcademicRiskAlert(
                course.id, course.code, course.name, AcademicRiskKind.Grade, AcademicRiskLevel.Watch,
                "Vigila tu promedio en ${course.code}",
                "Tu proyección es ${format(average)}. Mantén tus próximas evaluaciones sobre 13.",
                "Ver mi curso", average, AcademicRiskPolicy.PASSING_GRADE,
            )
            else -> null
        }
    }

    private fun attendanceAlert(
        course: AcademicCourse,
        attendance: List<AcademicAttendance>,
        sessions: List<AcademicClassSession>,
    ): AcademicRiskAlert? {
        val absences = attendance.count { !it.isPresent }
        val hasLaboratory = sessions.any { it.kind == AcademicSessionKind.Laboratory }
        val limit = attendanceLimit(sessions)
        val component = if (hasLaboratory) "teoría y laboratorio" else "este curso"

        return when {
            absences > limit -> AcademicRiskAlert(
                course.id, course.code, course.name, AcademicRiskKind.Attendance, AcademicRiskLevel.Critical,
                "Límite de faltas superado en ${course.code}",
                "Registras $absences faltas de $limit permitidas en $component. Comunícate con tu tutora hoy.",
                "Contactar a mi tutora", absences.toDouble(), limit.toDouble(),
            )
            absences == limit -> AcademicRiskAlert(
                course.id, course.code, course.name, AcademicRiskKind.Attendance, AcademicRiskLevel.Urgent,
                "Estás en el límite de faltas", "${course.name}: $absences de $limit faltas permitidas. Una falta más puede ponerte en riesgo.",
                "Contactar a mi tutora", absences.toDouble(), limit.toDouble(),
            )
            absences == limit - 1 -> AcademicRiskAlert(
                course.id, course.code, course.name, AcademicRiskKind.Attendance, AcademicRiskLevel.Watch,
                "Te queda una falta disponible", "${course.name}: llevas $absences de $limit faltas permitidas en $component.",
                "Ver mi curso", absences.toDouble(), limit.toDouble(),
            )
            else -> null
        }
    }

    private fun gradeMessage(average: Double, assessed: Int, neededAverage: Double?, prefix: String): String {
        val nextStep = when {
            assessed >= AcademicRiskPolicy.EVALUATIONS_PER_TERM -> "Consulta con tu tutora las alternativas del curso."
            neededAverage == null -> ""
            neededAverage > 20.0 -> "Incluso con 20 en las evaluaciones restantes no llegarías a 13; conversa con tu tutora."
            else -> "Necesitas promediar ${format(neededAverage)} en tus ${remainingLabel(assessed)} evaluaciones restantes para llegar a 13."
        }
        return "Tus $assessed notas registradas promedian ${format(average)} de 20. La meta es 13. $prefix $nextStep Cálculo orientativo con ocho evaluaciones de igual peso; revisa los pesos de tu sílabo."
    }

    private fun remainingLabel(assessed: Int): Int = (AcademicRiskPolicy.EVALUATIONS_PER_TERM - assessed).coerceAtLeast(0)

    private fun format(value: Double): String {
        val rounded = (value * 10).toInt() / 10.0
        return if (rounded % 1.0 == 0.0) rounded.toInt().toString() else rounded.toString()
    }
}
