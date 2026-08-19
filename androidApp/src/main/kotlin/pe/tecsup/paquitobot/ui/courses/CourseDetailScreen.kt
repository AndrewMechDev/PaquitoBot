package pe.tecsup.paquitobot.ui.courses

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Detalle de un curso: promedio proyectado + practicas/labs + faltas.
 * Sin frame Figma. Misma paleta que Home. Sin ViewModel (UI mock).
 */
@Composable
fun CourseDetailScreen(
    data: CourseDetailData = CourseDetailData.calculoIi(),
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.SurfaceHomeCanvas)
            .systemBarsPadding()
            .padding(horizontal = PaquitoSpacing.lg)
            .padding(top = 8.dp, bottom = PaquitoSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_paquito_arrow_back),
            contentDescription = "Volver",
            modifier = Modifier
                .size(21.dp)
                .clickable(onClick = onBackClick),
            contentScale = ContentScale.Fit,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = data.course.name,
                style = PaquitoTypography.DisplayLarge,
                color = PaquitoColors.TextHomeStrong,
            )
            Text(
                text = data.headline,
                style = PaquitoTypography.BodySmall,
                color = PaquitoColors.TextHomeMuted,
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            GradeHero(data = data)
            AbsenceStrip(
                used = data.course.absences,
                limit = data.course.absenceLimit,
            )
            EvaluationSection(title = "Practicas", items = data.practices)
            EvaluationSection(title = "Laboratorios", items = data.labs)
            EvaluationSection(title = "Foros", items = data.forums)
        }
    }
}

@Composable
private fun GradeHero(data: CourseDetailData) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PaquitoShapes.large)
            .background(PaquitoColors.SurfaceHomeWeekBg)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = data.course.projectedGrade,
            style = PaquitoTypography.Greeting,
            color = PaquitoColors.TextOnWhite,
        )
        Text(
            text = data.projectionHint,
            style = PaquitoTypography.BodySmall,
            color = PaquitoColors.TextOnDarkMuted,
        )
    }
}

@Composable
private fun AbsenceStrip(used: Int, limit: Int) {
    val remaining = (limit - used).coerceAtLeast(0)
    val color = when {
        remaining <= 1 -> PaquitoColors.StateDanger
        remaining <= 2 -> PaquitoColors.StateWarning
        else -> PaquitoColors.StateSuccess
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PaquitoShapes.large)
            .background(PaquitoColors.SurfaceElevated)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Asistencia",
            style = PaquitoTypography.TaskTitle,
            color = PaquitoColors.TextOnCardStrong,
        )
        Text(
            text = "$used de $limit faltas. $remaining para el limite (jalado automatico).",
            style = PaquitoTypography.Caption,
            color = color,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(limit) { index ->
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < used) color else PaquitoColors.BorderDefault,
                        ),
                )
            }
        }
    }
}

@Composable
private fun EvaluationSection(title: String, items: List<EvaluationItem>) {
    if (items.isEmpty()) return
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = PaquitoTypography.TaskTitle,
            color = PaquitoColors.TextHomeStrong,
        )
        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PaquitoShapes.medium)
                    .background(PaquitoColors.SurfaceHomeTaskListBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = PaquitoTypography.TaskTitle,
                        color = PaquitoColors.TextOnCardStrong,
                    )
                    Text(
                        text = item.weightLabel,
                        style = PaquitoTypography.Caption,
                        color = PaquitoColors.TextHomeTaskLabel,
                    )
                }
                Text(
                    text = item.scoresLabel,
                    style = PaquitoTypography.TaskTitle,
                    color = when (item.state) {
                        EvaluationState.Pending -> PaquitoColors.TextTimestampLarge
                        EvaluationState.Missing -> PaquitoColors.StateDanger
                        EvaluationState.Graded -> PaquitoColors.TextOnCardStrong
                    },
                )
            }
        }
    }
}

data class EvaluationItem(
    val name: String,
    val weightLabel: String,
    val scoresLabel: String,
    val state: EvaluationState,
)

enum class EvaluationState { Graded, Pending, Missing }

data class CourseDetailData(
    val course: CourseCardData,
    val headline: String,
    val projectionHint: String,
    val practices: List<EvaluationItem>,
    val labs: List<EvaluationItem>,
    val forums: List<EvaluationItem>,
) {
    companion object {
        fun forCourse(course: CourseCardData): CourseDetailData =
            if (course.id == "calculo-ii") calculoIi() else generic(course)

        fun calculoIi(): CourseDetailData {
            val course = CoursesScreenData.default().courses.first()
            return CourseDetailData(
                course = course,
                headline = "4 practicas · 8 labs · a mitad de ciclo",
                projectionHint = "Si entregas el Lab 4 completo subes a ~15.6. Punto debil: practicos (11 y 13).",
                practices = listOf(
                    EvaluationItem("Practica 1", "8%", "16 / 15", EvaluationState.Graded),
                    EvaluationItem("Practica 2", "8%", "11 / 13", EvaluationState.Graded),
                    EvaluationItem("Practica 3", "8%", "Pendiente", EvaluationState.Pending),
                    EvaluationItem("Practica 4", "8%", "Sin abrir", EvaluationState.Pending),
                ),
                labs = listOf(
                    EvaluationItem("Lab 1", "5%", "18 / 17", EvaluationState.Graded),
                    EvaluationItem("Lab 2", "5%", "14 / 15", EvaluationState.Graded),
                    EvaluationItem("Lab 3", "5%", "12 / —", EvaluationState.Graded),
                    EvaluationItem("Lab 4", "15%", "Hoy 23:59", EvaluationState.Pending),
                    EvaluationItem("Lab 5", "5%", "Sin abrir", EvaluationState.Pending),
                    EvaluationItem("Lab 6", "5%", "Sin abrir", EvaluationState.Pending),
                    EvaluationItem("Lab 7", "5%", "Sin abrir", EvaluationState.Pending),
                    EvaluationItem("Lab 8", "5%", "Sin abrir", EvaluationState.Pending),
                ),
                forums = listOf(
                    EvaluationItem("Foro 1", "2%", "Entregado", EvaluationState.Graded),
                    EvaluationItem("Foro 2", "2%", "Falta", EvaluationState.Missing),
                ),
            )
        }

        private fun generic(course: CourseCardData): CourseDetailData = CourseDetailData(
            course = course,
            headline = "Notas y faltas de este curso",
            projectionHint = course.trackHint,
            practices = listOf(
                EvaluationItem("Practica 1", "10%", "15 / 16", EvaluationState.Graded),
                EvaluationItem("Practica 2", "10%", "Pendiente", EvaluationState.Pending),
            ),
            labs = listOf(
                EvaluationItem("Lab 1", "8%", "17 / 14", EvaluationState.Graded),
                EvaluationItem("Lab 2", "8%", course.nextDueLabel, EvaluationState.Pending),
            ),
            forums = emptyList(),
        )
    }
}

@Preview(showBackground = true, name = "7b Detalle curso")
@Composable
private fun CourseDetailScreenPreview() {
    PaquitoTheme { CourseDetailScreen(onBackClick = {}) }
}
