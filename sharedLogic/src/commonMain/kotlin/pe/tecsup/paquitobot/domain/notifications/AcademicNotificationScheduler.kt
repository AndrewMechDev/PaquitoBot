package pe.tecsup.paquitobot.domain.notifications

/** Reconcilia el trabajo de cada plataforma con las preferencias y la conexión académica. */
interface AcademicNotificationScheduler {
    fun reconcile()
}
