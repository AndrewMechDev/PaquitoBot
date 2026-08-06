package pe.tecsup.paquitobot.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.chat.components.ChatHeader
import pe.tecsup.paquitobot.ui.components.ChatInput
import pe.tecsup.paquitobot.ui.components.ChatMessage
import pe.tecsup.paquitobot.ui.components.Message
import pe.tecsup.paquitobot.ui.components.MessageRole
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.components.Navbar
import pe.tecsup.paquitobot.ui.components.SuggestionChip
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Pantalla completa del chat con Paquito (Figma 285:324).
 *
 * Estructura:
 *   [Header glassmorphism con avatar + titulo + chip LMS]
 *   [Divider "HOY"]
 *   [Lista de mensajes (burbujas bot + usuario)]
 *   [Row scrolleable de chips de sugerencia]
 *   [Input inferior + boton enviar]
 *   [Navbar inferior con badge]
 *
 * Mientras no hay backend, los mensajes y sugerencias son mock.
 */
@Composable
fun ChatScreen(
    currentTab: NavTab = NavTab.Inicio,
    onTabSelected: (NavTab) -> Unit = {},
    notificationCount: Int? = 3,
    onPaquitoClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var messages by remember {
        mutableStateOf(defaultMessages())
    }
    val scrollState = rememberScrollState()

    Box(modifier = modifier.fillMaxSize().background(PaquitoColors.Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 110.dp), // espacio para la navbar
        ) {
            // Header.
            ChatHeader()

            // Divider "HOY".
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center,
            ) {
                DayDivider(text = "HOY")
            }

            // Lista de mensajes scrolleable.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                messages.forEach { msg ->
                    Message(message = msg)
                }
            }

            // Chips de sugerencia (scrollable horizontal).
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                listOf(
                    "Que vence esta semana?",
                    "Como voy?",
                    "Mis laboratorios",
                    "Proximo examen",
                ).forEach { suggestion ->
                    SuggestionChip(
                        text = suggestion,
                        onClick = {
                            // Mock: anade la sugerencia como mensaje del usuario.
                            messages = messages + ChatMessage(
                                role = MessageRole.User,
                                body = suggestion,
                            )
                        },
                    )
                }
            }

            // Input.
            ChatInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                onSend = { text ->
                    messages = messages + ChatMessage(
                        role = MessageRole.User,
                        body = text,
                    )
                    // Mock respuesta automatica del bot.
                    messages = messages + ChatMessage(
                        role = MessageRole.Bot,
                        body = "Buena pregunta. Estoy conectandome al LMS...",
                    )
                },
            )
        }

        // Navbar anclada abajo (la "X" central abre el chat, ya estamos aca).
        // Importante: padding inferior y horizontal DEBEN ser identicos a los
        // de HomeScreen.kt para que la Navbar se vea en la misma posicion en
        // todas las pantallas. El Box usa contentAlignment=Center para
        // centrar el Row de la Navbar (que ya no usa fillMaxWidth internamente).
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Navbar(
                currentTab = currentTab,
                onTabSelected = onTabSelected,
                onPaquitoClick = onPaquitoClick,
                notificationCount = notificationCount,
            )
        }
    }
}

private fun defaultMessages(): List<ChatMessage> = listOf(
    ChatMessage(
        role = MessageRole.Bot,
        body = "Hola Andrea. Revise tu LMS: 3 cosas por vencer esta semana y una nota nueva en Calculo II.",
    ),
    ChatMessage(
        role = MessageRole.Bot,
        body = "Lo mas urgente:\nLaboratorio 4 - Calculo II\nHoy 23:59 - 15% de la nota final",
        footnote = "Te aviso otra vez a las 20:00",
    ),
    ChatMessage(
        role = MessageRole.User,
        body = "como voy en calculo?",
    ),
    ChatMessage(
        role = MessageRole.Bot,
        body = "Vas en 14.8 con 3 de 5 evaluaciones. Si entregas el Lab 4 completo subis a ~15.6. Tu punto debil: los practicos calificados (11, 13).",
        footnote = "Fuente: notas del LMS - 2 ago",
    ),
)

/** Chip divisor "HOY" del Figma 285:331-285:332. */
@Composable
private fun DayDivider(text: String) {
    Box(
        modifier = Modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
            .background(androidx.compose.ui.graphics.Color(0x120D1520))
            .padding(horizontal = 11.dp, vertical = 5.dp),
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontFamily = pe.tecsup.paquitobot.ui.theme.PaquitoFont.DMMono,
            color = PaquitoColors.TextOnCardMuted,
            letterSpacing = 1.sp,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    PaquitoTheme {
        ChatScreen()
    }
}