package pe.tecsup.paquitobot.ui.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.components.NotificationBellButton
import pe.tecsup.paquitobot.ui.events.EventMockData
import pe.tecsup.paquitobot.ui.home.HomeFocusItem
import pe.tecsup.paquitobot.ui.home.HomeHighlightCard
import pe.tecsup.paquitobot.ui.home.HomeWeekSection
import pe.tecsup.paquitobot.ui.home.WeekDayData
import pe.tecsup.paquitobot.ui.academic.deviceToday
import pe.tecsup.paquitobot.ui.academic.weekOf
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography
import pe.tecsup.paquitobot.ui.theme.paquitoSurface
import pe.tecsup.paquitobot.util.AcademicDateUtils
import java.util.Locale

private enum class TeacherRiskFilter(val label: String) {
    All("Todos"),
    Di("DI"),
    Attendance("Advertencia"),
    Grade("Notas"),
}

private val TeacherSubtitleStyle = PaquitoTypography.BodyMedium.copy(fontWeight = FontWeight.Bold)

@Composable
internal fun TeacherDashboardScreen(
    notificationCount: Int,
    onNotificationsClick: () -> Unit,
    onEventsClick: () -> Unit,
    onSendNotification: (TeacherDemoCourse, TeacherDemoStudentCourse) -> Boolean,
) {
    var selectedCareerCode by rememberSaveable { mutableStateOf("C-24") }
    var selectedCourseId by rememberSaveable { mutableStateOf("fisica-i") }
    var careerMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var courseMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var selectedFilter by rememberSaveable { mutableStateOf(TeacherRiskFilter.All) }
    val career = TeacherDemoData.careers.firstOrNull { it.code == selectedCareerCode } ?: TeacherDemoData.careers.first()
    val courses = TeacherDemoData.courses.filter { it.careerCode == career.code }
    val course = courses.firstOrNull { it.id == selectedCourseId } ?: courses.first()
    val currentWeek = remember {
        val today = deviceToday()
        weekOf(today).map { date ->
            WeekDayData(
                dayOfWeek = AcademicDateUtils.weekdayName(date),
                dayNumber = AcademicDateUtils.dayNumber(date),
                dateLabel = AcademicDateUtils.shortDateLabel(date),
                isToday = date == today,
                isCritical = false,
                dateKey = date,
            )
        }
    }
    val weekTitle = currentWeek.firstOrNull()?.let {
        "Semana del ${it.dateLabel} al ${currentWeek.last().dateLabel}"
    } ?: "Semana actual"

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
            NotificationBellButton(unreadCount = notificationCount, onClick = onNotificationsClick)
        }
        Text("Vista docente", style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextHomeStrong)
        HomeWeekSection(
            title = weekTitle,
            days = currentWeek,
            selectedDayNumber = null,
            onDaySelected = null,
            titleStyle = PaquitoTypography.HeadlineSmall.copy(fontWeight = FontWeight.Bold),
        )
        HomeHighlightCard(
            title = "Eventos del campus",
            count = EventMockData.events.size,
            items = EventMockData.events.take(2).map { HomeFocusItem(it.title, "${it.dateLabel} · ${it.venue}") },
            accent = PaquitoColors.StateInfoStrong,
            actionLabel = "Ver todos",
            onClick = onEventsClick,
            detailStyle = TeacherSubtitleStyle,
        )

        Text("Seguimiento de alumnos", style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.TextHomeStrong)
        RiskSummary()

        TeacherDropdownField(
            label = "Carrera",
            selectedLabel = "${career.code} - ${career.name}",
            options = TeacherDemoData.careers.map { it.code to "${it.code} - ${it.name}" },
            expanded = careerMenuExpanded,
            onExpandedChange = { careerMenuExpanded = it },
            onSelect = { code ->
                selectedCareerCode = code
                selectedCourseId = TeacherDemoData.courses.first { it.careerCode == code }.id
                selectedFilter = TeacherRiskFilter.All
                courseMenuExpanded = false
            },
        )
        TeacherDropdownField(
            label = "Cursos",
            selectedLabel = course.name,
            options = courses.map { it.id to it.name },
            expanded = courseMenuExpanded,
            onExpandedChange = { courseMenuExpanded = it },
            onSelect = { selectedCourseId = it },
        )

        Column(
            modifier = Modifier.fillMaxWidth().paquitoSurface(PaquitoShapes.card).padding(PaquitoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm),
        ) {
            Text(course.name, style = PaquitoTypography.BodyLarge.copy(fontWeight = FontWeight.SemiBold), color = PaquitoColors.TextOnCardStrong)
            Text("${course.area} · ${course.students.size} alumnos", style = TeacherSubtitleStyle, color = PaquitoColors.TextOnCardMuted)
            Text("Semana 11 · 11 clases realizadas", style = TeacherSubtitleStyle, color = PaquitoColors.TextOnCardMuted)
        }

        Text("Filtrar alumnos", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextHomeStrong)
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm),
        ) {
            TeacherRiskFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { selectedFilter = filter },
                    label = { Text(filter.label, style = PaquitoTypography.BodyMedium) },
                    colors = teacherFilterChipColors(),
                )
            }
        }

        val students = course.students
            .filter { it.matches(selectedFilter) }
            .sortedWith(compareBy<TeacherDemoStudentCourse> { it.riskPriority() }.thenBy { it.displayName })
        if (students.isEmpty()) {
            Text("No hay alumnos con esta señal en el curso.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardMuted)
        } else {
            students.forEach { student ->
                StudentRiskCard(
                    courseId = course.id,
                    student = student,
                    onSendNotification = {
                        onSendNotification(course, student)
                    },
                )
            }
        }

    }
}

