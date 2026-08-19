package pe.tecsup.paquitobot.ui.courses

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.components.AppTabScaffold
import pe.tecsup.paquitobot.ui.components.CanvasMockErrorBanner
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Pestaña Cursos. Frame Figma `351:697` solo tiene navbar.
 * Dolor: "no saben si van bien" — promedio, faltas y proxima entrega por curso.
 *
 * Iteracion 2026-08-19 (bug real, reportado por el usuario en Horarios,
 * mismo patron aca): la lista ya NO tiene su propio
 * `Column(weight(1f).verticalScroll(...))` - desde el fix del Navbar
 * (2026-08-13), `AppTabScaffold` scrollea toda la pantalla como una sola
 * superficie. Un scroll anidado adentro de otro sin altura acotada dejaba
 * el titulo pegado arriba, como si fuera un header fijo.
 */
@Composable
fun CoursesScreen(
    data: CoursesScreenData = CoursesScreenData.default(),
    currentTab: NavTab = NavTab.Cursos,
    onTabSelected: (NavTab) -> Unit = {},
    onPaquitoClick: () -> Unit = {},
    onCourseClick: (CourseCardData) -> Unit = {},
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppTabScaffold(
        currentTab = currentTab,
        onTabSelected = onTabSelected,
        onPaquitoClick = onPaquitoClick,
        notificationCount = data.pendingCount,
        modifier = modifier,
    ) {
        if (errorMessage != null) {
            CanvasMockErrorBanner(message = errorMessage, onRetry = onRetry)
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Cursos",
                style = PaquitoTypography.DisplayLarge,
                color = PaquitoColors.TextHomeStrong,
            )
            Text(
                text = data.termLabel,
                style = PaquitoTypography.HeadlineSmall,
                color = PaquitoColors.TextHomeMuted,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            data.courses.forEach { course ->
                CourseSummaryCard(
                    course = course,
                    onClick = { onCourseClick(course) },
                )
            }
        }
    }
}

@Composable
private fun CourseSummaryCard(
    course: CourseCardData,
    onClick: () -> Unit,
) {
    val absenceColor = when {
        course.absences >= course.absenceLimit - 1 -> PaquitoColors.StateDanger
        course.absences >= course.absenceLimit - 2 -> PaquitoColors.StateWarning
        else -> PaquitoColors.StateSuccess
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PaquitoShapes.large)
            .background(PaquitoColors.SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = course.code,
            style = PaquitoTypography.Caption,
            color = PaquitoColors.TextHomeTaskLabel,
        )
        Text(
            text = course.name,
            style = PaquitoTypography.TaskTitle,
            color = PaquitoColors.TextOnCardStrong,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(
                    text = course.projectedGrade,
                    style = PaquitoTypography.Greeting,
                    color = PaquitoColors.TextHomeStrong,
                )
                Text(
                    text = course.trackHint,
                    style = PaquitoTypography.Caption,
                    color = PaquitoColors.TextOnCardMuted,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${course.absences}/${course.absenceLimit} faltas",
                    style = PaquitoTypography.TaskTitle,
                    color = absenceColor,
                )
                Text(
                    text = course.nextDueLabel,
                    style = PaquitoTypography.Caption,
                    color = PaquitoColors.TextTimestampLarge,
                )
            }
        }
    }
}

data class CourseCardData(
    val id: String,
    val code: String,
    val name: String,
    val projectedGrade: String,
    val trackHint: String,
    val absences: Int,
    val absenceLimit: Int,
    val nextDueLabel: String,
)

data class CoursesScreenData(
    val termLabel: String,
    val pendingCount: Int,
    val courses: List<CourseCardData>,
) {
    companion object {
        fun default(): CoursesScreenData = CoursesScreenData(
            termLabel = "Ciclo 2026-2",
            pendingCount = 3,
            courses = listOf(
                CourseCardData(
                    id = "calculo-ii",
                    code = "CALC-II",
                    name = "Calculo II",
                    projectedGrade = "14.8",
                    trackHint = "Vas justo. Lab 4 mueve 15%.",
                    absences = 2,
                    absenceLimit = 5,
                    nextDueLabel = "Lab 4 · hoy 23:59",
                ),
                CourseCardData(
                    id = "fisica-i",
                    code = "FIS-I",
                    name = "Fisica I",
                    projectedGrade = "16.2",
                    trackHint = "Encaminado",
                    absences = 1,
                    absenceLimit = 5,
                    nextDueLabel = "Practica 3 · jueves",
                ),
                CourseCardData(
                    id = "algoritmos",
                    code = "ALG-I",
                    name = "Algoritmos",
                    projectedGrade = "12.4",
                    trackHint = "En riesgo si falla el foro",
                    absences = 4,
                    absenceLimit = 5,
                    nextDueLabel = "Foro 2 · manana",
                ),
            ),
        )
    }
}

@Preview(showBackground = true, name = "7 Cursos")
@Composable
private fun CoursesScreenPreview() {
    PaquitoTheme { CoursesScreen() }
}
