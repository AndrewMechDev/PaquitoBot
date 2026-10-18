package pe.tecsup.paquitobot.domain.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class AcademicNotificationPlannerTest {
    private val now = Instant.parse("2026-10-18T13:00:00Z")
    private val zone = ZoneId.of("America/Bogota")
    private val categories = setOf(NotificationCategory.Deadlines)
    private val noSummary = NotificationSchedule(dailySummaryEnabled = false)

    private fun assignment(due: String, courseId: Int = 1, id: Int = 2) =
        NotificationAssignment(courseId, id, "Actividad de prueba", Instant.parse(due))

    @Test fun noReminderBeforeAnticipationWindow() {
        assertTrue(AcademicNotificationPlanner.plan(listOf(assignment("2026-10-19T13:00:01Z")),
            categories, noSummary, now, zone).isEmpty())
    }

    @Test fun reminderStartsAtBoundaryAndCatchesUpAfterDelay() {
        val task = assignment("2026-10-19T13:00:00Z")
        assertEquals(1, AcademicNotificationPlanner.plan(listOf(task), categories, noSummary, now, zone).size)
        assertEquals(1, AcademicNotificationPlanner.plan(listOf(task), categories, noSummary, now.plusSeconds(900), zone).size)
    }

    @Test fun expiredAndExactlyDueAssignmentsAreExcluded() {
        val tasks = listOf(assignment("2026-10-18T13:00:00Z"), assignment("2026-10-17T13:00:00Z", id = 3))
        assertTrue(AcademicNotificationPlanner.plan(tasks, categories, NotificationSchedule(), now, zone).isEmpty())
    }

    @Test fun disabledAndUnsupportedCategoriesDoNotProduceDeadlineNotices() {
        val tasks = listOf(assignment("2026-10-18T18:00:00Z"))
        for (selection in listOf(emptySet(), setOf(NotificationCategory.Attendance, NotificationCategory.Laboratories))) {
            assertTrue(AcademicNotificationPlanner.plan(tasks, selection, NotificationSchedule(), now, zone).isEmpty())
        }
    }

    @Test fun summaryUsesLocalDayAndConfiguredTime() {
        val tasks = listOf(assignment("2026-10-18T23:00:00Z"))
        val before = AcademicNotificationPlanner.plan(tasks, categories, NotificationSchedule(), now.minusSeconds(1), zone)
        assertFalse(before.any { it.key.startsWith("summary:") })
        val atTime = AcademicNotificationPlanner.plan(tasks, categories, NotificationSchedule(), now, zone)
        assertEquals("summary:2026-10-18", atTime.single { it.key.startsWith("summary:") }.key)
    }

    @Test fun summaryDoesNotIncludeTomorrowEvenWithinReminderWindow() {
        val notices = AcademicNotificationPlanner.plan(listOf(assignment("2026-10-19T05:01:00Z")),
            categories, NotificationSchedule(), now, zone)
        assertEquals(1, notices.size)
        assertTrue(notices.single().key.startsWith("reminder:"))
    }

    @Test fun summaryCanBeDisabledWithoutDisablingReminders() {
        val notices = AcademicNotificationPlanner.plan(listOf(assignment("2026-10-18T18:00:00Z")), categories, noSummary, now, zone)
        assertEquals(1, notices.size)
        assertTrue(notices.single().key.startsWith("reminder:"))
    }

    @Test fun stableKeysAvoidResendingWhenAnticipationChangesButDistinguishChangedDueDates() {
        val task = assignment("2026-10-18T18:00:00Z")
        val first = AcademicNotificationPlanner.plan(listOf(task), categories, noSummary, now, zone).single()
        val changedHours = AcademicNotificationPlanner.plan(listOf(task), categories, noSummary.copy(reminderHours = 6), now, zone).single()
        val changedDate = AcademicNotificationPlanner.plan(listOf(task.copy(due = task.due.plusSeconds(60))), categories, noSummary, now, zone).single()
        assertEquals(first.key, changedHours.key)
        assertNotEquals(first.key, changedDate.key)
        assertEquals(first.displayKey, changedDate.displayKey)
    }

    @Test fun duplicatesAreCollapsedButCourseIdsRemainDistinct() {
        val task = assignment("2026-10-18T18:00:00Z")
        val notices = AcademicNotificationPlanner.plan(listOf(task, task, task.copy(courseId = 3)), categories, noSummary, now, zone)
        assertEquals(2, notices.size)
        assertEquals(2, notices.map { it.key }.toSet().size)
    }

    @Test fun isoOffsetsAreRespectedAndAmbiguousOrInvalidDatesAreIgnored() {
        assertEquals(now, AcademicNotificationPlanner.parseDueAt("2026-10-18T08:00:00-05:00"))
        assertEquals(now.plusMillis(123), AcademicNotificationPlanner.parseDueAt("2026-10-18T13:00:00.123Z"))
        assertNull(AcademicNotificationPlanner.parseDueAt("2026-10-18T08:00:00"))
        assertNull(AcademicNotificationPlanner.parseDueAt("2026-10-18"))
        assertNull(AcademicNotificationPlanner.parseDueAt("fecha inválida"))
        assertNull(AcademicNotificationPlanner.parseDueAt("+999999999-10-18T13:00:00Z"))
        assertNull(AcademicNotificationPlanner.parseDueAt(null))
    }

    @Test fun daylightSavingGapMovesSummaryToNextValidLocalTime() {
        val berlin = ZoneId.of("Europe/Berlin")
        val tasks = listOf(assignment("2026-03-29T06:00:00Z"))
        val schedule = NotificationSchedule(summaryHour = 2, summaryMinute = 30)
        val before = AcademicNotificationPlanner.plan(tasks, categories, schedule, Instant.parse("2026-03-29T01:29:00Z"), berlin)
        val after = AcademicNotificationPlanner.plan(tasks, categories, schedule, Instant.parse("2026-03-29T01:30:00Z"), berlin)
        assertFalse(before.any { it.key.startsWith("summary:") })
        assertTrue(after.any { it.key.startsWith("summary:") })
    }
}
