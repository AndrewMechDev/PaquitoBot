package pe.tecsup.paquitobot.ui.academic

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import pe.tecsup.paquitobot.domain.academic.AcademicAssignment
import pe.tecsup.paquitobot.domain.academic.AcademicAttendance
import pe.tecsup.paquitobot.domain.academic.AcademicGrade

/**
 * Logica pura (sin Android ni corrutinas) compartida por los ViewModels
 * academicos: que cuenta como "pendiente", promedio, semana calendario.
 * Vive aparte para poder probarla con tests unitarios sin levantar nada.
 */

// El JSON academico no modela un "limite de faltas" por curso (es una regla
// de la institucion, no del dato) - 8 es un valor de referencia razonable,
// no un dato real. Mismo valor en Cursos, Detalle y cualquier otra pantalla.
internal const val ASSUMED_ABSENCE_LIMIT = 8

internal const val NO_DUE_LABEL = "Sin pendientes"

/** Mensaje generico: el detalle tecnico del fallo no le sirve al estudiante. */
internal const val ACADEMIC_LOAD_ERROR = "No se pudieron cargar los datos académicos. Inténtalo de nuevo."

/** Fecha del dispositivo en ISO `yyyy-MM-dd` (sin java.time: minSdk 24 no lo trae sin desugaring). */
fun deviceToday(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

/** `yyyy-MM-dd` de un ISO datetime (`2026-08-03T23:59:00`). */
internal fun dateKeyOf(isoDateTime: String): String = isoDateTime.take(10)

/** `HH:mm` de un ISO datetime, o `--:--` si no trae hora. */
internal fun timeOf(isoDateTime: String): String =
    isoDateTime.drop(11).take(5).ifBlank { "--:--" }

internal fun courseCountLabel(count: Int): String = if (count == 1) "1 curso" else "$count cursos"

/** Una entrega todavia pendiente junto con su fecha de entrega (`yyyy-MM-dd`). */
internal data class PendingAssignment(
    val assignment: AcademicAssignment,
    val dueDate: String,
)

/**
 * Entregas pendientes = sin nota Y con fecha de entrega >= [today]
 * (`yyyy-MM-dd`, la comparacion ISO equivale a la de fechas).
 * Las calificadas, vencidas y sin fecha no cuentan. Orden estable por fecha.
 */
internal fun pendingAssignments(
    assignments: List<AcademicAssignment>,
    grades: List<AcademicGrade>,
    today: String,
): List<PendingAssignment> {
    val gradedIds = grades.map { it.assignmentId }.toSet()
    return assignments
        .mapNotNull { assignment ->
            val dueDate = assignment.dueAt?.let(::dateKeyOf) ?: return@mapNotNull null
            if (assignment.id in gradedIds || dueDate < today) null else PendingAssignment(assignment, dueDate)
        }
        .sortedBy { it.dueDate }
}

/** Promedio de notas con un decimal, o "—" si no hay ninguna (no se inventa un numero). */
internal fun averageScoreLabel(grades: List<AcademicGrade>): String =
    if (grades.isEmpty()) "—" else "%.1f".format(grades.map { it.score }.average())

internal fun absenceCount(attendance: List<AcademicAttendance>): Int = attendance.count { !it.isPresent }

/**
 * Los 7 dias (`yyyy-MM-dd`, lunes a domingo) de la semana que contiene
 * [dateKey]. Lista vacia si [dateKey] no es una fecha valida.
 */
internal fun weekOf(dateKey: String): List<String> {
    val epochDay = epochDayOf(dateKey) ?: return emptyList()
    // 1970-01-01 (epochDay 0) fue jueves => lunes=0 ... domingo=6.
    val mondayOffset = ((epochDay + 3) % 7 + 7) % 7
    val monday = epochDay - mondayOffset
    return (0..6).map { isoOf(monday + it) }
}

// Aritmetica de fechas del calendario gregoriano (algoritmos "civil from days"
// de Howard Hinnant), en la misma linea que `AcademicDateUtils`: sin libreria
// de fechas. Dias contados desde 1970-01-01.

private fun epochDayOf(dateKey: String): Long? {
    val parts = dateKey.split("-")
    if (parts.size != 3) return null
    val year = parts[0].toIntOrNull() ?: return null
    val month = parts[1].toIntOrNull() ?: return null
    val day = parts[2].toIntOrNull() ?: return null
    if (month !in 1..12 || day !in 1..31) return null

    val y = (if (month <= 2) year - 1 else year).toLong()
    val era = (if (y >= 0) y else y - 399) / 400
    val yearOfEra = y - era * 400
    val dayOfYear = (153 * ((month + 9) % 12) + 2) / 5 + day - 1
    val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
    return era * 146097 + dayOfEra - 719468
}

private fun isoOf(epochDay: Long): String {
    val z = epochDay + 719468
    val era = (if (z >= 0) z else z - 146096) / 146097
    val dayOfEra = z - era * 146097
    val yearOfEra = (dayOfEra - dayOfEra / 1460 + dayOfEra / 36524 - dayOfEra / 146096) / 365
    val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
    val mp = (5 * dayOfYear + 2) / 153
    val day = dayOfYear - (153 * mp + 2) / 5 + 1
    val month = if (mp < 10) mp + 3 else mp - 9
    val year = yearOfEra + era * 400 + (if (month <= 2) 1 else 0)
    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}
