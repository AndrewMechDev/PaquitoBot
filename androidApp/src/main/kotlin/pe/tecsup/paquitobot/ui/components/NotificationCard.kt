package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Tipo de notificación a activar en el onboarding.
 *
 * Cada tipo tiene su icono y titulo asociados.
 */
enum class NotificationType(val title: String, val iconRes: Int) {
    Laboratorios("Laboratorios Calificados", R.drawable.ic_paquito_lab_profile),
    Plazos("Plazos de Entrega", R.drawable.ic_paquito_calendar_fill),
    Asistencias("Asistencias", R.drawable.ic_paquito_frame_person),
}

/**
 * Variante visual de la card de notificacion.
 *
 * - [Compact] (Figma 112:135): 180x140dp, dos lineas de titulo, icono en
 *   esquina superior derecha. Pensada para un grid 2 columnas.
 * - [Full] (Figma 156:124): 380x62dp, titulo en una linea + icono a la
 *   derecha. Pensada para lista vertical.
 */
enum class NotificationCardVariant { Compact, Full }

/**
 * Card de activacion de notificacion.
 *
 * Extraida de los frames 112:135 (variant Compact) y 156:124 (variant Full)
 * del Figma canvas Main. Token comun: fondo surface/overlay
 * rgba(0,0,0,0.02) y texto color rgba(0,0,0,0.75).
 *
 * Estado visual:
 * - [enabled] = true: fondo con brand/primary en alpha bajo (pendiente de
 *   definir cuando se confirme paleta del estado seleccionado).
 * - [enabled] = false: fondo surface/overlay, sin borde.
 */
@Composable
fun NotificationCard(
    type: NotificationType,
    variant: NotificationCardVariant,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (variant) {
        NotificationCardVariant.Compact -> NotificationCardCompact(type, enabled, onToggle, modifier)
        NotificationCardVariant.Full    -> NotificationCardFull(type, enabled, onToggle, modifier)
    }
}

@Composable
private fun NotificationCardCompact(
    type: NotificationType,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier,
) {
    val bg = if (enabled) {
        PaquitoColors.BrandPrimary.copy(alpha = 0.18f)
    } else {
        PaquitoColors.SurfaceElevated
    }
    val borderColor = if (enabled) PaquitoColors.BrandPrimary else Color.Transparent
    Column(
        modifier = modifier
            .fillMaxWidth()
            // 2026-08-13 (bug real, reportado por el usuario con captura):
            // `.height(140.dp)` fijo recortaba la 2da linea de titulos
            // largos ("Laboratorios Calificados", "Plazos de Entrega") -
            // icono + gap + 2 lineas de texto a 22sp no entraban en 140dp.
            // `heightIn(min = ...)` respeta el alto de Figma para titulos
            // cortos mientras crece si el contenido real lo necesita, en
            // vez de recortarlo.
            .heightIn(min = 140.dp)
            .clip(PaquitoShapes.large)
            .background(bg)
            .border(
                width = if (enabled) 2.dp else 0.dp,
                color = borderColor,
                shape = PaquitoShapes.large,
            )
            .clickable(onClick = onToggle)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.Bottom),
        horizontalAlignment = Alignment.Start,
    ) {
        Image(
            painter = painterResource(id = type.iconRes),
            contentDescription = type.title,
            modifier = Modifier.size(48.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = type.title,
            fontSize = 22.sp,
            fontFamily = PaquitoFont.DMSans,
            fontWeight = FontWeight.SemiBold,
            color = PaquitoColors.TextPrimary.copy(alpha = 0.75f),
            maxLines = 2,
        )
    }
}

@Composable
private fun NotificationCardFull(
    type: NotificationType,
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier,
) {
    val bg = if (enabled) {
        PaquitoColors.BrandPrimary.copy(alpha = 0.18f)
    } else {
        PaquitoColors.SurfaceElevated
    }
    val borderColor = if (enabled) PaquitoColors.BrandPrimary else Color.Transparent
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(bg)
            .border(
                width = if (enabled) 2.dp else 0.dp,
                color = borderColor,
                shape = RoundedCornerShape(25.dp),
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = type.title,
            fontSize = 22.sp,
            fontFamily = PaquitoFont.DMSans,
            fontWeight = FontWeight.SemiBold,
            color = PaquitoColors.TextPrimary.copy(alpha = 0.75f),
        )
        Image(
            painter = painterResource(id = type.iconRes),
            contentDescription = type.title,
            modifier = Modifier.size(32.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationCardCompactPreview() {
    PaquitoTheme {
        Column(
            modifier = Modifier.padding(PaquitoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm),
        ) {
            NotificationCard(NotificationType.Laboratorios, NotificationCardVariant.Compact, enabled = true, onToggle = {})
            NotificationCard(NotificationType.Plazos, NotificationCardVariant.Compact, enabled = false, onToggle = {})
            NotificationCard(NotificationType.Asistencias, NotificationCardVariant.Compact, enabled = true, onToggle = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationCardFullPreview() {
    PaquitoTheme {
        Column(
            modifier = Modifier.padding(PaquitoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm),
        ) {
            NotificationCard(NotificationType.Laboratorios, NotificationCardVariant.Full, enabled = true, onToggle = {})
            NotificationCard(NotificationType.Plazos, NotificationCardVariant.Full, enabled = false, onToggle = {})
            NotificationCard(NotificationType.Asistencias, NotificationCardVariant.Full, enabled = true, onToggle = {})
        }
    }
}