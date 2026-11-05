package pe.tecsup.paquitobot.domain.notifications

/** Identificadores persistentes, independientes del texto y los recursos de cada plataforma. */
enum class NotificationCategory(val storageKey: String) {
    Laboratories("laboratories"),
    Deadlines("deadlines"),
    Attendance("attendance"),
    AcademicRisk("academic_risk");

    companion object {
        val defaults: Set<NotificationCategory> = setOf(Laboratories, Deadlines, AcademicRisk)
    }
}
