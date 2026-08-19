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
 * con get_design_context sobre Welcome (112:3) y Home B (364:219) en
 * 2026-08-05. El archivo NO tiene variables de Figma configuradas; los
 * valores aquí son los que aparecieron aplicados como hex literal en los
 * frames seleccionados.
 *
 * Auditoría: ver requerimientos/UI_INTEGRATION.md -> "Tokens observados".
 */
object PaquitoColors {
    // Marca principal
    val BrandPrimary   = Color(0xFF00C9FB) // token: brand/primary (Welcome + Navbar + Home B CTA)
    val TextOnPrimary  = Color(0xFFFFFFFF) // token: text/on-primary
    val TextOnPrimaryDim = Color(0xFF04202E) // token: text/on-primary-dim (chips "VENCE EN 6 H" / "Entregar")

    // Estado (semáforo de días y badges)
    val StateInfo      = Color(0xFF0393C9) // token: state/info (azul, día seleccionado / dot de día con tarea)
    val StateInfoStrong = Color(0xFF0277A8) // token: state/info-strong (texto del día seleccionado y badges)
    val StateDanger    = Color(0xFFFF445A) // token: state/danger (badge navbar + dot de día crítico)
    val StateWarning   = Color(0xFFC88C14) // token: state/warning (fondo badge "FO" en Home B; amarillo)
    val StateNeutral   = Color(0xFF4D5866) // token: state/neutral (badge "EX" tareas neutras)
    val StateSuccess   = Color(0xFF15A05A) // token: state/success (verde) - dot "sincronizado" del Chat

    // Tokens únicos del Home A (`351:644`).
    // 2026-08-06: Figma actualizo el fondo del frame de gris azulado (#A8B6BC)
    // a blanco puro (bg-white en el design context re-extraido de "Paquito-v2").
    val SurfaceHomeCanvas       = Color(0xFFFFFFFF) // fondo del frame Home A (blanco, fiel a Figma actual)
    val SurfaceHomeWeekBg       = Color(0xFF10151A) // card oscura del calendario semanal
    val SurfaceHomeTaskListBg   = Color(0x33F5F5F5) // rgba(245,245,245,0.2) (task_list wrapper)
    val TextHomeStrong          = Color(0xFF000000) // saludo "Bienvenido"
    val TextHomeMuted           = Color(0xFF4D4D4D) // fecha "Lunes, 5 de enero de 2026"
    val TextHomeDayActiveLabel  = Color(0xE6FFFFFF) // rgba(255,255,255,0.9) dia actual
    val TextHomeDayLabel        = Color(0xE61C1B1F) // rgba(28,27,31,0.9) dia inactivo
    val TextHomeDayNumber       = Color(0xB300C9FB) // rgba(0,201,251,0.7) numero dia (BrandPrimary alpha)
    val TextHomeDayNumberCritical = Color(0xB3FF0000) // rgba(255,0,0,0.7) numero critico
    val TextTimestampLarge      = Color(0xFF29617B) // timestamp grande normal
    val TextTimestampLargeAccent = Color(0xB322CCFF) // rgba(34,204,255,0.7)
    val TextTimestampLargeMuted  = Color(0x8000C9FB) // rgba(0,201,251,0.5) futuro lejano
    val TextHomeTaskLabel       = Color(0x801C1B1F) // rgba(28,27,31,0.5) label "Curso" sobre task info

    // Superficies
    val Background     = Color(0xFFFFFFFF) // token: surface/background (fondo Welcome + Home)
    val SurfaceElevated = Color(0xFFEEEEEE) // token: surface/elevated (tab activo Navbar)
    val SurfaceGlassStrong = Color(0xD6FFFFFF) // rgba(255,255,255,0.82) (card "Paquito te avisó")
    val SurfaceGlassSoft   = Color(0xD9F6F7F9) // rgba(246,247,249,0.85) (días normales)
    val SurfaceOverlay  = Color(0x05000000) // rgba(0,0,0,0.02) (cards de selección Onboarding)

