package pe.tecsup.paquitobot.ui.academic

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import pe.tecsup.paquitobot.domain.academic.AcademicAssignment
import pe.tecsup.paquitobot.domain.academic.AcademicAttendance
import pe.tecsup.paquitobot.domain.academic.AcademicClassSession
import pe.tecsup.paquitobot.domain.academic.AcademicCourse
import pe.tecsup.paquitobot.domain.academic.AcademicGrade
import pe.tecsup.paquitobot.domain.academic.AcademicProfile
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.ui.home.HomeScreenData
import pe.tecsup.paquitobot.ui.home.TaskUrgency

private const val TODAY = "2026-08-03"

private fun assignment(id: Int, courseId: Int, dueAt: String?) =
    AcademicAssignment(id, courseId, "Tarea $id", 20.0, dueAt)

/** Curso 1: 5 entregas (solo 102 y 104 estan pendientes). Curso 2: 1 entrega pendiente (106). */
private val assignments = listOf(
    assignment(101, 1, "2026-08-02T23:59:00"), // vencida, sin nota -> NO pendiente
    assignment(102, 1, "2026-08-03T23:59:00"), // vence hoy, sin nota -> pendiente
    assignment(103, 1, "2026-08-05T23:59:00"), // con nota -> NO pendiente
    assignment(104, 1, "2026-08-06T23:59:00"), // futura, sin nota -> pendiente
    assignment(105, 1, null), // sin fecha -> NO pendiente
    assignment(106, 2, "2026-08-09T23:59:00"), // pendiente (ultimo dia de la semana)
)
private val grades = listOf(AcademicGrade(103, 18.0))
private val sessions = listOf(
    AcademicClassSession(1, 1, "2026-08-03T08:00:00", "2026-08-03T10:00:00"),
    AcademicClassSession(2, 1, "2026-08-04T08:00:00", "2026-08-04T10:00:00"),
)
private val attendance = listOf(
    AcademicAttendance(1, isPresent = true),
    AcademicAttendance(2, isPresent = false),
)

private class FakeAcademicRepository : AcademicRepository {
    var failure: Throwable? = null

    private fun <T> result(value: T): Result<T> =
        failure?.let { Result.failure(it) } ?: Result.success(value)

    override suspend fun profile() = result(AcademicProfile(1, "Demo", "demo@example.com"))
    override suspend fun courses() = result(
        listOf(AcademicCourse(1, "CALC-II", "Calculo II", 30), AcademicCourse(2, "FIS-I", "Fisica I", 28)),
    )
    override suspend fun grades() = result(grades)
    override suspend fun attendance(days: Int) = result(attendance)
    override suspend fun courseAssignments(courseId: Int) = result(assignments.filter { it.courseId == courseId })
    override suspend fun courseGrades(courseId: Int) = result(
        grades.filter { g -> assignments.any { it.id == g.assignmentId && it.courseId == courseId } },
    )
    override suspend fun courseAttendance(courseId: Int) = result(
        attendance.filter { a -> sessions.any { it.id == a.classSessionId && it.courseId == courseId } },
    )
    override suspend fun courseClassSessions(courseId: Int) = result(sessions.filter { it.courseId == courseId })
}

private fun recordsFor(courseId: Int) = CourseRecords(
    sessions = sessions.filter { it.courseId == courseId },
    attendance = attendance.filter { a -> sessions.any { it.id == a.classSessionId && it.courseId == courseId } },
    assignments = assignments.filter { it.courseId == courseId },
    grades = grades.filter { g -> assignments.any { it.id == g.assignmentId && it.courseId == courseId } },
)

private val allCourses = listOf(
    CourseWithRecords(AcademicCourse(1, "CALC-II", "Calculo II", 30), recordsFor(1)),
    CourseWithRecords(AcademicCourse(2, "FIS-I", "Fisica I", 28), recordsFor(2)),
)

class AcademicLogicTest {

