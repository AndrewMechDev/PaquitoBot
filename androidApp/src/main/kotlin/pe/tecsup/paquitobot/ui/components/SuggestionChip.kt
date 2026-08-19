package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Chip de sugerencia rapida en el chat. No es parte del frame Figma actual
 * (`438:662`, mas minimalista) pero se mantiene a pedido del usuario -
 * util para no escribir siempre desde cero. Restyleado a plano (fondo
 * `#F6F6F6`, sin sombra ni borde) para que combine con el resto del chat
 * (burbujas e input tambien planos, ver `Message.kt`/`ChatInput.kt`).
 *
 * Ejemplos:
 *   "Que cursos tengo este ciclo?"
 *   "Que vence este ciclo?"
 *   "Mis laboratorios"
 *   "Como voy?"
 *
 * Si hay overflow horizontal, los chips quedan en un Row scrolleable.
 */
@Composable
fun SuggestionChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        fontSize = 13.sp,
        color = PaquitoColors.TextOnCardStrong,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(androidx.compose.ui.graphics.Color(0xFFF6F6F6))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 10.dp),
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SuggestionChipPreview() {
    PaquitoTheme {
        SuggestionChip(text = "Que cursos tengo este ciclo?", onClick = {})
    }
}