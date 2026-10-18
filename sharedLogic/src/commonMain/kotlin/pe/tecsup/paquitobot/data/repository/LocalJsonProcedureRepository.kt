package pe.tecsup.paquitobot.data.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import pe.tecsup.paquitobot.domain.procedures.*

/** Catálogo editorial incluido en la app. La plataforma inyecta la lectura del asset. */
class LocalJsonProcedureRepository(private val readJson: suspend () -> String) : ProcedureRepository {
    private val mutex = Mutex()
    private var cached: ProcedureCatalog? = null
    override suspend fun catalog(): Result<ProcedureCatalog> = try {
        Result.success(mutex.withLock {
            cached ?: Json.decodeFromString<CatalogDocument>(readJson()).toDomain().also { cached = it }
        })
    } catch (cancelled: CancellationException) { throw cancelled
    } catch (error: Exception) { Result.failure(error) }
}
@Serializable
private data class CatalogDocument(val source: SourceDocument, val procedures: List<ProcedureDocument>, val enrollment: EnrollmentDocument? = null) {
    fun toDomain(): ProcedureCatalog {
        require(procedures.isNotEmpty() && procedures.map { it.id }.distinct().size == procedures.size)
        require(source.url.startsWith("https://www.tecsup.edu.pe/"))
        return ProcedureCatalog(ProcedureSource(source.title, source.url, source.edition, source.reviewedOn), procedures.map { it.toDomain() }, enrollment?.toDomain(procedures.map { it.id }.toSet()))
    }
}
@Serializable
private data class SourceDocument(val title: String, val url: String, val edition: String, val reviewedOn: String)
@Serializable
private data class ChannelDocument(val label: String, val target: String, val kind: String) {
    fun toDomain(): ProcedureChannel {
        val type = ProcedureChannelKind.valueOf(kind)
        val valid = when (type) {
            ProcedureChannelKind.Email -> Regex("[A-Za-z0-9._%+-]+@tecsup\\.edu\\.pe").matches(target)
            ProcedureChannelKind.Web -> Regex("https://(?:[a-z0-9-]+\\.)*tecsup\\.edu\\.pe/[^\\s]*|https://forms\\.gle/[A-Za-z0-9]+").matches(target)
        }
        require(valid && label.isNotBlank()) { "Canal de trámite inválido" }
        return ProcedureChannel(label, target, type)
    }
}
@Serializable
private data class ProcedureDocument(
    val id: String, val code: String, val title: String, val category: String,
    val audience: String, val summary: String, val requirements: List<String>, val steps: List<String>,
    val cost: String, val responseTime: String, val responsibleArea: String,
    val channels: List<ChannelDocument>, val sourcePage: Int,
    val applicationWindow: String? = null, val duration: String? = null, val notes: List<String> = emptyList(),
) {
    fun toDomain(): AcademicProcedure {
        require(id.matches(Regex("[a-z0-9-]+")) && code.isNotBlank() && title.isNotBlank())
        require(requirements.isNotEmpty() && steps.isNotEmpty() && channels.isNotEmpty() && sourcePage in 1..29)
        require((requirements + steps + listOf(cost, responseTime, responsibleArea, summary, audience)).all { it.isNotBlank() })
        return AcademicProcedure(id, code, title, ProcedureCategory.valueOf(category), audience, summary,
            requirements, steps, cost, responseTime, applicationWindow, duration, responsibleArea,
            channels.map { it.toDomain() }, notes, sourcePage)
    }
}

@Serializable
private data class EnrollmentDocument(val tracks: List<TrackDocument>, val steps: List<StepDocument>, val calendar: CalendarDocument) {
    fun toDomain(procedureIds: Set<String>): EnrollmentGuide {
        require(tracks.map { it.id }.toSet() == setOf("incoming", "returning") && tracks.size == 2)
        require(steps.isNotEmpty() && steps.map { it.id }.distinct().size == steps.size)
        val stepIds = steps.map { it.id }.toSet()
        tracks.forEach {
            require(it.checklist.isNotEmpty() && it.checklist.all(String::isNotBlank))
            require(it.stepIds.isNotEmpty() && it.stepIds.distinct().size == it.stepIds.size && it.stepIds.all(stepIds::contains))
            require(it.relatedProcedureIds.all(procedureIds::contains))
            val payment = it.stepIds.indexOf("payment")
            val validation = it.stepIds.indexOf("validation")
            val confirmation = it.stepIds.indexOf("confirmation")
            require(payment >= 0 && validation > payment && confirmation > validation)
            require(it.title.isNotBlank() && it.description.isNotBlank())
        }
        require(listOf(calendar.regular, calendar.extraordinary, calendar.classesStart).all { it == null || it.isNotBlank() })
        require(listOf(calendar.regular, calendar.extraordinary, calendar.classesStart).all { it == null } || !calendar.period.isNullOrBlank())
        return EnrollmentGuide(tracks.map { EnrollmentTrack(it.id, it.title, it.description, it.checklist, it.stepIds, it.relatedProcedureIds) },
            steps.map { it.toDomain() }, EnrollmentCalendar(calendar.period, calendar.regular, calendar.extraordinary, calendar.classesStart, calendar.officialChannel.toDomain()))
    }
}
@Serializable
private data class TrackDocument(val id: String, val title: String, val description: String, val checklist: List<String>, val stepIds: List<String>, val relatedProcedureIds: List<String>)
@Serializable
private data class CalendarDocument(val period: String? = null, val regular: String? = null, val extraordinary: String? = null,
    val classesStart: String? = null, val officialChannel: ChannelDocument)
@Serializable
private data class StepDocument(val id: String, val title: String, val body: List<String>, val channels: List<ChannelDocument>,
    val cost: String? = null, val responseTime: String? = null, val optional: Boolean = false, val sourcePage: Int) {
    fun toDomain(): EnrollmentStep {
        require(id.isNotBlank() && title.isNotBlank() && body.isNotEmpty() && body.all(String::isNotBlank) && sourcePage in 1..29)
        return EnrollmentStep(id, title, body, channels.map { it.toDomain() }, cost, responseTime, optional, sourcePage)
    }
}
