package pe.tecsup.paquitobot.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import pe.tecsup.paquitobot.R

/**
 * Familias tipográficas de PaquitoBot.
 *
 * Descargadas de Google Fonts (open source, SIL OFL). Las 4 familias que
 * aparecen en el Figma "Paquito (copia)":
 *
 *   - DM Sans              → títulos y cuerpo principal
 *   - DM Mono              → badges, etiquetas de día, timestamps
 *   - Instrument Sans      → textos sobre cards (Home B)
 *   - Bricolage Grotesque  → saludo principal y card de alerta (Home B)
 *
 * Cada familia expone sus pesos Regular / Medium / SemiBold / Bold
 * (algunas solo tienen los pesos que se usan, marcado en comentario).
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