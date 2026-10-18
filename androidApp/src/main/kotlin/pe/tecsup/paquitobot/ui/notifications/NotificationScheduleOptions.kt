package pe.tecsup.paquitobot.ui.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerLayoutType
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pe.tecsup.paquitobot.domain.notifications.NotificationSchedule
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography
import java.util.Locale

@Composable
fun NotificationScheduleOptions(
    schedule: NotificationSchedule,
    onChange: (NotificationSchedule) -> Unit,
    modifier: Modifier = Modifier,
) {
    var timePickerOpen by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Horario de avisos", style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.TextPrimary)
        Surface(shape = PaquitoShapes.large, color = PaquitoColors.SurfaceElevated) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Recordatorios de entregas", style = PaquitoTypography.BodyLarge.copy(fontWeight = FontWeight.SemiBold), color = PaquitoColors.TextPrimary)
                Text("Elige con cuánta anticipación quieres recibirlos.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NotificationSchedule.reminderOptions.forEach { hours ->
                        val selected = schedule.reminderHours == hours
                        FilterChip(
                            selected = selected,
                            onClick = { onChange(schedule.copy(reminderHours = hours)) },
                            label = { Text("$hours h", style = PaquitoTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold)) },
                            modifier = Modifier.heightIn(min = 48.dp),
                            shape = PaquitoShapes.medium,
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = PaquitoColors.Background,
                                labelColor = PaquitoColors.TextPrimary,
                                selectedContainerColor = PaquitoColors.BrandPrimary.copy(alpha = 0.18f),
                                selectedLabelColor = PaquitoColors.TextOnPrimaryDim,
                            ),
                            border = BorderStroke(if (selected) 2.dp else 1.dp,
                                if (selected) PaquitoColors.BrandPrimary else PaquitoColors.BorderDefault),
                        )
                    }
                }
                Text(if (schedule.reminderHours == 1) "1 hora antes del vencimiento" else "${schedule.reminderHours} horas antes del vencimiento",
                    style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
            }
        }
        Surface(shape = PaquitoShapes.large, color = PaquitoColors.SurfaceElevated) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().toggleable(
                        value = schedule.dailySummaryEnabled, role = Role.Switch,
                        onValueChange = { onChange(schedule.copy(dailySummaryEnabled = it)) },
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Resumen diario", style = PaquitoTypography.BodyLarge.copy(fontWeight = FontWeight.SemiBold), color = PaquitoColors.TextPrimary)
                        Text("Tus pendientes en un solo aviso.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                    }
                    Switch(
                        checked = schedule.dailySummaryEnabled, onCheckedChange = null,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = PaquitoColors.BrandPrimary,
                            checkedThumbColor = PaquitoColors.Background,
                            checkedBorderColor = PaquitoColors.BrandPrimary,
                            uncheckedTrackColor = PaquitoColors.Background,
                            uncheckedBorderColor = PaquitoColors.TextDisabled,
                            uncheckedThumbColor = PaquitoColors.TextSecondary,
                        ),
                    )
                }
                if (schedule.dailySummaryEnabled) {
                    Surface(
                        onClick = { timePickerOpen = true }, shape = PaquitoShapes.large,
                        color = PaquitoColors.BrandPrimary.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, PaquitoColors.BrandPrimary.copy(alpha = 0.5f)),
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("Hora del resumen", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnPrimaryDim)
                                Text(String.format(Locale.getDefault(), "%02d:%02d", schedule.summaryHour, schedule.summaryMinute),
                                    style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextOnPrimaryDim)
                            }
                            Text("Cambiar", style = PaquitoTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold), color = PaquitoColors.TextOnPrimaryDim)
                        }
                    }
                }
            }
        }
        Text("Hora local de tu dispositivo. Sin categorías activas no recibirás avisos.", style = PaquitoTypography.BodySmall, color = PaquitoColors.TextSecondary)
    }
    if (timePickerOpen) {
        NotificationTimePickerDialog(schedule.summaryHour, schedule.summaryMinute,
            onDismiss = { timePickerOpen = false },
            onConfirm = { hour, minute ->
                onChange(schedule.copy(summaryHour = hour, summaryMinute = minute))
                timePickerOpen = false
            },
        )
    }
}

// No existía un selector de hora Compose: comparte los tokens y el CTA del onboarding.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotificationTimePickerDialog(hour: Int, minute: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = true)
    var keyboardMode by rememberSaveable { mutableStateOf(false) }
    val colors = TimePickerDefaults.colors(
        clockDialColor = PaquitoColors.SurfaceElevated,
        selectorColor = PaquitoColors.BrandPrimary,
        containerColor = PaquitoColors.Background,
        clockDialSelectedContentColor = PaquitoColors.TextOnPrimaryDim,
        clockDialUnselectedContentColor = PaquitoColors.TextPrimary,
        timeSelectorSelectedContainerColor = PaquitoColors.BrandPrimary.copy(alpha = 0.18f),
        timeSelectorUnselectedContainerColor = PaquitoColors.SurfaceElevated,
        timeSelectorSelectedContentColor = PaquitoColors.TextOnPrimaryDim,
        timeSelectorUnselectedContentColor = PaquitoColors.TextPrimary,
    )
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(modifier = Modifier.padding(16.dp).widthIn(max = 400.dp).fillMaxWidth(),
            shape = PaquitoShapes.card, color = PaquitoColors.Background) {
            Column(Modifier.verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Hora del resumen", style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.TextPrimary)
                    Text("Un momento para organizar tu día.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                }
                MaterialTheme(typography = MaterialTheme.typography.copy(
                    displayLarge = PaquitoTypography.DisplayLarge.copy(fontSize = 48.sp),
                    titleMedium = PaquitoTypography.BodyLarge,
                    labelLarge = PaquitoTypography.BodyMedium,
                    labelMedium = PaquitoTypography.BodyMedium,
                )) {
                    if (keyboardMode) TimeInput(state = state, colors = colors)
                    else TimePicker(state = state, colors = colors, layoutType = TimePickerLayoutType.Vertical)
                }
                TextButton(onClick = { keyboardMode = !keyboardMode }) {
                    Text(if (keyboardMode) "Usar reloj" else "Escribir la hora", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnPrimaryDim)
                }
                PaquitoPrimaryButton(text = "Confirmar hora", onClick = { onConfirm(state.hour, state.minute) })
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", style = PaquitoTypography.BodyLarge, color = PaquitoColors.TextSecondary)
                }
            }
        }
    }
}

@Preview(showBackground = true, name = "Horario de avisos", widthDp = 360)
@Composable
private fun NotificationScheduleOptionsPreview() {
    var schedule by remember { mutableStateOf(NotificationSchedule()) }
    PaquitoTheme {
        NotificationScheduleOptions(schedule, onChange = { schedule = it }, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true, name = "Selector de hora Paquito", widthDp = 360, heightDp = 760)
@Composable
private fun NotificationTimePickerPreview() {
    PaquitoTheme {
        NotificationTimePickerDialog(8, 0, onDismiss = {}, onConfirm = { _, _ -> })
    }
}
