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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.chat.components.ChatGreeting
import pe.tecsup.paquitobot.ui.chat.components.ChatTopBar
import pe.tecsup.paquitobot.ui.components.ChatInput
import pe.tecsup.paquitobot.ui.components.ChatMessage
import pe.tecsup.paquitobot.ui.components.Message
import pe.tecsup.paquitobot.ui.components.MessageRole
import pe.tecsup.paquitobot.ui.components.MessageStatus
import pe.tecsup.paquitobot.ui.components.SuggestionChip
import pe.tecsup.paquitobot.ui.components.TypingIndicatorBubble
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
 * `MessageRole.System` (ver `Message.kt`).
 *
 * Iteracion 2026-08-11 (backend real): la pantalla dejo de manejar su
 * propia lista de mensajes con `remember` - ahora recibe [uiState] +
 * [onSendMessage] desde [ChatViewModel] (MVVM, stateless composable).
 * Las sugerencias tambien pasan por [onSendMessage] en vez de mutar
 * estado local directamente, para que toda pregunta (escrita o por chip)
 * pase por el mismo camino hacia el backend.
 *
 * Iteracion 2026-08-13 (gates a nivel app): el login con Google y la
 * conexion con Canvas ahora bloquean TODA la app desde `MainActivity`
 * (`AuthGateScreen` / `CanvasConnectScreen`), antes de que esta pantalla
 * siquiera se muestre - ya no maneja `isAuthenticated` ni gate propio.
 *
 * Iteracion 2026-08-19 (bugs reales, captura del usuario):
 * - El header completo (flecha + saludo + subtitulo) vivia FIJO fuera del
 *   scroll, consumiendo espacio permanente de la vista en pantallas
 *   chicas. Se separa: [ChatTopBar] (solo la flecha) queda fija arriba -
 *   el usuario siempre necesita poder volver -; el saludo (`ChatGreeting`)
 *   pasa a ser el primer item DENTRO de la lista scrolleable de mensajes,
 *   asi se va con el scroll al leer el historial.
 * - Los chips de sugerencia tambien consumian espacio fijo permanente del
 *   footer. Ahora solo se muestran cuando todavia no hay mensajes (inicio
 *   de la conversacion) - mismo criterio que apps de chat con IA (las
 *   sugerencias son para arrancar, no para acompañar toda la charla).
 * - El scroll no bajaba solo al enviar/recibir un mensaje: el mensaje mas
 *   reciente quedaba tapado por el footer fijo. Se agrega auto-scroll al
 *   final de la lista cada vez que cambia la cantidad de mensajes.
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

    // Auto-scroll al ultimo mensaje: sin esto, al enviar o recibir una
    // respuesta el scroll se quedaba en la posicion anterior y el mensaje
    // nuevo quedaba tapado por el footer fijo (bug real, captura del
    // usuario 2026-08-19).
    LaunchedEffect(messages.size, uiState.isSending) {
        if (messages.isNotEmpty() || uiState.isSending) {
            scrollState.animateScrollTo(scrollState.maxValue)
        }
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
            // Fija arriba: SOLO la flecha atras (chica) - el saludo se movio
            // adentro del scroll, ver comentario de la clase.
            ChatTopBar(onBackClick = onBackClick)

            // Lista de mensajes scrolleable. El saludo es el primer item -
            // se va con el scroll al leer el historial, en vez de quedar
            // fijo consumiendo espacio permanente.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(11.dp),
            ) {
                ChatGreeting(userName = userName)
                messages.forEach { msg ->
                    Message(message = msg)
                }
                if (uiState.isSending) {
                    TypingIndicatorBubble()
                }
            }

            // Chips de sugerencia: solo antes del primer mensaje (arranque
            // de la conversacion) - despues dejan de ocupar espacio fijo
            // permanente en el footer.
            if (messages.isEmpty()) {
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
            }

            // Input - deshabilitado mientras se espera respuesta (evita
            // mandar una segunda pregunta antes de que llegue la primera).
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
 * Prompts alineados al tool `get_user_courses_current_term` del backend
 * (ciclo TECSUP: ago-dic = periodo 2). No hay REST de cursos; el chip
 * dispara el mismo `POST /query` que una pregunta escrita.
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