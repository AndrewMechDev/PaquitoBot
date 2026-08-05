package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Estado visual del día en el calendario semanal.
 *
 * El "dot" inferior codifica la urgencia:
 * - [Neutral] → gris suave (sin tareas)
 * - [WithTask] → azul (tiene tarea)
 * - [Critical] → rojo (entrega crítica)
 *
 * El parámetro [selected] controla el día actual: sobrescribe el fondo con
 * blanco puro y un borde azul más fuerte.
 *
 * Extraído del Figma nodeId 364:285-364:327 (semáforo de días en Home B).
 */
enum class DayState { Neutral, WithTask, Critical }

@Composable
fun Day(
    dayOfWeek: String,
    dayNumber: String,
    state: DayState,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) PaquitoColors.Background else PaquitoColors.SurfaceGlassSoft
    val borderColor = if (selected) PaquitoColors.BorderInfo else PaquitoColors.BorderSubtle
    val dotColor = when (state) {
        DayState.Neutral  -> Color(0x1F0D1520) // rgba(13,21,32,0.12)
        DayState.WithTask -> PaquitoColors.StateInfo
        DayState.Critical -> PaquitoColors.StateDanger
    }
    val dayLabelColor = if (selected) PaquitoColors.TextDayActive else PaquitoColors.TextDayInactive
    val numberColor   = if (selected) PaquitoColors.TextDayNumber else PaquitoColors.TextDayNumberInactive

    Column(
        modifier = modifier
            .width(41.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 7.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = dayOfWeek,
            style = PaquitoTypography.DayOfWeek,
            color = dayLabelColor,
        )
        Text(
            text = dayNumber,
            style = PaquitoTypography.DayNumber,
            color = numberColor,
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DayPreviewRow() {
    PaquitoTheme {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaquitoSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm),
        ) {
            Day(dayOfWeek = "L", dayNumber = "3", state = DayState.WithTask, selected = true,  onClick = {})
            Day(dayOfWeek = "M", dayNumber = "4", state = DayState.WithTask, selected = false, onClick = {})
            Day(dayOfWeek = "M", dayNumber = "5", state = DayState.Neutral,  selected = false, onClick = {})
            Day(dayOfWeek = "J", dayNumber = "6", state = DayState.Critical, selected = false, onClick = {})
            Day(dayOfWeek = "V", dayNumber = "7", state = DayState.WithTask, selected = false, onClick = {})
        }
    }
}