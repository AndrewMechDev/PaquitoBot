package pe.tecsup.paquitobot.data

import java.time.LocalDate
import java.time.ZoneId
import org.json.JSONArray
import org.json.JSONObject
import pe.tecsup.paquitobot.domain.academic.ACADEMIC_ABSENCE_LIMIT
import pe.tecsup.paquitobot.domain.academic.AcademicRepository

/** JSON académico de la cuenta activa, construido desde el mismo repositorio que las pantallas. */
object AcademicContextSnapshot {
    suspend fun from(repository: AcademicRepository): JSONObject {
        val today = LocalDate.now(ZoneId.of("America/Lima"))
        val courses = JSONArray()
        repository.courses().getOrThrow().forEach { course ->
            val assignments = repository.courseAssignments(course.id).getOrThrow()
            val grades = repository.courseGrades(course.id).getOrThrow().associateBy { it.assignmentId }
            val sessions = repository.courseClassSessions(course.id).getOrThrow().associateBy { it.id }
            val attendance = repository.courseAttendance(course.id).getOrThrow()

            val tasks = JSONArray()
            assignments.forEach { assignment ->
                val dueDate = assignment.dueAt?.take(10)?.let { LocalDate.parse(it) }
                val status = when {
                    assignment.id in grades -> "calificada"
                    dueDate == null -> "sin_fecha"
                    dueDate.isBefore(today) -> "vencida"
                    else -> "pendiente"
                }
                tasks.put(JSONObject()
                    .put("id", assignment.id)
                    .put("nombre", assignment.name)
                    .put("tipo", assignment.kind.name.lowercase())
                    .put("fecha_entrega", assignment.dueAt ?: JSONObject.NULL)
                    .put("estado", status)
                    .put("nota", grades[assignment.id]?.score ?: JSONObject.NULL)
                    .put("nota_maxima", assignment.pointsPossible))
            }

            val attendanceJson = JSONArray()
            attendance.forEach { record ->
                val session = sessions[record.classSessionId] ?: error("Asistencia sin sesión")
                attendanceJson.put(JSONObject()
                    .put("sesion_id", session.id)
                    .put("fecha", session.startAt ?: JSONObject.NULL)
                    .put("tipo", session.kind.name.lowercase())
                    .put("estado", if (record.isPresent) "presente" else "falta"))
            }
            courses.put(JSONObject()
                .put("codigo", course.code)
                .put("nombre", course.name)
                .put("faltas", attendance.count { !it.isPresent })
                .put("limite_faltas_demo", ACADEMIC_ABSENCE_LIMIT)
                .put("tareas_y_notas", tasks)
                .put("asistencia", attendanceJson))
        }
        return JSONObject()
            .put("fuente", "Escenario académico de demostración asignado a esta cuenta de Clerk")
            .put("fecha_actual", today.toString())
            .put("cursos", courses)
    }
}
