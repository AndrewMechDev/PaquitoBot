package pe.tecsup.paquitobot.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Roles de un mensaje en el chat de Paquito.
 *
 * [System] es un aviso de estado (ej. "sin conexión", "no se pudo enviar
 * tu mensaje") que aparece COMO UN ITEM MAS del flujo de mensajes, no como
 * chrome fijo en el header. Iteracion 2026-08-06: reemplaza al chip de
 * estado de conexion que existia antes en `ChatHeader.kt` — el usuario lo
 * encontro visualmente ruidoso ("rompe el diseño"). Mismo patron que usan
 * apps de chat consolidadas (WhatsApp, etc.) para estos avisos.
 */
enum class MessageRole { Bot, User, System }

/**
 * Estado de entrega de un mensaje del usuario, estilo WhatsApp:
 * - [Sent]: 1 palomita gris (salio del dispositivo).
 * - [Delivered]: 2 palomitas grises (llego al servidor/bot).
 * - [Read]: 2 palomitas celestes (el bot "lo proceso").
 * Solo aplica a mensajes [MessageRole.User] - el bot no tiene estado de
 * lectura propio. Sin logica real de entrega todavia (mock/hardcoded).
 */
enum class MessageStatus { Sent, Delivered, Read }

/**
 * Mensaje individual del chat.
 *
 * @param role quien lo emite (Bot, User o System).
 * @param body texto principal (acepta multiples parrafos separados por \n).
 * @param footnote texto opcional adicional bajo el body (ej. "Te aviso otra
 *   vez a las 20:00", "Fuente: notas del LMS · 2 ago"). Iteracion 2026-08-06:
 *   ya NO tiene caja con borde (se veia "raro/feo") - es texto simple, un
 *   dato mas dentro de la burbuja.
 * @param timestamp hora del mensaje (ej. "14:32"), se muestra chica abajo a
 *   la derecha de la burbuja, estilo WhatsApp.
 * @param status estado de entrega (solo relevante para [MessageRole.User]),
 *   se muestra como palomitas junto al timestamp.
 */
data class ChatMessage(
    val role: MessageRole,
    val body: String,
    val footnote: String? = null,
    val timestamp: String? = null,
    val status: MessageStatus? = null,
)

/**
 * Renderiza un [ChatMessage] fiel al frame Figma `438:662` (instancia
 * "chat" `438:671`): burbujas planas sin sombra ni borde.
 * - Burbuja del bot: fondo `#F6F6F6`, texto negro, esquina superior
 *   izquierda con radio chico (2dp) formando la "cola".
 * - Burbuja del usuario: fondo `PaquitoColors.BrandPrimary` (`#00C9FB`),
 *   texto blanco, esquina superior derecha con radio chico (2dp).
 */
@Composable
fun Message(
    message: ChatMessage,
    modifier: Modifier = Modifier,
) {
    when (message.role) {
        MessageRole.Bot    -> BotBubble(message, modifier)
        MessageRole.User   -> UserBubble(message, modifier)
        MessageRole.System -> SystemNotice(message, modifier)
    }
}

// Radios de burbuja fieles al frame Figma 438:662 (instancia "chat"
// 438:671): rounded 20dp en 3 esquinas, 2dp en la esquina que forma la
// "cola" hacia el emisor. Antes eran burbujas glassmorphism (sombra +
// borde + fondo translucido/gradiente); el frame actual las pide PLANAS,
// sin sombra ni borde.
private val BotBubbleShape = RoundedCornerShape(
    topStart = 2.dp, topEnd = 20.dp, bottomEnd = 20.dp, bottomStart = 20.dp,
)
private val UserBubbleShape = RoundedCornerShape(
    topStart = 20.dp, topEnd = 2.dp, bottomEnd = 20.dp, bottomStart = 20.dp,
)

@Composable
private fun BotBubble(message: ChatMessage, modifier: Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        PaquitoAvatar()
        Column(
            modifier = Modifier
                .widthIn(max = 258.dp)
                .clip(BotBubbleShape)
                .background(Color(0xFFF6F6F6))
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            PaquitoMarkdown(
                content = message.body,
                textColor = Color.Black,
            )
            if (message.footnote != null) {
                BubbleFootnote(text = message.footnote)
            }
            if (message.timestamp != null) {
                MessageMeta(timestamp = message.timestamp, status = null, onLightBubble = false)
            }
        }
    }
}

/**
 * Avatar chico de Paquito junto a cada burbuja del bot - mismo icono
 * (`ic_paquito_bot`, aspect ratio real 50:40) que ya se usa en el FAB del
 * Navbar, en circulo oscuro para que se note sobre el fondo claro de la
 * burbuja. Le da identidad visual al chat (antes solo texto plano, sin
 * ninguna señal de "quien" esta respondiendo).
 */