    // Texto
    val TextPrimary    = Color(0xD9000000) // rgba(0,0,0,0.85) (título "PaquitoBot" Welcome)
    val TextSecondary  = Color(0xB3000000) // rgba(0,0,0,0.7) (subtítulo Welcome)
    val TextOnSurface  = Color(0xFF1C1B1F) // token: text/on-surface (etiquetas Navbar)
    val TextOnCardStrong = Color(0xFF0D1520) // títulos de tareas / saludo "Hola, Andrea"
    val TextOnCardMuted  = Color(0xFF6E7885) // subtítulos de tareas ("Curso", "Martes 20:00")
    val TextOnCardMutedAlt = Color(0xFF7B8694) // "Paquito te avisó"
    val TextOnDarkMuted   = Color(0xFF93A3B4) // "Cálculo II" sobre card oscura
    val TextOnDarkStronger = Color(0xFFA3B1C0) // "Hoy 23:59" sobre card oscura
    val TextOnWhite       = Color(0xFFFFFFFF) // texto blanco (sobre fondo de card oscura)
    val TextBubble        = Color(0xFF16202C) // texto de burbuja del bot en Chat
    val TextBubbleChip    = Color(0xFF28323E) // texto de chips de sugerencia en Chat
    val TextInputPlaceholder = Color(0xFF757575) // placeholder del input
    val TextDayActive    = Color(0xFF0277A8) // día seleccionado (label "L")
    val TextDayInactive  = Color(0xFF8D96A1) // días inactivos (label "M", "J", etc.)
    val TextDayNumber    = Color(0xFF0D1520) // número del día seleccionado
    val TextDayNumberInactive = Color(0xFF4D5866) // número del día inactivo
    val TextTimestamp    = Color(0xFF98A1AC) // "2 d", "hace cuanto"
    val TextLink         = Color(0xFF0393C9) // "Ver todo" (link secundario Home B)
    val TextDisabled     = Color(0xFFB0B7BF) // token: text/disabled - aún no observado, placeholder gris claro

    // Bordes
    val BorderDefault  = Color(0x120D1520) // rgba(13,21,32,0.07) (divisores cards)
    val BorderSubtle   = Color(0x0F0D1520) // rgba(13,21,32,0.06) (días)
    val BorderInfo     = Color(0x730393C9) // rgba(3,147,201,0.45) (día seleccionado)
    val BorderOnDarkSubtle = Color(0x29FFFFFF) // rgba(255,255,255,0.16) (botón "Recordar 2 h" sobre card oscura)

    // Overlays translúcidos (badges de tarea)
    val OverlayInfoSubtle    = Color(0x240393C9) // rgba(3,147,201,0.14) (fondo badge "NT")
    val OverlayWarningSubtle = Color(0x29C88C14) // rgba(200,140,20,0.16) (fondo badge "FO")
    val OverlayNeutralSubtle = Color(0x140D1520) // rgba(13,21,32,0.08) (fondo badge "EX")
    val OverlayOnDarkSubtle  = Color(0x1FFFFFFF) // rgba(255,255,255,0.12) (botón secundario sobre card oscura)
}

/**
 * Escala tipográfica de PaquitoBot.
 *
 * El archivo Figma usa DM Sans, DM Mono, Instrument Sans y Bricolage Grotesque
 * mezcladas. Mientras no descarguemos las fuentes reales, todo cae a
 * FontFamily.Default (Roboto en Android). Mantenemos nombres y pesos para
 * cuando se haga la migración real.
 */
object PaquitoTypography {
    val DisplayLarge   = TextStyle(fontSize = 34.sp, fontWeight = FontWeight.SemiBold, fontFamily = PaquitoFont.DMSans)
    val HeadlineSmall  = TextStyle(fontSize = 23.sp, fontWeight = FontWeight.Normal,   fontFamily = PaquitoFont.DMSans)
    val BodyLarge      = TextStyle(fontSize = 17.sp, fontWeight = FontWeight.Normal,   fontFamily = PaquitoFont.DMSans)
    val BodySmall      = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal,   fontFamily = PaquitoFont.DMSans)
    val BodyMedium     = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal,   fontFamily = PaquitoFont.InstrumentSans)
    val Caption        = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal,   fontFamily = PaquitoFont.DMMono)
    val LabelSmall     = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium,   fontFamily = PaquitoFont.DMSans)

    // Nuevos (Home B)
    val Greeting       = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.Bold,     fontFamily = PaquitoFont.BricolageGrotesque)
    val TaskTitle      = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold, fontFamily = PaquitoFont.InstrumentSans)
    val TaskSubtitle   = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal,   fontFamily = PaquitoFont.InstrumentSans)
    val DayOfWeek      = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal,   fontFamily = PaquitoFont.DMMono)
    val DayNumber      = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = PaquitoFont.InstrumentSans)
    val BadgeInitials  = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Bold,     fontFamily = PaquitoFont.DMMono)
    val ChipLabel      = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium,   fontFamily = PaquitoFont.DMMono)
    val Timestamp      = TextStyle(fontSize = 11.sp, fontWeight = FontWeight.Normal,   fontFamily = PaquitoFont.DMMono)
    val AlertCardTitle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.SemiBold, fontFamily = PaquitoFont.BricolageGrotesque)
    val AlertCardSubtitle = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Normal, fontFamily = PaquitoFont.InstrumentSans)
    val ButtonLabel    = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.SemiBold, fontFamily = PaquitoFont.InstrumentSans)
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
    val card   = RoundedCornerShape(24.dp)
    val alertCard = RoundedCornerShape(26.dp)
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
        error         = PaquitoColors.StateDanger,
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