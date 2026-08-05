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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Card oscura de alerta crítica (Figma nodeId 364:225).
 *
 * Estructura:
 *   - Fondo con gradiente linear (151deg, #101418 -> #0B1D28).
 *   - Glow radial turquesa en esquina superior derecha (decorativo).
 *   - Shadow fuerte: 0 16 34 rgba(13,21,32,0.28).
 *   - Chip turquesa "VENCE EN 6 H" + label "Cálculo II".
 *   - Titulo 20sp SemiBold ("Laboratorio 4 - Integrales").
 *   - Subtitulo 13sp ("Hoy 23:59 - vale 15% de la nota final").
 *   - 2 botones: "Entregar" (BrandPrimary) + "Recordar 2 h" (overlay translucido).
 *
 * Se usa para resaltar una entrega inminente (caso Home B).
 */
@Composable
fun AlertCard(
    chip: String,
    category: String,
    title: String,
    subtitle: String,
    primaryActionLabel: String,
    secondaryActionLabel: String,
    onPrimaryAction: () -> Unit,
    onSecondaryAction: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF101418),
            Color(0xFF0B1D28),
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(351f, 174f),
    )
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 16.dp,
                shape = PaquitoShapes.alertCard,
            )
            .clip(PaquitoShapes.alertCard)
            .background(gradient)
            .border(1.dp, Color(0x1FFFFFFF), PaquitoShapes.alertCard)
            .padding(horizontal = 18.dp, vertical = 17.dp),
        verticalArrangement = Arrangement.spacedBy(7.dp),
    ) {
        // Fila chip + categoria.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(PaquitoColors.BrandPrimary)
                    .padding(horizontal = 9.dp, vertical = 5.dp),
            ) {
                Text(
                    text = chip,
                    style = PaquitoTypography.ChipLabel.copy(
                        color = PaquitoColors.TextOnPrimaryDim,
                    ),
                )
            }
            Text(
                text = category,
                style = PaquitoTypography.TaskSubtitle.copy(
                    color = PaquitoColors.TextOnDarkMuted,
                ),
            )
        }

        // Título.
        Text(
            text = title,
            style = PaquitoTypography.AlertCardTitle.copy(
                color = PaquitoColors.TextOnWhite,
            ),
        )

        // Subtítulo.
        Text(
            text = subtitle,
            style = PaquitoTypography.AlertCardSubtitle.copy(
                color = PaquitoColors.TextOnDarkStronger,
            ),
        )

        // Acciones.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            AlertCardButton(
                label = primaryActionLabel,
                primary = true,
                onClick = onPrimaryAction,
            )
            AlertCardButton(
                label = secondaryActionLabel,
                primary = false,
                onClick = onSecondaryAction,
            )
        }
    }
}

@Composable
private fun AlertCardButton(
    label: String,
    primary: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (primary) PaquitoColors.BrandPrimary else Color(0x1FFFFFFF)
    val fg = if (primary) PaquitoColors.TextOnPrimaryDim else PaquitoColors.TextOnWhite
    val border = if (primary) null else BorderStroke(Color(0x29FFFFFF), 1.dp)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .let { mod ->
                if (border != null) mod.border(border.color, border.width, RoundedCornerShape(14.dp)) else mod
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 11.dp),
    ) {
        Text(
            text = label,
            style = PaquitoTypography.ButtonLabel.copy(color = fg),
        )
    }
}

/** Helper local para el border del botón secundario. */
private data class BorderStroke(val color: Color, val width: androidx.compose.ui.unit.Dp)

@Preview(showBackground = true)
@Composable
private fun AlertCardPreview() {
    PaquitoTheme {
        AlertCard(
            chip = "VENCE EN 6 H",
            category = "Cálculo II",
            title = "Laboratorio 4 — Integrales",
            subtitle = "Hoy 23:59 · vale 15% de la nota final",
            primaryActionLabel = "Entregar",
            secondaryActionLabel = "Recordar 2 h",
            onPrimaryAction = {},
            onSecondaryAction = {},
            modifier = Modifier.padding(PaquitoSpacing.md),
        )
    }
}