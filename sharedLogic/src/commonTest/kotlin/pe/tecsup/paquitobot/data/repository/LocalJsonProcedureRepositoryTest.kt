package pe.tecsup.paquitobot.data.repository

import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import pe.tecsup.paquitobot.domain.procedures.ProcedureCategory

class LocalJsonProcedureRepositoryTest {
    private fun asset(): String {
        val relative = "androidApp/src/main/assets/procedures.json"
        val path = listOf(relative, "../$relative", "../../$relative").map { Path(it) }
            .first { SystemFileSystem.exists(it) }
        return SystemFileSystem.source(path).buffered().use { it.readString() }
    }
    @Test fun bundledCatalogHasTraceableContent() = runTest {
        val catalog = LocalJsonProcedureRepository { asset() }.catalog().getOrThrow()
        assertEquals(35, catalog.procedures.size)
        assertEquals(ProcedureCategory.entries.toSet(), catalog.procedures.map { it.category }.toSet())
        assertEquals("2026-10-18", catalog.source.reviewedOn)
        assertTrue(catalog.procedures.all { it.sourcePage in 2..9 && it.channels.isNotEmpty() })
        assertNull(catalog.find("missing"))
    }
    @Test fun searchCombinesAccentsWordsCodeAndCategory() = runTest {
        val catalog = LocalJsonProcedureRepository { asset() }.catalog().getOrThrow()
        assertEquals(listOf("reserva-matricula"), catalog.search("RESERVA MATRICULA").map { it.id })
        assertEquals(listOf("silabo-curso"), catalog.search("silabo CURSO", ProcedureCategory.Documents).map { it.id })
        assertEquals(6, catalog.search("PE-012").size)
        assertTrue(catalog.search("silabo", ProcedureCategory.Finance).isEmpty())
        assertEquals(35, catalog.search("   ").size)
        assertTrue(catalog.search("no existe este trámite").isEmpty())
    }
    @Test fun responseTimeAndApplicationWindowRemainDistinct() = runTest {
        val reserve = LocalJsonProcedureRepository { asset() }.catalog().getOrThrow().find("reserva-matricula")!!
        assertTrue(reserve.applicationWindow!!.contains("admisión"))
        assertTrue(reserve.responseTime.contains("solicitud"))
    }
    @Test fun successfulLoadIsCached() = runTest {
        var reads = 0
        val repository = LocalJsonProcedureRepository { reads++; asset() }
        repository.catalog().getOrThrow(); repository.catalog().getOrThrow()
        assertEquals(1, reads)
    }
    @Test fun readFailureCanBeRetriedWithoutFalseEmptyCatalog() = runTest {
        var reads = 0
        val repository = LocalJsonProcedureRepository { if (++reads == 1) error("no asset") else asset() }
        assertTrue(repository.catalog().isFailure)
        assertEquals(35, repository.catalog().getOrThrow().procedures.size)
    }
    @Test fun invalidJsonIsReported() = runTest {
        assertTrue(LocalJsonProcedureRepository { "{}" }.catalog().isFailure)
    }
    @Test fun duplicateIdsAreRejected() = runTest {
        val json = asset().replace("constancia-estudios", "constancia-ingreso")
        assertTrue(LocalJsonProcedureRepository { json }.catalog().isFailure)
    }
    @Test fun unexpectedChannelHostIsRejected() = runTest {
        val json = asset().replace("https://campus-cloud.tecsup.edu.pe/auth/login", "https://tecsup.edu.pe.example.com/auth/login")
        assertTrue(LocalJsonProcedureRepository { json }.catalog().isFailure)
    }
    @Test fun coroutineCancellationIsNotConvertedToContentError() = runTest {
        assertFailsWith<CancellationException> {
            LocalJsonProcedureRepository { throw CancellationException("cancelled") }.catalog()
        }
    }
}