    @Test
    fun pendingAssignments_excludeGradedOverdueAndUndated() {
        val pending = pendingAssignments(assignments.filter { it.courseId == 1 }, grades, TODAY)
        assertEquals(listOf(102, 104), pending.map { it.assignment.id })
        assertEquals(listOf("2026-08-03", "2026-08-06"), pending.map { it.dueDate })
    }

    @Test
    fun pendingAssignments_dueTodayStillCounts() {
        val pending = pendingAssignments(listOf(assignment(1, 1, "2026-08-03T00:00:00")), emptyList(), TODAY)
        assertEquals(1, pending.size)
    }

    @Test
    fun weekOf_returnsMondayToSunday() {
        assertEquals(
            listOf("2026-08-03", "2026-08-04", "2026-08-05", "2026-08-06", "2026-08-07", "2026-08-08", "2026-08-09"),
            weekOf("2026-08-03"),
        )
        assertEquals(weekOf("2026-08-03"), weekOf("2026-08-09")) // domingo cae en la misma semana
        assertEquals(weekOf("2026-08-03"), weekOf("2026-08-06"))
    }

    @Test
    fun weekOf_crossesYearAndLeapDay() {
        val yearEnd = weekOf("2026-12-31")
        assertEquals("2026-12-28", yearEnd.first())
        assertEquals("2027-01-03", yearEnd.last())
        val leap = weekOf("2028-02-29")
        assertEquals("2028-02-28", leap.first())
        assertEquals("2028-03-05", leap.last())
    }

    @Test
    fun weekOf_malformedDateIsEmpty() {
        assertTrue(weekOf("no-es-fecha").isEmpty())
        assertTrue(weekOf("2026-13-40").isEmpty())
    }

    @Test
    fun averageScoreLabel_dashWhenNoGrades() {
        assertEquals("—", averageScoreLabel(emptyList()))
        assertEquals("15.0", averageScoreLabel(listOf(AcademicGrade(1, 14.0), AcademicGrade(2, 16.0))).replace(',', '.'))
    }

    @Test
    fun coursesData_countsOnlyPendingAssignments() {
        val data = buildCoursesData(allCourses, TODAY)
        assertEquals(3, data.pendingCount) // 102 + 104 + 106
        val calc = data.courses.first { it.code == "CALC-II" }
        assertEquals("Tarea 102 · 03/08/26", calc.nextDueLabel)
        assertEquals(1, calc.absences)
        val noPending = buildCoursesData(allCourses, "2026-09-01")
        assertEquals(0, noPending.pendingCount)
        assertEquals(NO_DUE_LABEL, noPending.courses.first().nextDueLabel)
    }

    @Test
    fun homeData_isTodayComesFromInjectedToday() {
        val base = HomeScreenData.default()
        val monday = buildHomeData(base, allCourses, "2026-08-03")
        assertEquals(7, monday.days.size)
        assertEquals(listOf(true, false, false, false, false, false, false), monday.days.map { it.isToday })
        assertEquals("Lunes", monday.days.first().dayOfWeek)

        // "hoy" miercoles: sigue siendo la misma semana, pero se marca el miercoles
        // (no "el primer dia con eventos").
        val wednesday = buildHomeData(base, allCourses, "2026-08-05")
        assertEquals(listOf("03", "04", "05", "06", "07", "08", "09"), wednesday.days.map { it.dayNumber })
        assertEquals("05", wednesday.days.single { it.isToday }.dayNumber)
    }

    @Test
    fun homeData_tasksArePendingOnesWithFullDates() {
        val data = buildHomeData(HomeScreenData.default(), allCourses, TODAY)
        assertEquals(listOf("Tarea 102", "Tarea 104", "Tarea 106"), data.tasks.map { it.title })
        assertEquals(listOf("2026-08-03", "2026-08-06", "2026-08-09"), data.tasks.map { it.dateKey })
        assertEquals(TaskUrgency.Urgent, data.tasks.first().urgency)
        assertEquals(TaskUrgency.Normal, data.tasks.last().urgency)
        assertEquals(3, data.pendingCount)
        assertTrue(data.dateLabel.contains("2026"))
    }

