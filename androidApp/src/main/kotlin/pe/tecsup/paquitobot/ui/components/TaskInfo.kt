package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Tipo de tarea que define el color del badge de iniciales.
 *
 * - [Info]    → badge azul claro (recordatorio académico, anuncios).
 * - [Warning] → badge amarillo (foro por vencer, quiz próximo).
 * - [Neutral] → badge gris (parciales, evaluaciones, eventos).
 */
enum class TaskBadge { Info, Warning, Neutral }

/**
 * Fila individual de una lista de tareas.
 *
 * Estructura (Figma nodeId 364:244, 364:255, 364:265):
 *   [Badge 38x38]  [Título + Subtítulo]  [Timestamp]
 *
 * El badge es un círculo/rect redondeado con 2 iniciales del curso o tarea.
 * El título es el nombre de la tarea; el subtítulo es curso o descripción
 * corta; el timestamp es el tiempo restante ("2 d", "hace cuanto", etc.).
 */
@Composable
fun TaskInfo(
    initials: String,
    badge: TaskBadge,
    title: String,
    subtitle: String,
    timestamp: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TaskBadgeView(initials = initials, badge = badge)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Text(
                text = title,
                style = PaquitoTypography.TaskTitle,
                color = PaquitoColors.TextOnCardStrong,
            )
            Text(
                text = subtitle,
                style = PaquitoTypography.TaskSubtitle,
                color = PaquitoColors.TextOnCardMuted,
            )
        }
        Text(
            text = timestamp,
            style = PaquitoTypography.Timestamp,
            color = PaquitoColors.TextTimestamp,
        )
    }
}

@Composable
private fun TaskBadgeView(initials: String, badge: TaskBadge) {
    val bg = when (badge) {
        TaskBadge.Info    -> PaquitoColors.OverlayInfoSubtle
        TaskBadge.Warning -> PaquitoColors.OverlayWarningSubtle
        TaskBadge.Neutral -> PaquitoColors.OverlayNeutralSubtle
    }
    val fg = when (badge) {
        TaskBadge.Info    -> PaquitoColors.StateInfoStrong
        TaskBadge.Warning -> PaquitoColors.StateWarning
        TaskBadge.Neutral -> PaquitoColors.StateNeutral
    }
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(bg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            style = PaquitoTypography.BadgeInitials,
            color = fg,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskInfoPreview() {
    PaquitoTheme {
        Column(modifier = Modifier.padding(PaquitoSpacing.md)) {
            TaskInfo(
                initials = "NT",
                badge = TaskBadge.Info,
                title = "Nombre :v",
                subtitle = "Curso",
                timestamp = "hace cuanto",
                onClick = {},
            )
            TaskInfo(
                initials = "FO",
                badge = TaskBadge.Warning,
                title = "Foro de Ética por vencer",
                subtitle = "Martes 20:00 · falta tu respuesta",
                timestamp = "2 d",
                onClick = {},
            )
            TaskInfo(
                initials = "EX",
                badge = TaskBadge.Neutral,
                title = "Parcial de Álgebra Lineal",
                subtitle = "Jueves 08:00 · aula B-204",
                timestamp = "4 d",
                onClick = {},
            )
        }
    }
}