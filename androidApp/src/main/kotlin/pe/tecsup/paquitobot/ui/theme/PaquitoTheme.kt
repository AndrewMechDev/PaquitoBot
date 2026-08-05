package pe.tecsup.paquitobot.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Paleta de PaquitoBot.
 *
 * Hex extraídos del archivo Figma "Paquito (copia)" canvas Main (109:97)
 * sobre el frame Welcome (112:3) el 2026-08-05 con get_design_context.
 * El archivo NO tiene variables de Figma configuradas; los valores aquí son
 * los que aparecieron aplicados en Welcome como hex literal.
 *
 * Auditoría: ver requerimientos/UI_INTEGRATION.md -> "Tokens observados".
 */
object PaquitoColors {
    // Marca principal (botón "Continuar" de Welcome)
    val BrandPrimary   = Color(0xFF00C9FB) // token: brand/primary
    val TextOnPrimary  = Color(0xFFFFFFFF) // token: text/on-primary

    // Estado (placeholders hasta extraer Home B con semáforo)
    val StateSuccess   = Color(0xFF______) // token: state/success (verde) - pendiente
    val StateWarning   = Color(0xFF______) // token: state/warning (amarillo) - pendiente
    val StateDanger    = Color(0xFF______) // token: state/danger (rojo) - pendiente

    // Superficies
    val Background     = Color(0xFFFFFFFF) // token: surface/background (fondo Welcome)
    val SurfaceElevated = Color(0xFF______) // token: surface/elevated - pendiente Home B
    val SurfaceOverlay  = Color(0xFF______) // token: surface/overlay - pendiente Home B

    // Texto (Welcome)
    val TextPrimary    = Color(0xD9000000) // rgba(0,0,0,0.85) (título "PaquitoBot")
    val TextSecondary  = Color(0xB3000000) // rgba(0,0,0,0.7) (subtítulo)
    val TextDisabled   = Color(0xFF______) // token: text/disabled - pendiente

    // Bordes
    val BorderDefault  = Color(0xFF______) // token: border/default - pendiente
    val BorderSubtle   = Color(0xFF______) // token: border/subtle - pendiente
}

/**
 * Escala tipográfica de PaquitoBot.
 *
 * Tamaños ajustados a partir de Figma Welcome aplicando factor de conversión
 * px->sp (Android ≈ 0.71 sobre la medida visual de Figma).
 *
 * Familia tipográfica: Figma usa "DM Sans" (SemiBold en títulos, Regular en
 * cuerpo). Aún no se ha descargado la fuente DM Sans; mientras tanto usamos
 * FontFamily.Default (Roboto en Android), que es lo más cercano disponible sin
 * agregar assets.
 */
object PaquitoTypography {
    val DisplayLarge   = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.SemiBold, fontFamily = FontFamily.Default)
    val HeadlineSmall  = TextStyle(fontSize = 23.sp, fontWeight = FontWeight.Normal,   fontFamily = FontFamily.Default)
    val BodyLarge      = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal,   fontFamily = FontFamily.Default)
    val BodyMedium     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,   fontFamily = FontFamily.Default)
    val Caption        = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal,   fontFamily = FontFamily.Default)
    val LabelSmall     = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium,   fontFamily = FontFamily.Default)
}

/**
 * Escala de espaciados (multiplicador base 4).
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
 * Radios de borde (Welcome usa 20dp para el botón "Continuar" y el ícono del bot).
 */
object PaquitoShapes {
    val small  = RoundedCornerShape(8.dp)
    val medium = RoundedCornerShape(12.dp)
    val large  = RoundedCornerShape(20.dp)
    val pill   = RoundedCornerShape(50)
}

/**
 * Theme raíz. Conecta tokens con MaterialTheme.
 * Aplicar en MainActivity y en Preview.
 */
@Composable
fun PaquitoTheme(content: @Composable () -> Unit) {
    val colorScheme = lightColorScheme(
        primary       = PaquitoColors.BrandPrimary,
        onPrimary     = PaquitoColors.TextOnPrimary,
        background    = PaquitoColors.Background,
        onBackground  = PaquitoColors.TextPrimary,
        surface       = PaquitoColors.Background,
        onSurface     = PaquitoColors.TextPrimary,
    )

    val typography = Typography(
        displayLarge  = PaquitoTypography.DisplayLarge,
        headlineSmall = PaquitoTypography.HeadlineSmall,
        bodyLarge     = PaquitoTypography.BodyLarge,
        bodyMedium    = PaquitoTypography.BodyMedium,
        bodySmall     = PaquitoTypography.Caption,
        labelSmall    = PaquitoTypography.LabelSmall,
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