@Composable
private fun TeacherDropdownField(
    label: String,
    selectedLabel: String,
    options: List<Pair<String, String>>,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onSelect: (String) -> Unit,
) {
    Text(label, style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextHomeStrong)
    Box(Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { onExpandedChange(true) },
            modifier = Modifier.fillMaxWidth(),
            shape = PaquitoShapes.large,
            border = BorderStroke(1.dp, PaquitoColors.BorderDefault),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = PaquitoColors.SurfaceGlassSoft,
                contentColor = PaquitoColors.TextOnCardStrong,
            ),
        ) {
            Text(
                selectedLabel,
                modifier = Modifier.weight(1f),
                style = PaquitoTypography.BodyMedium,
                color = PaquitoColors.TextOnCardStrong,
                maxLines = 2,
            )
            Text("⌄", style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.StateInfoStrong)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onExpandedChange(false) }) {
            options.forEach { (key, value) ->
                DropdownMenuItem(
                    text = {
                        Text(
                            value,
                            style = PaquitoTypography.BodyMedium,
                            color = PaquitoColors.TextOnCardStrong,
                            maxLines = 2,
                        )
                    },
                    onClick = {
                        onSelect(key)
                        onExpandedChange(false)
                    },
                )
            }
        }
    }
}

@Composable
private fun RiskSummary() {
    Column(verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm)) {
        Row(horizontalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm)) {
            SummaryMetric("Alumnos con señales", TeacherDemoData.uniqueStudentsWithRisk.toString(), PaquitoColors.StateInfoStrong, Modifier.weight(1f))
            SummaryMetric("Riesgo por notas", TeacherDemoData.uniqueStudentsWithGradeRisk.toString(), PaquitoColors.StateDanger, Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm)) {
            SummaryMetric("Advertencia", TeacherDemoData.uniqueStudentsWithWarning.toString(), PaquitoColors.StateWarning, Modifier.weight(1f))
            SummaryMetric("DI", TeacherDemoData.uniqueStudentsWithDi.toString(), PaquitoColors.StateDanger, Modifier.weight(1f))
        }
    }
}

@Composable
private fun teacherFilterChipColors() = FilterChipDefaults.filterChipColors(
    containerColor = PaquitoColors.SurfaceGlassSoft,
    labelColor = PaquitoColors.TextOnCardMuted,
    selectedContainerColor = PaquitoColors.OverlayInfoSubtle,
    selectedLabelColor = PaquitoColors.StateInfoStrong,
)

@Composable
private fun SummaryMetric(label: String, value: String, accent: androidx.compose.ui.graphics.Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.paquitoSurface().padding(PaquitoSpacing.md),
        verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.xs),
    ) {
        Text(value, style = PaquitoTypography.AlertCardTitle, color = accent)
        Text(label, style = TeacherSubtitleStyle, color = PaquitoColors.TextOnCardMuted)
    }
}

