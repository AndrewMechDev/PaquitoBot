package pe.tecsup.paquitobot.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.components.NotificationCard
import pe.tecsup.paquitobot.ui.components.NotificationCardVariant
import pe.tecsup.paquitobot.ui.components.NotificationType
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Screen de Onboarding: eleccion de notificaciones a activar.
 *
 * Compone los dos frames alternativos del Figma canvas Main:
 *   - 112:135 (variant Compact): grid 2 columnas x 2 filas con la tercera
 *     card sola al final (Figma usa flex-wrap, no es grid estricto).
 *   - 156:124 (variant Full): lista vertical de 3 cards full-width.
 *
 * Variante default: Compact. El usuario puede alternar via el segmented
 * control en la parte superior.
 *
 * Tokens claves:
 *   - fondo card inactiva = SurfaceOverlay = rgba(0,0,0,0.02)
 *   - fondo card activa    = BrandPrimary @ 10% alpha
 *   - texto titulo         = TextPrimary @ 75% alpha
 *   - boton Continuar      = BrandPrimary bg + TextOnPrimary (igual Welcome)
 */
@Composable
fun OnboardingNotificationsScreen(
    initialEnabled: Set<NotificationType> = setOf(
        NotificationType.Laboratorios,
        NotificationType.Plazos,
    ),
    onContinue: (Set<NotificationType>) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var variant by remember { mutableStateOf(NotificationCardVariant.Compact) }
    var enabled by remember { mutableStateOf(initialEnabled) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = PaquitoSpacing.lg,
                    end = PaquitoSpacing.lg,
                    top = 350.dp,
                    bottom = PaquitoSpacing.xxl,
                ),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // Titulo y subtitulo.
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Notificaciones",
                    style = PaquitoTypography.DisplayLarge,
                    color = PaquitoColors.TextPrimary,
                )
                Text(
                    text = "Activa tus avisos",
                    style = PaquitoTypography.HeadlineSmall,
                    color = PaquitoColors.TextSecondary,
                )
            }

            // Segmented control: Compact vs Full.
            VariantToggle(variant = variant, onVariantChange = { variant = it })

            // Contenedor de cards segun variante.
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

            // Boton Continuar.
            Button(
                onClick = { onContinue(enabled) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(51.dp),
                shape = PaquitoShapes.large,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaquitoColors.BrandPrimary,
                    contentColor = PaquitoColors.TextOnPrimary,
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(15.dp),
            ) {
                Text(
                    text = "Continuar",
                    style = PaquitoTypography.BodyLarge,
                    color = PaquitoColors.TextOnPrimary,
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
            .background(PaquitoColors.SurfaceOverlay)
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
            .background(if (selected) PaquitoColors.Background else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = PaquitoTypography.BodyMedium.copy(
                color = if (selected) PaquitoColors.TextPrimary else PaquitoColors.TextSecondary,
            ),
        )
    }
}

@Composable
private fun CompactGrid(
    enabled: Set<NotificationType>,
    onToggle: (NotificationType) -> Unit,
) {
    // Figma 112:135: dos cards en la primera fila, tercera sola en la segunda.
    // Layout manual: 2 Rows con gap 20dp entre cards y filas.
    val allTypes = NotificationType.values().toList()
    val firstRow = allTypes.take(2)
    val secondRow = allTypes.drop(2)
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            firstRow.forEach { type ->
                NotificationCard(
                    type = type,
                    variant = NotificationCardVariant.Compact,
                    enabled = type in enabled,
                    onToggle = { onToggle(type) },
                )
            }
        }
        if (secondRow.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                secondRow.forEach { type ->
                    NotificationCard(
                        type = type,
                        variant = NotificationCardVariant.Compact,
                        enabled = type in enabled,
                        onToggle = { onToggle(type) },
                    )
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
        NotificationType.values().forEach { type ->
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

@Preview(showBackground = true)
@Composable
private fun OnboardingNotificationsScreenPreview() {
    PaquitoTheme {
        OnboardingNotificationsScreen()
    }
}