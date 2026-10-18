package pe.tecsup.paquitobot.domain.calendar

import kotlin.test.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest

class CalendarSyncEngineTest {
    private fun event(id: String) = CalendarEvent(id, id, "Tarea", "", "2026-10-20", "2026-10-21", true, "America/Lima")
    private fun gateway(action: suspend (CalendarEvent) -> CalendarWrite) = object : CalendarGateway {
        override suspend fun upsert(event: CalendarEvent) = action(event)
    }
    @Test fun summarizesCreatesUpdatesAndUnchangedWrites() = runTest {
        val engine = CalendarSyncEngine(gateway { when (it.id) { "a" -> CalendarWrite.Created; "b" -> CalendarWrite.Updated; else -> CalendarWrite.Unchanged } })
        assertEquals(CalendarSyncReport(created = 1, updated = 1, unchanged = 1), engine.sync(listOf(event("a"), event("b"), event("c"))) { true })
    }
    @Test fun duplicateSelectionsWriteOnlyOnce() = runTest {
        var calls = 0
        val engine = CalendarSyncEngine(gateway { calls++; CalendarWrite.Created })
        val report = engine.sync(listOf(event("a"), event("a"))) { true }
        assertEquals(1, calls); assertEquals(1, report.created)
    }
    @Test fun revokedPermissionStopsBatchAndReportsPartialSuccess() = runTest {
        var calls = 0
        val engine = CalendarSyncEngine(gateway { if (++calls == 2) throw CalendarServiceException(CalendarError.Permission) else CalendarWrite.Created })
        val report = engine.sync(listOf(event("a"), event("b"), event("c"))) { true }
        assertEquals(1, report.created); assertEquals(1, report.failed); assertEquals(1, report.remaining)
        assertEquals(CalendarError.Permission, report.error); assertEquals(2, calls)
    }
    @Test fun networkFailureDoesNotContinueHammeringGoogle() = runTest {
        var calls = 0
        val report = CalendarSyncEngine(gateway { calls++; throw CalendarServiceException(CalendarError.Network) })
            .sync(listOf(event("a"), event("b"))) { true }
        assertEquals(1, calls); assertEquals(1, report.remaining)
    }
    @Test fun conflictingEventDoesNotBlockOtherSelectedTasks() = runTest {
        val report = CalendarSyncEngine(gateway { if (it.id == "a") throw CalendarServiceException(CalendarError.Conflict) else CalendarWrite.Created })
            .sync(listOf(event("a"), event("b"))) { true }
        assertEquals(1, report.created); assertEquals(1, report.failed); assertEquals(CalendarError.Conflict, report.error)
    }
    @Test fun changedAcademicSessionPreventsAnyWrites() = runTest {
        val report = CalendarSyncEngine(gateway { error("must not write") }).sync(listOf(event("a"))) { false }
        assertEquals(1, report.remaining); assertEquals(CalendarError.Expired, report.error)
    }
    @Test fun cancellationPropagates() = runTest {
        assertFailsWith<CancellationException> {
            CalendarSyncEngine(gateway { throw CancellationException("cancel") }).sync(listOf(event("a"))) { true }
        }
    }
}
