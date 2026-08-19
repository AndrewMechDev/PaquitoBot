package pe.tecsup.paquitobot.util

/**
 * Utilidades de fecha para datos de `canvas-mock` (ISO `yyyy-MM-dd...`), sin
 * libreria de fechas (Zeller's congruence, Gregoriano) - mismo criterio que
 * ya se uso en `AcademicScheduleViewModel`, extraido aca para reusar tambien
 * en Home. `dateKey` siempre es el prefijo `yyyy-MM-dd` de un ISO datetime.
 */
object AcademicDateUtils {
    private val WEEKDAY_NAMES = listOf("Domingo", "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado")
    private val ZELLER_TO_WEEKDAY_INDEX = listOf(6, 0, 1, 2, 3, 4, 5)
    private val MONTH_NAMES = listOf(
        "enero", "febrero", "marzo", "abril", "mayo", "junio",
        "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre",
    )

    /** 0=Domingo..6=Sabado. Devuelve null si [dateKey] no tiene formato `yyyy-MM-dd`. */
    private fun weekdayIndex(dateKey: String): Int? {
        val parts = dateKey.split("-")
        if (parts.size != 3) return null
        val year = parts[0].toIntOrNull() ?: return null
        val month = parts[1].toIntOrNull() ?: return null
        val day = parts[2].toIntOrNull() ?: return null

        var y = year
        var m = month
        if (m < 3) {
            m += 12
            y -= 1
        }
        val k = y % 100
        val j = y / 100
        val h = (day + (13 * (m + 1)) / 5 + k + k / 4 + j / 4 + 5 * j) % 7
        return ZELLER_TO_WEEKDAY_INDEX[h]
    }

    /** "2026-08-17" -> "Lunes". Devuelve [dateKey] tal cual si no se puede parsear. */
    fun weekdayName(dateKey: String): String =
        weekdayIndex(dateKey)?.let { WEEKDAY_NAMES[it] } ?: dateKey

    /** "2026-08-17" -> "Lunes 17". */
    fun weekdayWithDay(dateKey: String): String {
        val day = dateKey.split("-").getOrNull(2) ?: return dateKey
        return "${weekdayName(dateKey)} $day"
    }

    /** "2026-08-17" -> "17". */
    fun dayNumber(dateKey: String): String = dateKey.split("-").getOrNull(2) ?: dateKey

    /** "2026-08-17" -> "17/08/26". */
    fun shortDateLabel(dateKey: String): String {
        val parts = dateKey.split("-")
        if (parts.size != 3) return dateKey
        val year = parts[0].takeLast(2)
        return "${parts[2]}/${parts[1]}/$year"
    }

    /** "2026-08-17" -> "Lunes, 17 de agosto de 2026". */
    fun fullDateLabel(dateKey: String): String {
        val parts = dateKey.split("-")
        if (parts.size != 3) return dateKey
        val year = parts[0]
        val month = parts[1].toIntOrNull()?.let { MONTH_NAMES.getOrNull(it - 1) } ?: parts[1]
        val day = parts[2]
        return "${weekdayName(dateKey)}, $day de $month de $year"
    }
}
