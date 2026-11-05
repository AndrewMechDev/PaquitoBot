package pe.tecsup.paquitobot.domain.academic

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class AcademicRiskEngineTest {
    private val course = AcademicCourse(1, "CALC-II", "Cálculo II", 30)

    @Test
    fun gradeBelowTwelveIsCriticalAndExplainsTheRecoveryPlan() {
        val risk = AcademicRiskEngine.assess(
            course = course,
            grades = listOf(AcademicGrade(1, 10.0), AcademicGrade(2, 12.0)),
            attendance = emptyList(),
            sessions = emptyList(),
        )

        assertEquals(AcademicRiskLevel.Critical, risk.grade?.level)
        assertEquals(AcademicRiskKind.Grade, risk.grade?.kind)
        assertEquals("Contactar a mi tutora", risk.grade?.actionLabel)
        assertEquals(11.0, risk.grade?.currentValue)
    }

    @Test
    fun gradeTwelveShowsRecoveryInsteadOfFailure() {
        val risk = AcademicRiskEngine.assess(
            course = course,
            grades = listOf(AcademicGrade(1, 12.0)),
            attendance = emptyList(),
            sessions = emptyList(),
        )

        assertEquals(AcademicRiskLevel.Urgent, risk.grade?.level)
        assertEquals("Mejora tu promedio en CALC-II", risk.grade?.title)
    }

    @Test
    fun theoryOnlyCourseWarnsAtThreeAndExceedsAtFiveAbsences() {
        val sessions = (1..5).map { AcademicClassSession(it, 1, null, null, AcademicSessionKind.Class) }
        val atWatch = AcademicRiskEngine.assess(
            course, emptyList(), sessions.take(3).map { AcademicAttendance(it.id, false) }, sessions,
        )
        val exceeded = AcademicRiskEngine.assess(
            course, emptyList(), sessions.map { AcademicAttendance(it.id, false) }, sessions,
        )

        assertEquals(4.0, atWatch.attendance?.limitValue)
        assertEquals(AcademicRiskLevel.Watch, atWatch.attendance?.level)
        assertEquals(AcademicRiskLevel.Critical, exceeded.attendance?.level)
    }

    @Test
    fun courseWithLaboratoryUsesEightAbsenceLimit() {
        val sessions = (1..8).map { id ->
            AcademicClassSession(id, 1, null, null, if (id % 2 == 0) AcademicSessionKind.Laboratory else AcademicSessionKind.Class)
        }
        val risk = AcademicRiskEngine.assess(course, emptyList(), sessions.map { AcademicAttendance(it.id, false) }, sessions)

        assertEquals(8, AcademicRiskEngine.attendanceLimit(sessions))
        assertEquals(AcademicRiskLevel.Urgent, risk.attendance?.level)
        assertNull(risk.grade)
    }
}
