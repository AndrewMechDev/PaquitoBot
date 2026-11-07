package pe.tecsup.paquitobot.ui.teacher

import java.util.Locale

internal data class TeacherDemoGrade(
    val week: Int,
    val label: String,
    val score: Double,
)

internal data class TeacherDemoLabSubnote(
    val labId: String,
    val label: String,
    val week: Int,
    val part: Int,
    val requiredParts: Int,
    val score: Double,
)

internal enum class TeacherDemoAttendanceSignal { None, Warning, Di }

internal data class TeacherDemoStudentCourse(
    val studentId: String,
    val displayName: String,
    val email: String?,
    val weeklyGrades: List<TeacherDemoGrade>,
    val labSubnotes: List<TeacherDemoLabSubnote>,
    val absenceWeeks: Set<Int>,
    val tardyWeeks: Set<Int> = emptySet(),
    val performedClasses: Int = 11,
    val courseComplete: Boolean = false,
) {
    val completedLabGrades: List<Pair<String, Double>>
        get() = labSubnotes.groupBy { it.labId }.values.mapNotNull { parts ->
            val first = parts.first()
            if (parts.size != first.requiredParts || parts.map { it.part }.toSet().size != first.requiredParts) {
                null
            } else {
                first.label to parts.map { it.score }.average()
            }
        }

    val partialLabs: List<List<TeacherDemoLabSubnote>>
        get() = labSubnotes.groupBy { it.labId }.values.filter { parts ->
            val first = parts.first()
            parts.size != first.requiredParts || parts.map { it.part }.toSet().size != first.requiredParts
        }

    val finalGrades: List<Double>
        get() = weeklyGrades.map { it.score } + completedLabGrades.map { it.second }

    val average: Double?
        get() = finalGrades.takeIf { it.isNotEmpty() }?.average()

    val gradeRisk: Boolean
        get() = average?.let { it < PASSING_GRADE } == true

    val absenceRate: Double
        get() = if (performedClasses == 0) 0.0 else absenceWeeks.size.toDouble() / performedClasses

    val attendanceSignal: TeacherDemoAttendanceSignal
        get() = when {
            absenceRate > DI_THRESHOLD -> TeacherDemoAttendanceSignal.Di
            absenceRate >= WARNING_THRESHOLD -> TeacherDemoAttendanceSignal.Warning
            else -> TeacherDemoAttendanceSignal.None
        }

    val hasAnyRisk: Boolean
        get() = gradeRisk || attendanceSignal != TeacherDemoAttendanceSignal.None

    companion object {
        const val PASSING_GRADE = 13.0
        const val WARNING_THRESHOLD = 0.20
        const val DI_THRESHOLD = 0.30
    }
}

internal data class TeacherDemoCourse(
    val id: String,
    val name: String,
    val careerCode: String,
    val area: String,
    val students: List<TeacherDemoStudentCourse>,
)

internal data class TeacherDemoCareer(val code: String, val name: String)

internal object TeacherDemoData {
    val careers = listOf(
        TeacherDemoCareer("C-24", "Diseño y Desarrollo de Software"),
        TeacherDemoCareer("C-21", "Gestión y Mantenimiento de Maquinaria Pesada"),
    )

    private fun grades(scores: List<Int>, label: String = "Evaluación") = scores.mapIndexed { index, score ->
        TeacherDemoGrade(index + 1, label, score.toDouble())
    }

    private fun student(
        id: String,
        name: String,
        email: String?,
        scores: List<Int> = emptyList(),
        absenceWeeks: Set<Int>,
        tardyWeeks: Set<Int> = emptySet(),
    ) = TeacherDemoStudentCourse(
        studentId = id,
        displayName = name,
        email = email,
        weeklyGrades = grades(scores),
        labSubnotes = emptyList(),
        absenceWeeks = absenceWeeks,
        tardyWeeks = tardyWeeks,
    )

    private val andrew = "andrew.alca@tecsup.edu.pe"
    private val joshua = "joshua.salazar@tecsup.edu.pe"

