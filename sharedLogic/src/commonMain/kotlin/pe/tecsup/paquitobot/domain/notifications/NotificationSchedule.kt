package pe.tecsup.paquitobot.domain.notifications

/** Horario local del dispositivo; no implica una garantía de ejecución exacta. */
data class NotificationSchedule(
    val reminderHours: Int = 24,
    val dailySummaryEnabled: Boolean = true,
    val summaryHour: Int = 8,
    val summaryMinute: Int = 0,
) {
    init {
        require(reminderHours in reminderOptions)
        require(summaryHour in 0..23)
        require(summaryMinute in 0..59)
    }

    companion object {
        val reminderOptions: List<Int> = listOf(1, 6, 12, 24, 48)
    }
}
