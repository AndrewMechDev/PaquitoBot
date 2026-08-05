package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Chip de sugerencia rapida en el chat (Figma 285:353-285:360).
 *
 * Ejemplos:
 *   "Que vence esta semana?"
 *   "Como voy?"
 *   "Mis laboratorios"
 *   "Proximo examen"
 *
 * Estilo: glassmorphism rgba(255,255,255,0.85), border rgba(13,21,32,0.09),
 * rounded 16dp, padding 15x10, texto Instrument Sans Medium 13sp color
 * #28323E. Si hay overflow horizontal, los chips quedan en un Row scrolleable.
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
        color = PaquitoColors.TextBubbleChip,
        modifier = modifier
            .shadow(1.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(PaquitoColors.SurfaceGlassStrong.copy(alpha = 0.85f))
            .border(1.dp, androidx.compose.ui.graphics.Color(0x170D1520), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 15.dp, vertical = 10.dp),
    )
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun SuggestionChipPreview() {
    PaquitoTheme {
        SuggestionChip(text = "Que vence esta semana?", onClick = {})
    }
}