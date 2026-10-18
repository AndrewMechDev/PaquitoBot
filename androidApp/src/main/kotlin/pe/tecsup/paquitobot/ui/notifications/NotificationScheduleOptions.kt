package pe.tecsup.paquitobot.ui.notifications

import android.app.TimePickerDialog
import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.domain.notifications.NotificationSchedule
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import java.util.Locale

@Composable
fun NotificationScheduleOptions(
    schedule: NotificationSchedule,
    onChange: (NotificationSchedule) -> Unit,
    modifier: Modifier = Modifier,
) {
    var reminderMenuOpen by remember { mutableStateOf(false) }
    val context = LocalContext.current
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Horario de avisos", style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.TextPrimary)
        Text("Recordar entregas antes de su vencimiento", style = PaquitoTypography.BodyMedium,
            color = PaquitoColors.TextSecondary)
        Box {
            OutlinedButton(onClick = { reminderMenuOpen = true }) {
                Text(if (schedule.reminderHours == 1) "1 hora antes" else "${schedule.reminderHours} horas antes",
                    style = PaquitoTypography.BodyMedium)
            }
            DropdownMenu(expanded = reminderMenuOpen, onDismissRequest = { reminderMenuOpen = false }) {
                NotificationSchedule.reminderOptions.forEach { hours ->
                    DropdownMenuItem(
                        text = { Text(if (hours == 1) "1 hora antes" else "$hours horas antes",
                            style = PaquitoTypography.BodyMedium) },
                        onClick = {
                            onChange(schedule.copy(reminderHours = hours))
                            reminderMenuOpen = false
                        },
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().toggleable(
                value = schedule.dailySummaryEnabled,
                role = Role.Switch,
                onValueChange = { onChange(schedule.copy(dailySummaryEnabled = it)) },
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Resumen diario", modifier = Modifier.weight(1f), style = PaquitoTypography.BodyLarge,
                color = PaquitoColors.TextPrimary)
            Switch(checked = schedule.dailySummaryEnabled, onCheckedChange = null)
        }
        if (schedule.dailySummaryEnabled) {
            OutlinedButton(onClick = {
                TimePickerDialog(
                    context,
                    { _, hour, minute -> onChange(schedule.copy(summaryHour = hour, summaryMinute = minute)) },
                    schedule.summaryHour,
                    schedule.summaryMinute,
                    DateFormat.is24HourFormat(context),
                ).show()
            }) {
                Text(
                    text = "Hora: " + String.format(Locale.getDefault(), "%02d:%02d", schedule.summaryHour, schedule.summaryMinute),
                    style = PaquitoTypography.BodyMedium,
                )
            }
        }
        Text("Los horarios siguen la hora local de tu dispositivo. Desactivar todas las categorías desactiva los avisos.",
            style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
    }
}

@Preview(showBackground = true, name = "Horario de avisos")
@Composable
private fun NotificationScheduleOptionsPreview() {
    var schedule by remember { mutableStateOf(NotificationSchedule()) }
    PaquitoTheme {
        NotificationScheduleOptions(schedule = schedule, onChange = { schedule = it })
    }
}
