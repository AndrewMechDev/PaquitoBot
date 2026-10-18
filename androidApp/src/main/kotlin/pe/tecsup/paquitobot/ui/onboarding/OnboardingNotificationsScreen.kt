package pe.tecsup.paquitobot.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.domain.notifications.NotificationCategory
import pe.tecsup.paquitobot.domain.notifications.NotificationSchedule
import pe.tecsup.paquitobot.ui.notifications.NotificationScheduleOptions
import pe.tecsup.paquitobot.ui.components.NotificationCard
import pe.tecsup.paquitobot.ui.components.NotificationCardVariant
import pe.tecsup.paquitobot.ui.components.NotificationType
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Eleccion de avisos (Figma `112:135` Compact / `156:124` Full).
 *
 * El padding top de 350dp copiaba la coordenada absoluta de Figma y empujaba
 * titulo, cards y Continuar fuera de pantalla. Ahora el contenido arranca
 * bajo la status bar y Continuar queda anclado abajo (edge-to-edge).
 */
@Composable
fun OnboardingNotificationsScreen(
    initialEnabled: Set<NotificationType> = NotificationType.entries.filterTo(mutableSetOf()) {
        it.category in NotificationCategory.defaults
    },
    onContinue: (Set<NotificationType>, NotificationSchedule) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    onCancel: (() -> Unit)? = null,
    initialSchedule: NotificationSchedule = NotificationSchedule(),
    permissionContent: @Composable () -> Unit = {},
) {
    var variant by rememberSaveable { mutableStateOf(NotificationCardVariant.Compact) }
    var enabled by rememberSaveable(
        stateSaver = Saver<Set<NotificationType>, ArrayList<String>>(
            save = { selected -> ArrayList(selected.map { it.category.storageKey }) },
            restore = { saved -> NotificationType.entries.filterTo(mutableSetOf()) { it.category.storageKey in saved } },
        ),
    ) { mutableStateOf(initialEnabled.toSet()) }
    var schedule by rememberSaveable(
        stateSaver = Saver<NotificationSchedule, ArrayList<Int>>(
            save = { ArrayList(listOf(it.reminderHours, if (it.dailySummaryEnabled) 1 else 0, it.summaryHour, it.summaryMinute)) },
            restore = { NotificationSchedule(it[0], it[1] != 0, it[2], it[3]) },
        ),
    ) { mutableStateOf(initialSchedule) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background)
            .systemBarsPadding()
            .padding(horizontal = PaquitoSpacing.lg)
            .padding(top = 32.dp, bottom = PaquitoSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = "Notificaciones",
                style = PaquitoTypography.DisplayLarge,
                color = PaquitoColors.TextPrimary,
            )
            Text(
                text = if (onCancel == null) "Activa tus avisos" else "Elige qué avisos quieres recibir",
                style = PaquitoTypography.HeadlineSmall,
                color = PaquitoColors.TextSecondary,
            )
        }

        Spacer(modifier = Modifier.padding(top = 20.dp))

        VariantToggle(variant = variant, onVariantChange = { variant = it })

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            when (variant) {
                NotificationCardVariant.Compact -> CompactGrid(
                    enabled = enabled,
                    onToggle = { type -> toggleNotification(enabled, type) { enabled = it } },
                )
                NotificationCardVariant.Full -> FullList(
                    enabled = enabled,
                    onToggle = { type -> toggleNotification(enabled, type) { enabled = it } },
                )
            }
            NotificationScheduleOptions(schedule = schedule, onChange = { schedule = it })
            permissionContent()
        }

        PaquitoPrimaryButton(
            text = if (onCancel == null) "Continuar" else "Guardar cambios",
            onClick = { onContinue(enabled, schedule) },
            modifier = Modifier.padding(top = 16.dp),
        )
        if (onCancel != null) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "Cancelar",
                    style = PaquitoTypography.BodyLarge,
                    color = PaquitoColors.TextPrimary,
                )
            }
        }
    }
}

@Composable
private fun VariantToggle(
    variant: NotificationCardVariant,
    onVariantChange: (NotificationCardVariant) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .background(PaquitoColors.SurfaceElevated)
            .padding(4.dp),
    ) {
        ToggleSegment(
            label = "Cuadros",
            selected = variant == NotificationCardVariant.Compact,
            onClick = { onVariantChange(NotificationCardVariant.Compact) },
            modifier = Modifier.weight(1f),
        )
        ToggleSegment(
            label = "Lista",
            selected = variant == NotificationCardVariant.Full,
            onClick = { onVariantChange(NotificationCardVariant.Full) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ToggleSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) PaquitoColors.BrandPrimary else PaquitoColors.SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = PaquitoTypography.BodyMedium,
            color = if (selected) PaquitoColors.TextOnPrimary else PaquitoColors.TextSecondary,
        )
    }
}

@Composable
private fun CompactGrid(
    enabled: Set<NotificationType>,
    onToggle: (NotificationType) -> Unit,
) {
    val allTypes = NotificationType.entries
    val firstRow = allTypes.take(2)
    val secondRow = allTypes.drop(2)
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            firstRow.forEach { type ->
                NotificationCard(
                    type = type,
                    variant = NotificationCardVariant.Compact,
                    enabled = type in enabled,
                    onToggle = { onToggle(type) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        if (secondRow.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                secondRow.forEach { type ->
                    NotificationCard(
                        type = type,
                        variant = NotificationCardVariant.Compact,
                        enabled = type in enabled,
                        onToggle = { onToggle(type) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (secondRow.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FullList(
    enabled: Set<NotificationType>,
    onToggle: (NotificationType) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        NotificationType.entries.forEach { type ->
            NotificationCard(
                type = type,
                variant = NotificationCardVariant.Full,
                enabled = type in enabled,
                onToggle = { onToggle(type) },
            )
        }
    }
}

private fun toggleNotification(
    current: Set<NotificationType>,
    type: NotificationType,
    setter: (Set<NotificationType>) -> Unit,
) {
    setter(if (type in current) current - type else current + type)
}

@Preview(showBackground = true, name = "2. Notificaciones")
@Composable
private fun OnboardingNotificationsScreenPreview() {
    PaquitoTheme {
        OnboardingNotificationsScreen()
    }
}

// Reutiliza el selector del onboarding: cambia las acciones, no duplica tarjetas ni estado.
@Preview(showBackground = true, name = "Preferencias de notificaciones")
@Composable
private fun NotificationPreferencesPreview() {
    PaquitoTheme {
        OnboardingNotificationsScreen(onCancel = {})
    }
}
