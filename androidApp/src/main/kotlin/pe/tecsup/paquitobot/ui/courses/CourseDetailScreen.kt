package pe.tecsup.paquitobot.ui.courses

import androidx.compose.animation.animateContentSize
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.components.CanvasMockErrorBanner
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Detalle de un curso: promedio proyectado + asistencia + practicas/labs +
 * pendientes. Sin frame Figma. Misma paleta que Home. Sin ViewModel (UI
 * mock) - TODO conectar a `canvas-mock` (courseAttendance/courseClassSessions
 * por curso especifico) para que la asistencia con fecha real y las
 * practicas/labs dejen de ser el mismo mock para todos los cursos excepto
 * "calculo-ii" (ver skill `canvas-mock-backend`).
 *
 * Iteracion 2026-08-19 (pedido del usuario, con captura):
 * - Las 4 secciones (Asistencia, Practicas, Laboratorios, Pendientes) ahora
 *   son desplegables/retraibles (`CollapsibleSection`) - antes ocupaban
 *   toda la pantalla siempre expandidas.
 * - "Foros" se renombra a "Pendientes" - la idea es agrupar TODO lo que
 *   sigue abierto (no solo foros), y mostrarlo bien estructurado.
 * - Cada item de Practicas/Laboratorios/Pendientes es clickeable y abre un
 *   popup (`EvaluationDetailDialog`) con la info completa y estructurada.
 * - Asistencia ahora muestra la fecha real de cada sesion (presente o
 *   falta), no solo el conteo agregado.
 */
@Composable
fun CourseDetailScreen(
    data: CourseDetailData = CourseDetailData.calculoIi(),
    onBackClick: () -> Unit,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedItem by remember { mutableStateOf<EvaluationItem?>(null) }

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
            if (errorMessage != null) {
                CanvasMockErrorBanner(message = errorMessage, onRetry = onRetry)
            }
            GradeHero(data = data)
            AttendanceSection(
                used = data.course.absences,
                limit = data.course.absenceLimit,
                entries = data.attendance,
            )
            EvaluationSection(title = "Practicas", items = data.practices, onItemClick = { selectedItem = it })
            EvaluationSection(title = "Laboratorios", items = data.labs, onItemClick = { selectedItem = it })
            EvaluationSection(title = data.pendingTitle, items = data.pending, onItemClick = { selectedItem = it })
        }
    }

    selectedItem?.let { item ->
        EvaluationDetailDialog(item = item, onDismiss = { selectedItem = null })
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

/**
 * Cabecera clickeable, chevron que rota 180° - mismo patron reusado por
 * [AttendanceSection] y [EvaluationSection] para desplegar/retraer.
 */
@Composable
private fun SectionHeader(
    title: String,
    expanded: Boolean,
    onClick: () -> Unit,
    trailingHint: String? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = PaquitoTypography.TaskTitle,
                color = PaquitoColors.TextHomeStrong,
            )
            if (trailingHint != null) {
                Text(
                    text = trailingHint,
                    style = PaquitoTypography.Caption,
                    color = PaquitoColors.TextHomeTaskLabel,
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.ic_paquito_arrow_back),
            contentDescription = if (expanded) "Retraer" else "Desplegar",
            modifier = Modifier
                .size(16.dp)
                .rotate(if (expanded) 90f else -90f),
            contentScale = ContentScale.Fit,
        )
    }
}

