package pe.tecsup.paquitobot.domain.procedures

enum class ProcedureCategory(val label: String) {
    Enrollment("Matrícula y continuidad"), Documents("Constancias y certificados"),
    Graduation("Egreso y titulación"), Finance("Becas y finanzas"),
    Transfers("Traslados"), Practices("Prácticas"), Admission("Admisión"),
}
enum class ProcedureChannelKind { Web, Email }
data class ProcedureChannel(val label: String, val target: String, val kind: ProcedureChannelKind)
data class AcademicProcedure(
    val id: String, val code: String, val title: String, val category: ProcedureCategory,
    val audience: String, val summary: String, val requirements: List<String>,
    val steps: List<String>, val cost: String, val responseTime: String,
    val applicationWindow: String?, val duration: String?, val responsibleArea: String,
    val channels: List<ProcedureChannel>, val notes: List<String>, val sourcePage: Int,
)
data class ProcedureSource(val title: String, val url: String, val edition: String, val reviewedOn: String)
data class ProcedureCatalog(val source: ProcedureSource, val procedures: List<AcademicProcedure>) {
    fun find(id: String?): AcademicProcedure? = procedures.firstOrNull { it.id == id }
    /** Búsqueda por palabras, sin distinguir tildes o mayúsculas, combinada con categoría. */
    fun search(query: String, category: ProcedureCategory? = null): List<AcademicProcedure> {
        val terms = query.searchKey().split(Regex("\\s+")).filter { it.isNotBlank() }
        return procedures.filter { item ->
            (category == null || item.category == category) && terms.all { term ->
                listOf(item.title, item.code, item.summary, item.audience, item.category.label)
                    .joinToString(" ").searchKey().contains(term)
            }
        }
    }
}
private fun String.searchKey(): String = lowercase().map {
    when (it) { 'á', 'à', 'ä' -> 'a'; 'é', 'è', 'ë' -> 'e'; 'í', 'ì', 'ï' -> 'i';
        'ó', 'ò', 'ö' -> 'o'; 'ú', 'ù', 'ü' -> 'u'; else -> it }
}.joinToString("")
interface ProcedureRepository { suspend fun catalog(): Result<ProcedureCatalog> }