@Composable
private fun PaquitoAvatar() {
    Row(
        modifier = Modifier
            .padding(end = 8.dp)
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFF1C1B1F)),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_paquito_bot),
            contentDescription = null,
            modifier = Modifier.size(width = 20.dp, height = 16.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

/**
 * Burbuja "escribiendo..." mientras se espera la respuesta del bot -
 * mismo estilo que [BotBubble], 3 puntos con una animacion de fade
 * secuencial simple (sin depender de una libreria de animacion externa).
 */
@Composable
fun TypingIndicatorBubble(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
    ) {
        PaquitoAvatar()
        Row(
            modifier = Modifier
                .clip(BotBubbleShape)
                .background(Color(0xFFF6F6F6))
                .padding(horizontal = 16.dp, vertical = 15.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(3) { index ->
                TypingDot(delayMillis = index * 160)
            }
        }
    }
}

@Composable
private fun TypingDot(delayMillis: Int) {
    val transition = rememberInfiniteTransition(label = "typing-dot")
    val alpha by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, delayMillis = delayMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "typing-dot-alpha",
    )
    Row(
        modifier = Modifier
            .size(7.dp)
            .alpha(alpha)
            .clip(CircleShape)
            .background(PaquitoColors.TextOnCardMuted),
    ) {}
}

@Composable
private fun UserBubble(message: ChatMessage, modifier: Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(UserBubbleShape)
                .background(PaquitoColors.BrandPrimary)
                .padding(horizontal = 16.dp, vertical = 13.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = message.body,
                fontSize = 15.sp,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextOnWhite,
                lineHeight = 20.sp,
            )
            if (message.timestamp != null) {
                MessageMeta(timestamp = message.timestamp, status = message.status, onLightBubble = true)
            }
        }
    }
}

/**
 * Hora + palomitas de estado, estilo WhatsApp: chico, abajo a la derecha
 * DE LA BURBUJA (no de la pantalla) - aplica igual para burbujas de bot
 * (solo hora) y de usuario (hora + palomitas). [onLightBubble] ajusta el
 * color para que se lea sobre el fondo cyan del usuario.
 */
@Composable
private fun MessageMeta(timestamp: String, status: MessageStatus?, onLightBubble: Boolean) {
    val textColor = if (onLightBubble) Color.White.copy(alpha = 0.85f) else PaquitoColors.TextOnCardMuted
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = timestamp,
            fontSize = 11.sp,
            fontFamily = PaquitoFont.DMSans,
            color = textColor,
        )
        if (status != null) {
            Text(
                text = if (status == MessageStatus.Sent) " ✓" else " ✓✓",
                fontSize = 11.sp,
                fontFamily = PaquitoFont.DMSans,
                color = if (status == MessageStatus.Read) PaquitoColors.StateInfo else textColor,
            )
        }
    }
}

/**
 * Aviso de sistema (sin conexión, mensaje no entregado, etc.), centrado y
 * discreto - un chip chico, no una burbuja como los mensajes de Bot/User.
 */
@Composable
private fun SystemNotice(message: ChatMessage, modifier: Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        Text(
            text = message.body,
            fontSize = 12.sp,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextOnCardMuted,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color(0x0D0D1520))
                .padding(horizontal = 12.dp, vertical = 6.dp),
        )
    }
}

@Composable
private fun BubbleFootnote(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontFamily = PaquitoFont.DMSans,
        color = PaquitoColors.TextOnCardMuted,
        lineHeight = 16.sp,
    )
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
                    timestamp = "14:30",
                ),
            )
            Message(
                ChatMessage(
                    role = MessageRole.Bot,
                    body = "Lo más urgente:\nLaboratorio 4 — Cálculo II\nHoy 23:59 · 15% de la nota final",
                    footnote = "Te aviso otra vez a las 20:00",
                    timestamp = "14:30",
                ),
            )
            Message(
                ChatMessage(
                    role = MessageRole.User,
                    body = "¿cómo voy en cálculo?",
                    timestamp = "14:32",
                    status = MessageStatus.Read,
                ),
            )
            Message(
                ChatMessage(
                    role = MessageRole.Bot,
                    body = "Vas en 14.8 con 3 de 5 evaluaciones. Si entregás el Lab 4 completo subís a ~15.6. Tu punto débil: los prácticos calificados (11, 13).",
                    footnote = "Fuente: notas del LMS · 2 ago",
                    timestamp = "14:33",
                ),
            )
        }
    }
}
