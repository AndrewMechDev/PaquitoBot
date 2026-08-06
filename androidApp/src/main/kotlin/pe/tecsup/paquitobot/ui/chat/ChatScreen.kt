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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.chat.components.ChatHeader
import pe.tecsup.paquitobot.ui.chat.components.SyncStatus
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
 * Pantalla completa del chat con Paquito, fiel al frame Figma `438:662`.
 *
 * Estructura:
 *   [Header: flecha atras + "Hola, {nombre}" + subtitulo + chip de sync]
 *   [Lista de mensajes (burbujas bot + usuario, planas)]
 *   [Row scrolleable de chips de sugerencia] (se mantiene, no viene del
 *     frame actual pero el usuario pidio conservarlo)
 *   [Input inferior + boton enviar]
 *   [Navbar inferior con badge]
 *
 * Mientras no hay backend, los mensajes, sugerencias y [syncStatus] son
 * mock/hardcoded. Cuando exista logica real, [syncStatus] se conecta al
 * estado real de sincronizacion con el LMS.
 */
@Composable
fun ChatScreen(
    userName: String = "{nombre}",
    syncStatus: SyncStatus = SyncStatus.Synced,
    currentTab: NavTab = NavTab.Inicio,
    onTabSelected: (NavTab) -> Unit = {},
    notificationCount: Int? = 3,
    onPaquitoClick: () -> Unit = {},
    onBackClick: () -> Unit = onPaquitoClick,
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
                .padding(horizontal = 24.dp)
                .padding(bottom = 110.dp), // espacio para la navbar
        ) {
            // Header: flecha atras + saludo + chip de sincronizacion.
            ChatHeader(
                userName = userName,
                syncStatus = syncStatus,
                onBackClick = onBackClick,
            )

            // Lista de mensajes scrolleable.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(top = 20.dp),
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
                    .padding(vertical = 10.dp),
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
                    .padding(vertical = 12.dp),
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
        // Iteracion 2026-08-06 01:37: padding(bottom) unificado a 24.dp para
        // que la Navbar respire del borde inferior tanto en Home como en Chat.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
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

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    PaquitoTheme {
        ChatScreen()
    }
}