@Composable
private fun StudentRiskCard(
    courseId: String,
    student: TeacherDemoStudentCourse,
    onSendNotification: () -> Unit,
) {
    var expanded by rememberSaveable(courseId, student.studentId) { mutableStateOf(false) }
    val average = student.average
    Column(
        modifier = Modifier.fillMaxWidth().paquitoSurface(PaquitoShapes.card).padding(PaquitoSpacing.md),
        verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm),
    ) {
        Text(student.displayName, style = PaquitoTypography.BodyLarge.copy(fontWeight = FontWeight.SemiBold), color = PaquitoColors.TextOnCardStrong)
        student.email?.let { Text(it, style = TeacherSubtitleStyle, color = PaquitoColors.TextOnCardMuted) }
        Text(
            average?.let { "Promedio ${it.formatOneDecimal()} · ${student.finalGrades.size} notas · ${if (student.courseComplete) "Completo" else "Parcial"}" }
                ?: "Sin notas finales registradas",
            style = PaquitoTypography.BodyMedium,
            color = PaquitoColors.TextOnCardStrong,
        )
        if (student.gradeRisk) SignalLabel("Riesgo por notas · parcial", PaquitoColors.StateDanger)

        Text(
            "${student.absenceWeeks.size} faltas de ${student.performedClasses} clases · ${(student.absenceRate * 100).formatOneDecimal()} %",
            style = PaquitoTypography.BodyMedium,
            color = PaquitoColors.TextOnCardStrong,
        )
        when (student.attendanceSignal) {
            TeacherDemoAttendanceSignal.Di -> SignalLabel("DI por asistencia", PaquitoColors.StateDanger)
            TeacherDemoAttendanceSignal.Warning -> SignalLabel("Advertencia de asistencia", PaquitoColors.StateWarning)
            TeacherDemoAttendanceSignal.None -> Text("Sin señal de asistencia", style = TeacherSubtitleStyle, color = PaquitoColors.TextOnCardMuted)
        }
        if (student.tardyWeeks.isNotEmpty()) {
            Text("${student.tardyWeeks.size} tardanza, contada por separado", style = TeacherSubtitleStyle, color = PaquitoColors.TextOnCardMuted)
        }

        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Ocultar detalle" else "Ver notas y asistencias", style = PaquitoTypography.BodyMedium, color = PaquitoColors.StateInfoStrong)
        }
        if (expanded) StudentAcademicDetail(student)

        if (student.hasAnyRisk) {
            Spacer(Modifier.height(PaquitoSpacing.xs))
            OutlinedButton(onClick = onSendNotification, modifier = Modifier.fillMaxWidth()) {
                Text("Enviar mensaje al alumno", style = PaquitoTypography.BodyMedium, color = PaquitoColors.StateInfoStrong)
            }
        }
    }
}

@Composable
private fun SignalLabel(text: String, color: androidx.compose.ui.graphics.Color) {
    Surface(color = color.copy(alpha = 0.12f), shape = PaquitoShapes.pill) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = PaquitoSpacing.sm, vertical = PaquitoSpacing.xs),
            style = PaquitoTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
            color = color,
        )
    }
}

@Composable
private fun StudentAcademicDetail(student: TeacherDemoStudentCourse) {
    Column(
        modifier = Modifier.fillMaxWidth().background(PaquitoColors.SurfaceOverlay, PaquitoShapes.medium).padding(PaquitoSpacing.md),
        verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.xs),
    ) {
        Text("Notas registradas", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextOnCardStrong)
        student.weeklyGrades.sortedBy { it.week }.forEach { grade ->
            Text("Semana ${grade.week} · ${grade.label}: ${grade.score.toInt()}/20", style = PaquitoTypography.Caption, color = PaquitoColors.TextOnCardMuted)
        }
        student.completedLabGrades.forEach { (name, score) ->
            val parts = student.labSubnotes.filter { it.label == name }.sortedBy { it.part }
            Text("$name · subnotas ${parts.joinToString(" y ") { it.score.toInt().toString() }} · promedio ${score.formatOneDecimal()}",
                style = PaquitoTypography.Caption, color = PaquitoColors.TextOnCardMuted)
        }
        student.partialLabs.forEach { parts ->
            val lab = parts.first()
            Text("${lab.label} · subnota ${lab.part}: ${lab.score.toInt()}/20 · falta ${lab.requiredParts - parts.size} · no incluida en el promedio",
                style = PaquitoTypography.Caption, color = PaquitoColors.StateWarning)
        }
        Text("Faltas en semanas: ${student.absenceWeeks.sorted().joinToString().ifEmpty { "ninguna" }}", style = PaquitoTypography.Caption, color = PaquitoColors.TextOnCardMuted)
        Text("Tardanzas en semanas: ${student.tardyWeeks.sorted().joinToString().ifEmpty { "ninguna" }}", style = PaquitoTypography.Caption, color = PaquitoColors.TextOnCardMuted)
    }
}

private fun TeacherDemoStudentCourse.matches(filter: TeacherRiskFilter): Boolean = when (filter) {
    TeacherRiskFilter.All -> true
    TeacherRiskFilter.Di -> attendanceSignal == TeacherDemoAttendanceSignal.Di
    TeacherRiskFilter.Attendance -> attendanceSignal == TeacherDemoAttendanceSignal.Warning
    TeacherRiskFilter.Grade -> gradeRisk
}

private fun TeacherDemoStudentCourse.riskPriority(): Int = when {
    attendanceSignal == TeacherDemoAttendanceSignal.Di -> 0
    attendanceSignal == TeacherDemoAttendanceSignal.Warning -> 1
    gradeRisk -> 2
    else -> 3
}

private fun Double.formatOneDecimal(): String = String.format(Locale.US, "%.1f", this)

@Preview(showBackground = true, widthDp = 390, heightDp = 1200)
@Composable
private fun TeacherDashboardPreview() {
    PaquitoTheme {
        TeacherDashboardScreen(
            notificationCount = 1,
            onNotificationsClick = {},
            onEventsClick = {},
            onSendNotification = { _, _ -> true },
        )
    }
}
