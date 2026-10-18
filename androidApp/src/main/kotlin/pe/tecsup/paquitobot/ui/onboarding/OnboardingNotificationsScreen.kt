package pe.tecsup.paquitobot.ui.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.LinearProgressIndicator
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
import pe.tecsup.paquitobot.ui.notifications.NotificationScheduleSection
import pe.tecsup.paquitobot.ui.components.NotificationCard
import pe.tecsup.paquitobot.ui.components.NotificationCardVariant
import pe.tecsup.paquitobot.ui.components.NotificationType
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

enum class NotificationSetupStep { Categories, Timing, Summary }

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
    initialStep: NotificationSetupStep = NotificationSetupStep.Categories,
) {
    var step by rememberSaveable { mutableStateOf(initialStep) }
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

    BackHandler(enabled = onCancel == null && step != NotificationSetupStep.Categories) {
        step = NotificationSetupStep.entries[step.ordinal - 1]
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background)
            .systemBarsPadding()
            .padding(horizontal = PaquitoSpacing.lg)
            .padding(top = 32.dp, bottom = PaquitoSpacing.lg),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = "Notificaciones",
                style = PaquitoTypography.DisplayLarge,
                color = PaquitoColors.TextPrimary,
            )
            Text(
                text = "Paso ${step.ordinal + 1} de 3",
                style = PaquitoTypography.BodyMedium,
                color = PaquitoColors.TextSecondary,
            )
            LinearProgressIndicator(
                progress = { (step.ordinal + 1) / 3f },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(50)),
                color = PaquitoColors.BrandPrimary,
                trackColor = PaquitoColors.SurfaceElevated,
            )
        }

        AnimatedContent(
            targetState = step,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(top = 24.dp),
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                (slideInHorizontally(tween(250)) { it / 6 * direction } + fadeIn(tween(200))) togetherWith
                    (slideOutHorizontally(tween(250)) { -it / 6 * direction } + fadeOut(tween(150)))
            },
            label = "Pasos de preferencias",
        ) { visibleStep ->
            Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = when (visibleStep) {
                        NotificationSetupStep.Categories -> "¿Qué avisos quieres recibir?"
                        NotificationSetupStep.Timing -> "¿Cuándo te recordamos?"
                        NotificationSetupStep.Summary -> "Organiza tu resumen diario"
                    },
                    style = PaquitoTypography.HeadlineSmall,
                    color = PaquitoColors.TextPrimary,
                )
                when (visibleStep) {
                    NotificationSetupStep.Categories -> {
                        Text("Elige una o varias categorías. Puedes cambiarlas después.",
                            style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                        VariantToggle(variant = variant, onVariantChange = { variant = it })
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
                    }
                    NotificationSetupStep.Timing -> {
                        Text("Define la anticipación de tus entregas.",
                            style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                        NotificationScheduleOptions(schedule = schedule, onChange = { schedule = it },
                            section = NotificationScheduleSection.Reminders)
                    }
                    NotificationSetupStep.Summary -> {
                        NotificationScheduleOptions(schedule = schedule, onChange = { schedule = it },
                            section = NotificationScheduleSection.Summary)
                        permissionContent()
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (step != NotificationSetupStep.Categories) {
                TextButton(onClick = { step = NotificationSetupStep.entries[step.ordinal - 1] }) {
                    Text("Atrás", style = PaquitoTypography.BodyLarge, color = PaquitoColors.TextPrimary)
                }
            }
            PaquitoPrimaryButton(
                text = if (step != NotificationSetupStep.Summary) "Continuar"
                    else if (onCancel == null) "Finalizar" else "Guardar cambios",
                onClick = {
                    if (step == NotificationSetupStep.Summary) onContinue(enabled, schedule)
                    else step = NotificationSetupStep.entries[step.ordinal + 1]
                },
                modifier = Modifier.weight(1f),
            )
        }
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

@Preview(showBackground = true, name = "Paso 2 - Anticipación", widthDp = 360, heightDp = 760)
@Composable
private fun NotificationTimingStepPreview() {
    PaquitoTheme {
        OnboardingNotificationsScreen(onCancel = {}, initialStep = NotificationSetupStep.Timing)
    }
}

@Preview(showBackground = true, name = "Paso 3 - Resumen diario", widthDp = 360, heightDp = 760)
@Composable
private fun NotificationSummaryStepPreview() {
    PaquitoTheme {
        OnboardingNotificationsScreen(onCancel = {}, initialStep = NotificationSetupStep.Summary)
    }
}
