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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
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
 * `MessageRole.System` (ver `Message.kt`) - se dispara desde donde se
 * detecte la falla real cuando exista logica de red.
 *
 * Mientras no hay backend, los mensajes y sugerencias son mock/hardcoded.
 */
@Composable
fun ChatScreen(
    userName: String = "{nombre}",
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var messages by remember {
        mutableStateOf(defaultMessages())
    }
    val scrollState = rememberScrollState()

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
    }
}

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
        ChatScreen()
    }
}