@Composable
private fun AttendanceSection(used: Int, limit: Int, entries: List<AttendanceEntry>) {
    var expanded by remember { mutableStateOf(false) }
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
            .animateContentSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SectionHeader(title = "Asistencia", expanded = expanded, onClick = { expanded = !expanded })
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
        if (expanded && entries.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                entries.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = entry.dateLabel,
                            style = PaquitoTypography.Caption,
                            color = PaquitoColors.TextOnCardMuted,
                        )
                        Text(
                            text = if (entry.isPresent) "Presente" else "Falta",
                            style = PaquitoTypography.Caption,
                            color = if (entry.isPresent) PaquitoColors.StateSuccess else PaquitoColors.StateDanger,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EvaluationSection(
    title: String,
    items: List<EvaluationItem>,
    onItemClick: (EvaluationItem) -> Unit,
) {
    if (items.isEmpty()) return
    var expanded by remember { mutableStateOf(true) }
    Column(
        modifier = Modifier.animateContentSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SectionHeader(
            title = title,
            expanded = expanded,
            onClick = { expanded = !expanded },
            trailingHint = "${items.size}",
        )
        if (expanded) {
            items.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PaquitoShapes.medium)
                        .background(PaquitoColors.SurfaceHomeTaskListBg)
                        .clickable(onClick = { onItemClick(item) })
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
}

/**
 * Popup de detalle de una practica/laboratorio/pendiente - info completa y
 * estructurada en vez de solo el nombre + nota que se ve en la lista.
 */
@Composable
private fun EvaluationDetailDialog(item: EvaluationItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
        title = { Text(item.name) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(label = "Estado", value = stateLabel(item.state))
                DetailRow(label = "Peso en la nota final", value = item.weightLabel)
                DetailRow(label = "Nota / puntaje", value = item.scoresLabel)
                if (item.detail.isNotBlank()) {
                    Text(
                        text = item.detail,
                        style = PaquitoTypography.BodySmall,
                        color = PaquitoColors.TextOnCardStrong,
                    )
                }
            }
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = PaquitoTypography.BodySmall, color = PaquitoColors.TextHomeMuted)
        Text(text = value, style = PaquitoTypography.BodySmall, color = PaquitoColors.TextOnCardStrong)
    }
}

private fun stateLabel(state: EvaluationState): String = when (state) {
    EvaluationState.Graded -> "Calificado"
    EvaluationState.Pending -> "Pendiente"
    EvaluationState.Missing -> "No entregado"
}

/** Un registro de asistencia real: fecha de la sesion + si el estudiante estuvo presente. */
data class AttendanceEntry(
    val dateLabel: String,
    val isPresent: Boolean,
)

data class EvaluationItem(
    val name: String,
    val weightLabel: String,
    val scoresLabel: String,
    val state: EvaluationState,
    /** Texto libre para el popup de detalle - vacio si no hay mas info que mostrar. */
    val detail: String = "",
)

enum class EvaluationState { Graded, Pending, Missing }

data class CourseDetailData(
    val course: CourseCardData,
    val headline: String,
    val projectionHint: String,
    val attendance: List<AttendanceEntry>,
    val practices: List<EvaluationItem>,
    val labs: List<EvaluationItem>,
    val pending: List<EvaluationItem>,
    // Titulo de la seccion de [pending] - "Pendientes" para el mock
    // categorizado (Practicas/Laboratorios/Pendientes), "Entregas" para
    // datos reales de canvas-mock (que no categoriza assignments, ver
    // `AcademicCourseDetailViewModel`).
    val pendingTitle: String = "Pendientes",
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
                attendance = listOf(
                    AttendanceEntry("03/08/26", isPresent = true),
                    AttendanceEntry("05/08/26", isPresent = true),
                    AttendanceEntry("10/08/26", isPresent = false),
                    AttendanceEntry("12/08/26", isPresent = true),
                    AttendanceEntry("17/08/26", isPresent = false),
                ),
                practices = listOf(
                    EvaluationItem(
                        "Practica 1", "8%", "16 / 15", EvaluationState.Graded,
                        detail = "Corregida el 04/08. Buen manejo de derivadas implicitas.",
                    ),
                    EvaluationItem(
                        "Practica 2", "8%", "11 / 13", EvaluationState.Graded,
                        detail = "Corregida el 11/08. Perdiste puntos en la pregunta 3 (limites).",
                    ),
                    EvaluationItem(
                        "Practica 3", "8%", "Pendiente", EvaluationState.Pending,
                        detail = "Se abre el 18/08, vence el 22/08 23:59.",
                    ),
                    EvaluationItem(
                        "Practica 4", "8%", "Sin abrir", EvaluationState.Pending,
                        detail = "Todavia no esta disponible - se habilita al cerrar la Practica 3.",
                    ),
                ),
                labs = listOf(
                    EvaluationItem("Lab 1", "5%", "18 / 17", EvaluationState.Graded, detail = "Corregido el 05/08."),
                    EvaluationItem("Lab 2", "5%", "14 / 15", EvaluationState.Graded, detail = "Corregido el 08/08."),
                    EvaluationItem(
                        "Lab 3", "5%", "12 / —", EvaluationState.Graded,
                        detail = "Corregido el 12/08. Falta la nota de exposicion oral.",
                    ),
                    EvaluationItem(
                        "Lab 4", "15%", "Hoy 23:59", EvaluationState.Pending,
                        detail = "El de mayor peso del ciclo - entregalo completo para subir el promedio.",
                    ),
                    EvaluationItem("Lab 5", "5%", "Sin abrir", EvaluationState.Pending),
                    EvaluationItem("Lab 6", "5%", "Sin abrir", EvaluationState.Pending),
                    EvaluationItem("Lab 7", "5%", "Sin abrir", EvaluationState.Pending),
                    EvaluationItem("Lab 8", "5%", "Sin abrir", EvaluationState.Pending),
                ),
                pending = listOf(
                    EvaluationItem(
                        "Foro 1", "2%", "Entregado", EvaluationState.Graded,
                        detail = "Participaste el 06/08 - cumple el minimo de 2 respuestas.",
                    ),
                    EvaluationItem(
                        "Foro 2", "2%", "Falta", EvaluationState.Missing,
                        detail = "Vencio el 15/08 - todavia se puede entregar con penalidad hasta el 20/08.",
                    ),
                ),
            )
        }

        private fun generic(course: CourseCardData): CourseDetailData = CourseDetailData(
            course = course,
            headline = "Notas y faltas de este curso",
            projectionHint = course.trackHint,
            attendance = listOf(
                AttendanceEntry("04/08/26", isPresent = true),
                AttendanceEntry("11/08/26", isPresent = course.absences == 0),
            ),
            practices = listOf(
                EvaluationItem("Practica 1", "10%", "15 / 16", EvaluationState.Graded),
                EvaluationItem("Practica 2", "10%", "Pendiente", EvaluationState.Pending),
            ),
            labs = listOf(
                EvaluationItem("Lab 1", "8%", "17 / 14", EvaluationState.Graded),
                EvaluationItem("Lab 2", "8%", course.nextDueLabel, EvaluationState.Pending),
            ),
            pending = emptyList(),
        )
    }
}

@Preview(showBackground = true, name = "7b Detalle curso")
@Composable
private fun CourseDetailScreenPreview() {
    PaquitoTheme { CourseDetailScreen(onBackClick = {}) }
}
