package pe.tecsup.paquitobot.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import pe.tecsup.paquitobot.R

/**
 * Familias tipográficas de PaquitoBot.
 *
 * Descargadas de Google Fonts (open source, SIL OFL). El Figma original
 * mezclaba 4 familias (DM Sans, DM Mono, Instrument Sans, Bricolage
 * Grotesque) para roles distintos, pero eso rompia la consistencia
 * visual entre pantallas.
 *
 * Decision de UX (2026-08-19): TODO `PaquitoTypography` usa solo [DMSans]
 * - es la unica familia activa en el proyecto. `DMMono`/`InstrumentSans`/
 * `BricolageGrotesque` quedan declaradas pero SIN USO - no referenciarlas
 * desde ningun Composable nuevo, rompen el patron.
 */
object PaquitoFont {
    val DMSans = FontFamily(
        Font(R.font.dm_sans_regular,  FontWeight.Normal),
        Font(R.font.dm_sans_medium,   FontWeight.Medium),
        Font(R.font.dm_sans_semibold, FontWeight.SemiBold),
        Font(R.font.dm_sans_bold,     FontWeight.Bold),
    )

    val DMMono = FontFamily(
        Font(R.font.dm_mono_regular, FontWeight.Normal),
        Font(R.font.dm_mono_medium,  FontWeight.Medium),
    )

    val InstrumentSans = FontFamily(
        Font(R.font.instrument_sans_regular,  FontWeight.Normal),
        Font(R.font.instrument_sans_medium,   FontWeight.Medium),
        Font(R.font.instrument_sans_semibold, FontWeight.SemiBold),
        Font(R.font.instrument_sans_bold,     FontWeight.Bold),
    )

    val BricolageGrotesque = FontFamily(
        Font(R.font.bricolage_grotesque_semibold, FontWeight.SemiBold),
        Font(R.font.bricolage_grotesque_bold,     FontWeight.Bold),
    )
}