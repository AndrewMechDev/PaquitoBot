package pe.tecsup.paquitobot.ui.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.chat.components.ChatHeader
import pe.tecsup.paquitobot.ui.components.ChatInput
import pe.tecsup.paquitobot.ui.components.ChatMessage
import pe.tecsup.paquitobot.ui.components.Message
import pe.tecsup.paquitobot.ui.components.MessageRole
import pe.tecsup.paquitobot.ui.components.MessageStatus
import pe.tecsup.paquitobot.ui.components.SuggestionChip
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Pantalla completa del chat con Paquito, fiel al frame Figma `438:662`.
 *
 * Estructura:
 *   [Header: flecha atras + "Hola, {nombre}" + subtitulo]
 *   [Lista de mensajes (burbujas bot + usuario + avisos de sistema, planas)]
 *   [Row scrolleable de chips de sugerencia] (se mantiene, no viene del
 *     frame actual pero el usuario pidio conservarlo)
 *   [Input inferior + boton enviar]
 *
 * Iteracion 2026-08-06: se saca la Navbar flotante de esta pantalla — el
 * chat ya tiene su propia flecha de "volver" en el header, mostrar ademas
 * el navbar completo (Home/Cursos/Horarios/FAB) es navegacion duplicada y
 * rompe la composicion visual de una pantalla de chat a pantalla completa.
 *
 * Iteracion 2026-08-06 (teclado): `Modifier.imePadding()` en el Column
 * raiz para que, al abrir el teclado, el contenido se desplace limpio
 * hacia arriba (estilo WhatsApp) en vez de que el teclado tape el input.
 * Complementa `android:windowSoftInputMode="adjustResize"` en el Manifest.
 *
 * Iteracion 2026-08-06 (estado de conexion): ya no hay un chip fijo de
 * "conectado/desconectado" en el header (se sacó, rompía el diseño). El
 * estado de conexion se comunica como un mensaje mas en el flujo via
 * `MessageRole.System` (ver `Message.kt`).
 *
 * La pantalla dejo de manejar su
 * propia lista de mensajes con `remember` - ahora recibe [uiState] +
 * [onSendMessage] desde [ChatViewModel] (MVVM, stateless composable).
 * Las sugerencias tambien pasan por [onSendMessage] en vez de mutar
 * estado local directamente, para que toda pregunta (escrita o por chip)
 * pase por el mismo repositorio mock. Clerk controla el acceso desde
 * `MainActivity`; esta pantalla solo presenta la conversación.
 */
@Composable
fun ChatScreen(
    uiState: ChatUiState,
    onSendMessage: (String) -> Unit,
    userName: String = "estudiante",
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val messages = uiState.messages
    val scrollState = rememberScrollState()
    val keyboardController = LocalSoftwareKeyboardController.current
    val dismissKeyboardOnUserScroll = androidx.compose.runtime.remember(keyboardController) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.UserInput && available.y != 0f) {
                    keyboardController?.hide()
                }
                return Offset.Zero
            }
        }
    }
    LaunchedEffect(messages.size, uiState.isSending) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Box(modifier = modifier.fillMaxSize().background(PaquitoColors.Background)) {
        // Iteracion 2026-08-06 (feedback "muy separado del teclado"): el
        // padding(bottom=24dp) SUMABA por encima del imePadding() en vez de
        // reemplazarlo, dejando un hueco grande entre el input y el teclado
        // (imePadding ya agrega exactamente la altura del teclado; el 24dp
        // fijo era ADEMAS de eso). Bajado a 8dp - un respiro chico cuando el
        // teclado esta cerrado, sin duplicar espacio cuando esta abierto.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 8.dp),
        ) {
            // Header: flecha atras + saludo.
            ChatHeader(
                userName = userName,
                onBackClick = onBackClick,
            )

            // Lista de mensajes scrolleable.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .nestedScroll(dismissKeyboardOnUserScroll)
                    .verticalScroll(scrollState)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                messages.forEach { msg ->
                    Message(message = msg)
                }
                if (uiState.isSending) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CircularProgressIndicator()
                        Text(
                            text = "Paquito está pensando…",
                            style = PaquitoTypography.BodySmall,
                            color = PaquitoColors.TextHomeMuted,
                        )
                    }
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
                ChatSuggestionPrompts.forEach { suggestion ->
                    SuggestionChip(
                        text = suggestion,
                        onClick = { onSendMessage(suggestion) },
                    )
                }
            }

            // Input.
            ChatInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                enabled = !uiState.isSending,
                onSend = onSendMessage,
            )
        }
    }
}

/**
 * Los chips envían preguntas al mismo repositorio MiniMax que el campo de texto.
 */
private val ChatSuggestionPrompts = listOf(
    "Que cursos tengo este ciclo?",
    "Que vence este ciclo?",
    "Mis laboratorios",
    "Como voy?",
)

private fun defaultMessages(): List<ChatMessage> = listOf(
    ChatMessage(
        role = MessageRole.Bot,
        body = "Hola Andrea. Revise tu LMS: 3 cosas por vencer esta semana y una nota nueva en Calculo II.",
        timestamp = "14:18",
    ),
    ChatMessage(
        role = MessageRole.Bot,
        body = "Lo mas urgente:\nLaboratorio 4 - Calculo II\nHoy 23:59 - 15% de la nota final",
        footnote = "Te aviso otra vez a las 20:00",
        timestamp = "14:18",
    ),
    ChatMessage(
        role = MessageRole.User,
        body = "como voy en calculo?",
        timestamp = "14:19",
        status = MessageStatus.Read,
    ),
    ChatMessage(
        role = MessageRole.Bot,
        body = "Vas en 14.8 con 3 de 5 evaluaciones. Si entregas el Lab 4 completo subis a ~15.6. Tu punto debil: los practicos calificados (11, 13).",
        footnote = "Fuente: notas del LMS - 2 ago",
        timestamp = "14:19",
    ),
)

@Preview(showBackground = true)
@Composable
private fun ChatScreenPreview() {
    PaquitoTheme {
        ChatScreen(
            uiState = ChatUiState(messages = defaultMessages()),
            onSendMessage = {},
            userName = "Andrea",
        )
    }
}