    @Test
    fun homeData_marksAbsenceDaysAsCritical() {
        val data = buildHomeData(HomeScreenData.default(), allCourses, TODAY)
        assertEquals(listOf("04"), data.days.filter { it.isCritical }.map { it.dayNumber })
    }

    @Test
    fun scheduleData_marksTodayAndCountsPending() {
        val data = buildScheduleData(allCourses, TODAY)
        assertEquals(3, data.pendingCount)
        assertEquals(listOf(true), data.days.filter { it.label.endsWith(" 03") }.map { it.isToday })
        assertEquals(1, data.days.count { it.isToday })
        // la falta marcada aparece como evento de tipo Falta
        assertTrue(data.days.flatMap { it.events }.any { it.title == "Calculo II" && it.subtitle == "Falta marcada" })
    }

    @Test
    fun courseDetail_pendingHintIgnoresGradedAndOverdue() {
        val card = buildCoursesData(allCourses, TODAY).courses.first { it.code == "CALC-II" }
        val detail = buildCourseDetail(card, recordsFor(1), TODAY)
        assertEquals(5, detail.pending.size) // la seccion lista TODAS las entregas
        assertTrue(detail.projectionHint.contains("2")) // pendientes reales: 102 y 104
        assertEquals("18 / 20", detail.pending.first { it.name == "Tarea 103" }.scoresLabel)
        assertEquals(1, detail.course.absences)
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class AcademicViewModelRefreshTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun refreshFailure_keepsPreviousDataAndShowsError() {
        val repository = FakeAcademicRepository()
        val viewModel = AcademicViewModel(repository, today = { TODAY })
        val loaded = viewModel.uiState.value
        assertEquals(2, loaded.courses.size)
        assertNull(viewModel.errorMessage.value)

        repository.failure = IllegalStateException("boom")
        viewModel.load()

        assertEquals(loaded, viewModel.uiState.value) // no se pisa con datos vacios
        assertNotNull(viewModel.errorMessage.value)

        repository.failure = null
        viewModel.load()
        assertNull(viewModel.errorMessage.value) // un reintento exitoso limpia el error
    }

    @Test
    fun firstLoadFailure_exposesErrorInsteadOfEmptyData() {
        val repository = FakeAcademicRepository().apply { failure = IllegalStateException("boom") }
        val viewModel = AcademicHomeViewModel(repository, today = { TODAY })
        assertNotNull(viewModel.errorMessage.value)
        assertTrue(viewModel.uiState.value.tasks.isEmpty())
    }

    @Test
    fun homeViewModel_usesInjectedToday() {
        val viewModel = AcademicHomeViewModel(FakeAcademicRepository(), today = { "2026-08-05" })
        assertEquals("05", viewModel.uiState.value.days.single { it.isToday }.dayNumber)
    }

    @Test
    fun scheduleAndDetailViewModels_keepDataOnRefreshFailure() {
        val repository = FakeAcademicRepository()
        val schedule = AcademicScheduleViewModel(repository, today = { TODAY })
        val loadedSchedule = schedule.uiState.value
        assertTrue(loadedSchedule.days.isNotEmpty())

        val card = AcademicViewModel(repository, today = { TODAY }).uiState.value.courses.first()
        val detail = AcademicCourseDetailViewModel(repository, card, today = { TODAY })
        val loadedDetail = detail.uiState.value
        assertTrue(loadedDetail.pending.isNotEmpty())

        repository.failure = IllegalStateException("boom")
        schedule.load()
        detail.load()

        assertEquals(loadedSchedule, schedule.uiState.value)
        assertEquals(loadedDetail, detail.uiState.value)
        assertNotNull(schedule.errorMessage.value)
        assertNotNull(detail.errorMessage.value)
    }
}
