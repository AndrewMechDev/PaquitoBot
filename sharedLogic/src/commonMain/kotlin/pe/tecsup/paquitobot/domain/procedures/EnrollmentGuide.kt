package pe.tecsup.paquitobot.domain.procedures

data class EnrollmentStep(val id: String, val title: String, val body: List<String>,
    val channels: List<ProcedureChannel>, val cost: String?, val responseTime: String?, val optional: Boolean, val sourcePage: Int)
data class EnrollmentTrack(val id: String, val title: String, val description: String,
    val checklist: List<String>, val stepIds: List<String>, val relatedProcedureIds: List<String>)
data class EnrollmentCalendar(val period: String?, val regular: String?, val extraordinary: String?,
    val classesStart: String?, val officialChannel: ProcedureChannel)
data class EnrollmentGuide(val tracks: List<EnrollmentTrack>, val steps: List<EnrollmentStep>, val calendar: EnrollmentCalendar) {
    fun stepsFor(track: EnrollmentTrack): List<EnrollmentStep> = track.stepIds.map { id -> steps.first { it.id == id } }
}