    val courses = listOf(
        TeacherDemoCourse(
            id = "calculo-ii",
            name = "Cálculo II",
            careerCode = "C-24",
            area = "Teoría",
            students = listOf(
                student("andrew", "Andrew", andrew, listOf(15, 16, 14, 17, 16, 15, 18, 14, 17, 16, 15), setOf(11)),
                student("joshua", "Joshua", joshua, listOf(10, 12, 11, 13, 9, 12, 10, 11, 12, 8, 11), setOf(3, 7, 10)),
            ),
        ),
        TeacherDemoCourse(
            id = "fisica-i",
            name = "Física I",
            careerCode = "C-24",
            area = "Teoría",
            students = listOf(
                student("andrew", "Andrew", andrew, listOf(16, 15, 17, 14, 16, 18, 15, 17, 16, 14, 17), setOf(11)),
                student("joshua", "Joshua", joshua, listOf(11, 10, 12, 9, 13, 11, 10, 12, 9, 11, 10), setOf(2, 5, 8, 11)),
            ),
        ),
        TeacherDemoCourse(
            id = "desarrollo-aplicaciones",
            name = "Desarrollo de Aplicaciones Empresariales Avanzado",
            careerCode = "C-24",
            area = "Programación",
            students = listOf(
                TeacherDemoStudentCourse(
                    studentId = "andrew", displayName = "Andrew", email = andrew,
                    weeklyGrades = grades(listOf(17, 16, 18, 17, 15, 19, 16, 18, 17), "Práctica"),
                    labSubnotes = listOf(
                        TeacherDemoLabSubnote("lab-1", "Laboratorio 1", 10, 1, 2, 17.0),
                        TeacherDemoLabSubnote("lab-1", "Laboratorio 1", 11, 2, 2, 15.0),
                        TeacherDemoLabSubnote("lab-2", "Laboratorio 2", 11, 1, 2, 18.0),
                    ),
                    absenceWeeks = setOf(11),
                ),
                TeacherDemoStudentCourse(
                    studentId = "joshua", displayName = "Joshua", email = joshua,
                    weeklyGrades = grades(listOf(12, 11, 10, 12, 9, 11, 10, 13, 11), "Práctica"),
                    labSubnotes = listOf(
                        TeacherDemoLabSubnote("lab-1", "Laboratorio 1", 10, 1, 2, 8.0),
                        TeacherDemoLabSubnote("lab-1", "Laboratorio 1", 11, 2, 2, 10.0),
                        TeacherDemoLabSubnote("lab-2", "Laboratorio 2", 11, 1, 2, 9.0),
                    ),
                    absenceWeeks = setOf(4, 9),
                    tardyWeeks = setOf(7),
                ),
            ),
        ),
        TeacherDemoCourse(
            id = "integracion-sistemas",
            name = "Integración de Sistemas Empresariales Avanzado",
            careerCode = "C-24",
            area = "Programación",
            students = listOf(
                student("andrew", "Andrew", andrew, listOf(16, 17, 15, 18, 16, 17, 19, 15, 18, 16, 17), setOf(11)),
                student("joshua", "Joshua", joshua, listOf(11, 13, 10, 12, 9, 11, 12, 10, 13, 11, 9), setOf(3, 10)),
            ),
        ),
        TeacherDemoCourse(
            id = "ondas-calor",
            name = "Ondas y Calor",
            careerCode = "C-21",
            area = "Teoría",
            students = listOf(
                student("c21-sofia", "Sofía Cáceres", null, listOf(16, 15, 17, 14, 16, 18, 15, 17, 16, 14, 17), setOf(11)),
                student("c21-mateo", "Mateo Flores", null, listOf(10, 12, 11, 13, 9, 12, 10, 11, 12, 8, 11), setOf(2, 5, 8, 11)),
            ),
        ),
        TeacherDemoCourse(
            id = "calculo-estadistica",
            name = "Cálculo y Estadística",
            careerCode = "C-21",
            area = "Teoría",
            students = listOf(
                student("c21-sofia", "Sofía Cáceres", null, listOf(15, 16, 14, 17, 16, 15, 18, 14, 17, 16, 15), setOf(11)),
                student("c21-mateo", "Mateo Flores", null, listOf(10, 11, 12, 9, 13, 11, 10, 12, 9, 11, 10), setOf(3, 7, 10)),
            ),
        ),
        TeacherDemoCourse(
            id = "tecnologia-equipo-pesado",
            name = "Tecnología del Equipo Pesado",
            careerCode = "C-21",
            area = "Técnico",
            students = listOf(
                student("c21-sofia", "Sofía Cáceres", null, listOf(17, 16, 18, 17, 15, 19, 16, 18, 17, 16, 18), setOf(11)),
                student("c21-mateo", "Mateo Flores", null, listOf(12, 11, 10, 12, 9, 11, 10, 13, 11, 10, 12), setOf(4, 9)),
            ),
        ),
        TeacherDemoCourse(
            id = "mecanica-taller",
            name = "Mecánica de Taller",
            careerCode = "C-21",
            area = "Técnico",
            students = listOf(
                student("c21-sofia", "Sofía Cáceres", null, listOf(16, 17, 15, 18, 16, 17, 19, 15, 18, 16, 17), setOf(11)),
                student("c21-mateo", "Mateo Flores", null, listOf(11, 13, 10, 12, 9, 11, 12, 10, 13, 11, 9), setOf(2, 6, 10)),
            ),
        ),
    )

