package pe.tecsup.paquitobot.ui.schedule

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.components.AppTabScaffold
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Pestaña Horarios. Frame Figma `351:719` solo tiene navbar.
 * Une clases (faltas) + vencimientos (desorden de canales) en una linea de tiempo.
 */
@Composable
fun ScheduleScreen(
    data: ScheduleScreenData = ScheduleScreenData.default(),
    currentTab: NavTab = NavTab.Horarios,
    onTabSelected: (NavTab) -> Unit = {},
    onPaquitoClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppTabScaffold(
        currentTab = currentTab,
        onTabSelected = onTabSelected,
        onPaquitoClick = onPaquitoClick,
        notificationCount = data.pendingCount,
        modifier = modifier,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Horarios",
                style = PaquitoTypography.DisplayLarge,
                color = PaquitoColors.TextHomeStrong,
            )
            Text(
                text = data.weekLabel,
                style = PaquitoTypography.HeadlineSmall,
                color = PaquitoColors.TextHomeMuted,
            )
        }
        AbsenceBanner(used = data.absencesUsed, limit = data.absencesLimit)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            data.days.forEach { day ->
                DayBlock(day = day)
            }
        }
    }
}

@Composable
private fun AbsenceBanner(used: Int, limit: Int) {
    val remaining = (limit - used).coerceAtLeast(0)
    val color = if (remaining <= 2) PaquitoColors.StateDanger else PaquitoColors.StateWarning
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PaquitoShapes.large)
            .background(PaquitoColors.SurfaceElevated)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = "Asistencia del ciclo",
            style = PaquitoTypography.TaskTitle,
            color = PaquitoColors.TextOnCardStrong,
        )
        Text(
            text = "$used de $limit faltas usadas. Te quedan $remaining antes del jalado automatico.",
            style = PaquitoTypography.BodySmall,
            color = color,
        )
    }
}

@Composable
private fun DayBlock(day: ScheduleDay) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = day.label,
            style = PaquitoTypography.TaskTitle,
            color = PaquitoColors.TextHomeStrong,
        )
        day.events.forEach { event ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(PaquitoShapes.medium)
                    .background(PaquitoColors.SurfaceElevated)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = event.time,
                    style = PaquitoTypography.Caption,
                    color = PaquitoColors.TextTimestampLarge,
                    modifier = Modifier.width(56.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = PaquitoTypography.TaskTitle,
                        color = PaquitoColors.TextOnCardStrong,
                    )
                    Text(
                        text = event.subtitle,
                        style = PaquitoTypography.Caption,
                        color = PaquitoColors.TextOnCardMuted,
                    )
                }
                Text(
                    text = event.kind.label,
                    style = PaquitoTypography.ChipLabel,
                    color = event.kind.color(),
                )
            }
        }
    }
}

enum class ScheduleKind(val label: String) {
    Clase("CLASE"),
    Entrega("ENTREGA"),
    Falta("FALTA"),
}

private fun ScheduleKind.color(): Color = when (this) {
    ScheduleKind.Clase -> PaquitoColors.StateInfoStrong
    ScheduleKind.Entrega -> PaquitoColors.TextTimestampLargeAccent
    ScheduleKind.Falta -> PaquitoColors.StateDanger
}

data class ScheduleEvent(
    val time: String,
    val title: String,
    val subtitle: String,
    val kind: ScheduleKind,
)

data class ScheduleDay(
    val label: String,
    val events: List<ScheduleEvent>,
)

data class ScheduleScreenData(
    val weekLabel: String,
    val pendingCount: Int,
    val absencesUsed: Int,
    val absencesLimit: Int,
    val days: List<ScheduleDay>,
) {
    companion object {
        fun default(): ScheduleScreenData = ScheduleScreenData(
            weekLabel = "Semana 10 · 3 al 9 ago",
            pendingCount = 3,
            absencesUsed = 2,
            absencesLimit = 5,
            days = listOf(
                ScheduleDay(
                    label = "Lunes 3",
                    events = listOf(
                        ScheduleEvent("08:00", "Calculo II", "Aula 302", ScheduleKind.Clase),
                        ScheduleEvent("23:59", "Lab 4 — Calculo II", "Aula virtual", ScheduleKind.Entrega),
                    ),
                ),
                ScheduleDay(
                    label = "Martes 4",
                    events = listOf(
                        ScheduleEvent("10:00", "Fisica I", "Lab B", ScheduleKind.Clase),
                        ScheduleEvent("10:00", "Fisica I", "No marcaste asistencia", ScheduleKind.Falta),
                    ),
                ),
                ScheduleDay(
                    label = "Jueves 6",
                    events = listOf(
                        ScheduleEvent("14:00", "Algoritmos", "Aula 110", ScheduleKind.Clase),
                        ScheduleEvent("23:59", "Practica 3 — Fisica I", "Correo del docente", ScheduleKind.Entrega),
                    ),
                ),
                ScheduleDay(
                    label = "Viernes 7",
                    events = listOf(
                        ScheduleEvent("18:00", "Foro 2 — Algoritmos", "Aula virtual", ScheduleKind.Entrega),
                    ),
                ),
            ),
        )
    }
}

@Preview(showBackground = true, name = "8 Horarios")
@Composable
private fun ScheduleScreenPreview() {
    PaquitoTheme { ScheduleScreen() }
}
