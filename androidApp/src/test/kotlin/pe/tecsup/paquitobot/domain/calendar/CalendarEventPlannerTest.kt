package pe.tecsup.paquitobot.domain.calendar

import org.junit.Assert.*
import org.junit.Test
import java.time.Instant

class CalendarEventPlannerTest {
    private val now = Instant.parse("2026-10-18T12:00:00Z")
    private fun task(due: String) = CalendarTask(1, 2, 3, "Informe", "FIS-I", due)
    @Test fun localDeadlineUsesPeruAndFifteenMinuteBlock() {
        val item = CalendarEventPlanner.candidate(task("2026-10-20T23:59:00"), now)!!
        assertEquals("2026-10-21T04:59:00Z", item.start)
        assertEquals("2026-10-21T05:14:00Z", item.end)
        assertFalse(item.past)
    }
    @Test fun explicitOffsetIsPreservedAsAnInstant() {
        val item = CalendarEventPlanner.candidate(task("2026-10-20T15:00:00+02:00"), now)!!
        assertEquals("2026-10-20T13:00:00Z", item.start)
    }
    @Test fun allDayHasExclusiveEndAcrossMonthBoundary() {
        val item = CalendarEventPlanner.candidate(task("2026-10-31"), now)!!
        assertTrue(item.allDay)
        assertEquals("2026-11-01", item.end)
    }
    @Test fun pastDatesAreFlaggedWithoutMovingThemToToday() {
        val item = CalendarEventPlanner.candidate(task("2026-08-03T23:59:00"), now)!!
        assertTrue(item.past)
        assertEquals("2026-08-04T04:59:00Z", item.start)
    }
    @Test fun malformedAndImpossibleDatesAreOmitted() {
        listOf("", "sin fecha", "2026-02-30", "2026-02-30T11:00:00").forEach {
            assertNull(CalendarEventPlanner.candidate(task(it), now))
        }
    }
    @Test fun changedDeadlineAndTitleUpdateSameEventIdentity() {
        val before = CalendarEventPlanner.candidate(task("2026-10-20T10:00:00"), now)!!
        val after = CalendarEventPlanner.candidate(task("2026-10-22T11:00:00").copy(title = "Informe final"), now)!!
        val first = CalendarEventPlanner.event(before, "clerk1", "google1")
        val second = CalendarEventPlanner.event(after, "clerk1", "google1")
        assertEquals(first.id, second.id)
        assertNotEquals(first.start, second.start)
        assertNotEquals(first.title, second.title)
    }
    @Test fun accountsAndTasksHaveSeparateIdsWithoutExposingUserIds() {
        val candidate = CalendarEventPlanner.candidate(task("2026-10-20"), now)!!
        val first = CalendarEventPlanner.event(candidate, "clerk1", "google1")
        assertNotEquals(first.id, CalendarEventPlanner.event(candidate, "clerk2", "google1").id)
        assertNotEquals(first.id, CalendarEventPlanner.event(candidate, "clerk1", "google2").id)
        assertNotEquals(first.id, CalendarEventPlanner.event(candidate.copy(task = candidate.task.copy(assignmentId = 4)), "clerk1", "google1").id)
        assertTrue(first.id.matches(Regex("[0-9a-v]{5,1024}")))
        assertFalse(first.id.contains("clerk1"))
    }
}
