package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Item de la lista (titulo + subtítulo + acción "Ver todo").
 */
data class TaskListHeader(val title: String, val actionLabel: String? = null)

/**
 * Item de la lista de tareas (TaskInfo).
 */
data class TaskListItem(
    val initials: String,
    val badge: TaskBadge,
    val title: String,
    val subtitle: String,
    val timestamp: String,
)

/**
 * Lista vertical de tareas con un encabezado opcional.
 *
 * Estructura (Figma nodeId 364:241 + 364:244):
 *   [Header "Paquito te avisó" + link "Ver todo"]
 *   [Card glassmorphic con varias TaskInfo divididas por borders]
 */
@Composable
fun TaskList(
    header: TaskListHeader?,
    items: List<TaskListItem>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(11.dp),
    ) {
        if (header != null) {
            TaskListHeaderView(header.title, header.actionLabel)
        }

        // Card con glassmorphism: fondo translucido, border sutil, blur, shadow.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = PaquitoShapes.card,
                )
                .clip(PaquitoShapes.card)
                .background(PaquitoColors.SurfaceGlassStrong)
                .border(
                    width = 1.dp,
                    color = PaquitoColors.BorderDefault,
                    shape = PaquitoShapes.card,
                ),
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = PaquitoColors.BorderDefault,
                    )
                }
                TaskInfo(
                    initials = item.initials,
                    badge = item.badge,
                    title = item.title,
                    subtitle = item.subtitle,
                    timestamp = item.timestamp,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
private fun TaskListHeaderView(title: String, actionLabel: String?) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = PaquitoTypography.BodyMedium.copy(
                color = PaquitoColors.TextOnCardMutedAlt,
            ),
        )
        if (actionLabel != null) {
            Text(
                text = actionLabel,
                style = PaquitoTypography.BodyMedium.copy(
                    color = PaquitoColors.TextLink,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskListPreview() {
    PaquitoTheme {
        TaskList(
            header = TaskListHeader(title = "Paquito te avisó", actionLabel = "Ver todo"),
            items = listOf(
                TaskListItem("NT", TaskBadge.Info,    "Nombre :v",                    "Curso",                                      "hace cuanto"),
                TaskListItem("FO", TaskBadge.Warning, "Foro de Ética por vencer",     "Martes 20:00 · falta tu respuesta",          "2 d"),
                TaskListItem("EX", TaskBadge.Neutral, "Parcial de Álgebra Lineal",    "Jueves 08:00 · aula B-204",                  "4 d"),
            ),
            modifier = Modifier.padding(PaquitoSpacing.md),
        )
    }
}

// Wrapper de padding redundante eliminado: ya hay un Modifier.padding(Dp) en
// androidx.compose.foundation.layout importado arriba.