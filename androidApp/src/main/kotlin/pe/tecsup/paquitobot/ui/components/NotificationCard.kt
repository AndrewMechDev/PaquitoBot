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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.domain.notifications.NotificationCategory
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
enum class NotificationType(val title: String, val iconRes: Int, val category: NotificationCategory) {
    Laboratorios("Laboratorios Calificados", R.drawable.ic_paquito_lab_profile, NotificationCategory.Laboratories),
    Plazos("Plazos de Entrega", R.drawable.ic_paquito_calendar_fill, NotificationCategory.Deadlines),
    Asistencias("Asistencias", R.drawable.ic_paquito_frame_person, NotificationCategory.Attendance),
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
 * Conserva las variantes Compact y Full del onboarding y comparte la
 * superficie clara de Inicio. El estado activo usa un tinte azul sutil.
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
    val bg = if (enabled) PaquitoColors.OverlayInfoSubtle else PaquitoColors.SurfaceGlassSoft
    val borderColor = if (enabled) PaquitoColors.BorderInfo else PaquitoColors.BorderDefault
    Column(
        modifier = modifier
            .fillMaxWidth()
            // 2026-08-13 (bug real, reportado por el usuario con captura):
            // `.height(140.dp)` fijo recortaba la 2da linea de titulos
            // largos ("Laboratorios Calificados", "Plazos de Entrega") -
            // icono + gap + 2 lineas de texto no entraban en 140dp.
            // `heightIn(min = ...)` respeta el alto de Figma para titulos
            // cortos mientras crece si el contenido real lo necesita, en
            // vez de recortarlo.
            .heightIn(min = 140.dp)
            .clip(PaquitoShapes.large)
            .background(bg)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = PaquitoShapes.large,
            )
            .clickable(onClick = onToggle)
            .padding(16.dp),
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
            fontSize = 16.sp,
            fontFamily = PaquitoFont.DMSans,
            fontWeight = FontWeight.SemiBold,
            color = PaquitoColors.TextOnCardStrong,
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
    val bg = if (enabled) PaquitoColors.OverlayInfoSubtle else PaquitoColors.SurfaceGlassSoft
    val borderColor = if (enabled) PaquitoColors.BorderInfo else PaquitoColors.BorderDefault
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .clip(PaquitoShapes.large)
            .background(bg)
            .border(
                width = 1.dp,
                color = borderColor,
                shape = PaquitoShapes.large,
            )
            .clickable(onClick = onToggle)
            .padding(horizontal = 15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = type.title,
            fontSize = 16.sp,
            fontFamily = PaquitoFont.DMSans,
            fontWeight = FontWeight.SemiBold,
            color = PaquitoColors.TextOnCardStrong,
            modifier = Modifier.weight(1f),
            maxLines = 2,
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