    val uniqueStudentsWithRisk: Int
        get() = courses.flatMap { it.students }.filter { it.hasAnyRisk }.distinctBy { it.studentId }.size

    val uniqueStudentsWithGradeRisk: Int
        get() = courses.flatMap { it.students }.filter { it.gradeRisk }.distinctBy { it.studentId }.size

    val uniqueStudentsWithWarning: Int
        get() = courses.flatMap { it.students }
            .filter { it.attendanceSignal == TeacherDemoAttendanceSignal.Warning }.distinctBy { it.studentId }.size

    val uniqueStudentsWithDi: Int
        get() = courses.flatMap { it.students }
            .filter { it.attendanceSignal == TeacherDemoAttendanceSignal.Di }.distinctBy { it.studentId }.size

    val chatContext: String
        get() = buildString {
            appendLine("Período: semana 11 de 11. Nota aprobatoria: 13.")
            appendLine("Asistencia: advertencia desde 20 %; DI solo si supera 30 %. Las tardanzas se cuentan aparte.")
            appendLine("El promedio de cada curso se calcula con las notas finales registradas, todas con el mismo peso. Los laboratorios completos aportan una nota promedio de sus subnotas; los incompletos no entran en el promedio.")
            appendLine("Resumen general de alumnos únicos: $uniqueStudentsWithRisk con señales, $uniqueStudentsWithGradeRisk con riesgo por notas, $uniqueStudentsWithWarning con advertencia de asistencia y $uniqueStudentsWithDi con DI.")

            careers.forEach { career ->
                val careerCourses = courses.filter { it.careerCode == career.code }
                val careerStudents = careerCourses.flatMap { it.students }.distinctBy { it.studentId }
                appendLine("\nCarrera ${career.code}: ${career.name}.")
                appendLine("Resumen de alumnos únicos: ${careerStudents.count { it.hasAnyRisk }} con señales; ${careerStudents.count { it.gradeRisk }} con riesgo por notas; ${careerStudents.count { it.attendanceSignal == TeacherDemoAttendanceSignal.Warning }} con advertencia; ${careerStudents.count { it.attendanceSignal == TeacherDemoAttendanceSignal.Di }} con DI.")

                careerCourses.forEach { course ->
                    appendLine("\nCurso: ${course.name} (${course.area}).")
                    course.students.forEach { student ->
                        val average = student.average?.formatForChat() ?: "sin notas finales"
                        val gradeStatus = when {
                            student.average == null -> "sin promedio"
                            student.gradeRisk && student.courseComplete -> "desaprobado"
                            student.gradeRisk -> "riesgo actual por notas, parcial"
                            student.courseComplete -> "aprobado"
                            else -> "promedio aprobatorio actual, parcial"
                        }
                        val gradeDetails = student.weeklyGrades.sortedBy { it.week }.joinToString { grade ->
                            "semana ${grade.week} ${grade.label}: ${grade.score.formatForChat()}"
                        }.ifEmpty { "ninguna" }
                        val completedLabs = student.completedLabGrades.joinToString { (name, score) ->
                            "$name promedio ${score.formatForChat()}"
                        }.ifEmpty { "ninguno" }
                        val partialLabs = student.partialLabs.joinToString { parts ->
                            val lab = parts.first()
                            val subnotes = parts.sortedBy { it.part }.joinToString { "subnota ${it.part}: ${it.score.formatForChat()} (semana ${it.week})" }
                            "${lab.label}: $subnotes; ${parts.size} de ${lab.requiredParts} subnotas"
                        }.ifEmpty { "ninguno" }
                        val attendanceSignal = when (student.attendanceSignal) {
                            TeacherDemoAttendanceSignal.None -> "sin señal"
                            TeacherDemoAttendanceSignal.Warning -> "advertencia"
                            TeacherDemoAttendanceSignal.Di -> "DI"
                        }
                        appendLine("Alumno: ${student.displayName}.")
                        appendLine("Promedio: $average con ${student.finalGrades.size} notas finales; estado: $gradeStatus; riesgo por notas: ${if (student.gradeRisk) "sí" else "no"}.")
                        appendLine("Notas por semana: $gradeDetails.")
                        appendLine("Laboratorios completos: $completedLabs. Laboratorios parciales, fuera del promedio: $partialLabs.")
                        appendLine("Asistencia: ${student.absenceWeeks.size} faltas de ${student.performedClasses} clases realizadas (${(student.absenceRate * 100).formatForChat()} %); estado: $attendanceSignal; faltas en semanas ${student.absenceWeeks.sorted().joinToString().ifEmpty { "ninguna" }}; tardanzas separadas en semanas ${student.tardyWeeks.sorted().joinToString().ifEmpty { "ninguna" }}.")
                    }
                }
            }
        }
}

private fun Double.formatForChat(): String = String.format(Locale.US, "%.1f", this)
