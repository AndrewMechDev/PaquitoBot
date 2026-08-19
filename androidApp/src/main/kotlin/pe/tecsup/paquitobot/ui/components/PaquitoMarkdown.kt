package pe.tecsup.paquitobot.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.m3.markdownTypography
import pe.tecsup.paquitobot.ui.theme.PaquitoFont

/**
 * Renderiza texto Markdown (formato en el que responde paquitobot-rag:
 * negritas, tablas, headers) en vez de mostrarlo como texto plano con
 * "**"/"##" literales.
 *
 * `markdownTypography()` por defecto usa slots de `MaterialTheme.typography`
 * (h1=displayLarge, h2=displayMedium, ...) - `PaquitoTheme` solo pisa ALGUNOS
 * de esos slots con DM Sans (displayLarge, headlineSmall, bodyLarge,
 * bodyMedium, bodySmall, labelSmall), asi que h2/h3/h4/h6 caerian en la
 * tipografia default de Material3 (no DM Sans) si se usara el default de la
 * libreria tal cual. Por eso aca se pisan TODOS los estilos explicitamente -
 * regla dura del proyecto: una sola familia tipografica en toda la app.
 */
@Composable
fun PaquitoMarkdown(
    content: String,
    modifier: Modifier = Modifier,
    textColor: Color = Color.Black,
    fontSize: TextUnit = 15.sp,
    lineHeight: TextUnit = 20.sp,
) {
    val base = TextStyle(
        fontFamily = PaquitoFont.DMSans,
        color = textColor,
        fontSize = fontSize,
        lineHeight = lineHeight,
    )
    Markdown(
        content = content,
        colors = markdownColor(text = textColor),
        typography = markdownTypography(
            h1 = base.copy(fontSize = fontSize * 1.5f, fontWeight = FontWeight.Bold),
            h2 = base.copy(fontSize = fontSize * 1.35f, fontWeight = FontWeight.Bold),
            h3 = base.copy(fontSize = fontSize * 1.2f, fontWeight = FontWeight.SemiBold),
            h4 = base.copy(fontWeight = FontWeight.SemiBold),
            h5 = base.copy(fontWeight = FontWeight.SemiBold),
            h6 = base.copy(fontWeight = FontWeight.Medium),
            text = base,
            paragraph = base,
            ordered = base,
            bullet = base,
            list = base,
            table = base,
            quote = base.copy(fontWeight = FontWeight.Normal),
            code = base.copy(fontFamily = PaquitoFont.DMSans),
            inlineCode = base.copy(fontFamily = PaquitoFont.DMSans),
        ),
        modifier = modifier,
    )
}
