package pe.tecsup.paquitobot.data.academic

/** Contactos ficticios para la demostración; el backend reemplazará este directorio. */
data class DemoTutorContact(val name: String, val email: String)

object DemoTutorDirectory {
    fun forCourse(courseId: Int): DemoTutorContact = when (courseId) {
        1 -> DemoTutorContact("Ana Torres", "ana.torres@example.invalid")
        2 -> DemoTutorContact("Luis Rojas", "luis.rojas@example.invalid")
        3 -> DemoTutorContact("María Huamán", "maria.huaman@example.invalid")
        4 -> DemoTutorContact("Daniela Flores", "daniela.flores@example.invalid")
        else -> DemoTutorContact("Tutora académica", "tutoria@example.invalid")
    }
}
