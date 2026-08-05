package dev.paquitobot.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Paleta de colores centralizada para PaquitoBot.
 *
 * Los hex se extraen de la página "Main" del archivo Figma "Paquito (copia)"
 * (fileKey=Piy1K37xHS9jB1qXaaVtuQ, nodeId=109:97) usando get_variable_defs.
 *
 * PENDIENTE: reemplazar los marcadores 0xFF______ por los valores reales
 * una vez que se extraigan las variables con selección activa en Figma.
 */
object PaquitoColors {
    // Marca principal
    val BrandPrimary       = Color(0xFF______) // token: brand/primary
    val BrandSecondary     = Color(0xFF______) // token: brand/secondary
    val BrandAccent        = Color(0xFF______) // token: brand/accent

    // Estados semánticos (semáforo de aprobación)
    val StateSuccess       = Color(0xFF______) // token: state/success (verde)
    val StateWarning       = Color(0xFF______) // token: state/warning (amarillo)
    val StateDanger        = Color(0xFF______) // token: state/danger (rojo)

    // Superficies
    val Background         = Color(0xFF______) // token: surface/background
    val SurfaceElevated    = Color(0xFF______) // token: surface/elevated
    val SurfaceOverlay     = Color(0xFF______) // token: surface/overlay

    // Texto
    val TextPrimary        = Color(0xFF______) // token: text/primary
    val TextSecondary      = Color(0xFF______) // token: text/secondary
    val TextDisabled       = Color(0xFF______) // token: text/disabled
    val TextOnPrimary      = Color(0xFF______) // token: text/on-primary

    // Bordes
    val BorderDefault      = Color(0xFF______) // token: border/default
    val BorderSubtle       = Color(0xFF______) // token: border/subtle
}

/**
 * Escala tipográfica. Los pesos y tamaños exactos vienen de la extracción de
 * variables en Figma; mientras no estén, mantenemos los valores por defecto de
 * Material 3 para no bloquear desarrollo.
 */
object PaquitoTypography {
    val DisplayLarge   = TextStyle(fontSize = 57.sp, fontWeight = FontWeight.Bold,    fontFamily = FontFamily.Default)
    val HeadlineMedium = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Default)
    val TitleLarge     = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Default)
    val TitleMedium    = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium,   fontFamily = FontFamily.Default)
    val BodyLarge      = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Normal,   fontFamily = FontFamily.Default)
    val BodyMedium     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,   fontFamily = FontFamily.Default)
    val Caption        = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal,   fontFamily = FontFamily.Default)
    val LabelSmall     = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium,   fontFamily = FontFamily.Default)
}

/**
 * Escala de espaciados. Centralizada para no repetir literales dp en cada
 * pantalla. Multiplicador base 4.
 */
object PaquitoSpacing {
    val xs  = 4.dp
    val sm  = 8.dp
    val md  = 16.dp
    val lg  = 24.dp
    val xl  = 32.dp
    val xxl = 48.dp
}

/**
 * Radios de borde.
 */
object PaquitoShapes {
    val small  = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large  = RoundedCornerShape(20.dp)
    val pill   = RoundedCornerShape(50)
}

/**
 * Theme raíz. Aplicar en MainActivity y en Preview.
 *
 * Conecta los tokens anteriores con MaterialTheme; cualquier pantalla que use
 * MaterialTheme.colorScheme / typography hereda automáticamente PaquitoColors y
 * PaquitoTypography.
 */
@Composable
fun PaquitoTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary       = PaquitoColors.BrandPrimary,
        onPrimary     = PaquitoColors.TextOnPrimary,
        secondary     = PaquitoColors.BrandSecondary,
        background    = PaquitoColors.Background,
        surface       = PaquitoColors.SurfaceElevated,
        error         = PaquitoColors.StateDanger,
        onBackground  = PaquitoColors.TextPrimary,
        onSurface     = PaquitoColors.TextPrimary,
    )

    val typography = Typography(
        displayLarge   = PaquitoTypography.DisplayLarge,
        headlineMedium = PaquitoTypography.HeadlineMedium,
        titleLarge     = PaquitoTypography.TitleLarge,
        titleMedium    = PaquitoTypography.TitleMedium,
        bodyLarge      = PaquitoTypography.BodyLarge,
        bodyMedium     = PaquitoTypography.BodyMedium,
        bodySmall      = PaquitoTypography.Caption,
        labelSmall     = PaquitoTypography.LabelSmall,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = Shapes(
            small  = PaquitoShapes.small,
            medium = PaquitoShapes.medium,
            large  = PaquitoShapes.large,
        ),
        content = content,
    )
}
