package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Roles de un mensaje en el chat de Paquito.
 */
enum class MessageRole { Bot, User }

/**
 * Mensaje individual del chat.
 *
 * @param role quien lo emite (Bot o User, determina el estilo).
 * @param body texto principal (acepta multiples parrafos separados por \n).
 * @param footnote texto opcional al pie del mensaje (separado por un border-top).
 *   Para los mensajes del bot: "Te aviso otra vez a las 20:00", "Fuente: notas del LMS · 2 ago".
 */
data class ChatMessage(
    val role: MessageRole,
    val body: String,
    val footnote: String? = null,
)

/**
 * Renderiza un [ChatMessage] respetando el diseño del Figma 285:324.
 *
 * - Burbuja del bot: rounded 22 22 22 7 (esquina inferior izquierda con menos
 *   radio para formar la "cola"). Fondo glassmorphism rgba(255,255,255,0.88)
 *   + border rgba(13,21,32,0.07) + shadow card.
 * - Burbuja del usuario: rounded 22 22 7 22 (cola a la derecha). Gradiente
 *   turquesa + border rgba(3,147,201,0.45) + shadow.
 */
@Composable
fun Message(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    when (message.role) {
        MessageRole.Bot  -> BotBubble(message, modifier)
        MessageRole.User -> UserBubble(message, modifier)
    }
}

@Composable
private fun BotBubble(message: ChatMessage, modifier: Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 285.dp)
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(
                        topStart = 22.dp,
                        topEnd = 22.dp,
                        bottomEnd = 22.dp,
                        bottomStart = 7.dp,
                    ),
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 22.dp,
                        topEnd = 22.dp,
                        bottomEnd = 22.dp,
                        bottomStart = 7.dp,
                    ),
                )
                .background(PaquitoColors.SurfaceGlassStrong)
                .border(1.dp, PaquitoColors.BorderDefault, RoundedCornerShape(22.dp, 22.dp, 22.dp, 7.dp))
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = message.body,
                fontSize = 14.sp,
                color = PaquitoColors.TextBubble,
                lineHeight = 22.sp,
            )
            if (message.footnote != null) {
                BubbleFootnote(text = message.footnote)
            }
        }
    }
}

@Composable
private fun UserBubble(message: ChatMessage, modifier: Modifier) {
    val gradient = Brush.linearGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color(0xFF2AD0FF),
            androidx.compose.ui.graphics.Color(0xFF0393C9),
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(150f, 200f),
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 285.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(
                        topStart = 22.dp,
                        topEnd = 22.dp,
                        bottomEnd = 7.dp,
                        bottomStart = 22.dp,
                    ),
                )
                .clip(
                    RoundedCornerShape(
                        topStart = 22.dp,
                        topEnd = 22.dp,
                        bottomEnd = 7.dp,
                        bottomStart = 22.dp,
                    ),
                )
                .background(gradient)
                .border(
                    width = 1.dp,
                    color = androidx.compose.ui.graphics.Color(0x730393C9),
                    shape = RoundedCornerShape(22.dp, 22.dp, 7.dp, 22.dp),
                )
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = message.body,
                fontSize = 14.sp,
                color = PaquitoColors.TextOnWhite,
                lineHeight = 22.sp,
            )
        }
    }
}

@Composable
private fun BubbleFootnote(text: String) {
    Column(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = androidx.compose.ui.graphics.Color(0x1A0D1520),
                shape = RoundedCornerShape(0.dp),
            )
            .padding(top = 10.dp, start = 0.dp, end = 0.dp, bottom = 0.dp),
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = PaquitoColors.TextOnCardMuted,
            lineHeight = 16.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MessagePreview() {
    PaquitoTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(PaquitoSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm),
        ) {
            Message(
                ChatMessage(
                    role = MessageRole.Bot,
                    body = "Hola Andrea. Revisé tu LMS: 3 cosas por vencer esta semana y una nota nueva en Cálculo II.",
                ),
            )
            Message(
                ChatMessage(
                    role = MessageRole.Bot,
                    body = "Lo más urgente:\nLaboratorio 4 — Cálculo II\nHoy 23:59 · 15% de la nota final",
                    footnote = "Te aviso otra vez a las 20:00",
                ),
            )
            Message(
                ChatMessage(
                    role = MessageRole.User,
                    body = "¿cómo voy en cálculo?",
                ),
            )
            Message(
                ChatMessage(
                    role = MessageRole.Bot,
                    body = "Vas en 14.8 con 3 de 5 evaluaciones. Si entregás el Lab 4 completo subís a ~15.6. Tu punto débil: los prácticos calificados (11, 13).",
                    footnote = "Fuente: notas del LMS · 2 ago",
                ),
            )
        }
    }
}