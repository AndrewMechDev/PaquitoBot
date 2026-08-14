package pe.tecsup.paquitobot.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.SecureTokenStore
import pe.tecsup.paquitobot.data.remote.PaquitoBotApiError
import pe.tecsup.paquitobot.data.repository.createDefaultChatRepository
import pe.tecsup.paquitobot.domain.chat.ChatRepository
import pe.tecsup.paquitobot.ui.components.ChatMessage
import pe.tecsup.paquitobot.ui.components.MessageRole

/** Estado de la pantalla de Chat, expuesto por [ChatViewModel]. */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
)

/**
 * ViewModel de la pantalla de Chat - primer ViewModel real del proyecto
 * (hasta ahora toda pantalla era UI con datos hardcoded, siguiendo la
 * skill `architecture-paquitobot`: "ViewModel se introduce cuando se
 * conecte el backend"). Justo ese momento es este.
 *
 * Alcance confirmado con el usuario (2026-08-11): SOLO esta pantalla
 * consume datos reales por ahora; Home sigue con mocks.
 *
 * Iteracion 2026-08-13 (gates a nivel app): el login con Google y la
 * conexion con Canvas se movieron a `SessionViewModel`/`CanvasConnectViewModel`,
 * evaluados ANTES de que `MainActivity` siquiera muestre esta pantalla -
 * `ChatScreen` ya no necesita manejar `isAuthenticated`. [tokenStore] se
 * sigue pasando por constructor porque `chatRepository` lo necesita para
 * autenticar cada request (`Authorization: Bearer <jwt>`).
 */
class ChatViewModel(
    private val tokenStore: SecureTokenStore,
    private val chatRepository: ChatRepository = createDefaultChatRepository(tokenStore),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        val question = text.trim()
        if (question.isEmpty() || _uiState.value.isSending) return

        appendMessage(ChatMessage(role = MessageRole.User, body = question))
        _uiState.update { it.copy(isSending = true) }

        viewModelScope.launch {
            chatRepository.askAssistant(question)
                .onSuccess { answer ->
                    appendMessage(ChatMessage(role = MessageRole.Bot, body = answer.text))
                }
                .onFailure { error ->
                    appendMessage(
                        ChatMessage(role = MessageRole.System, body = systemMessageFor(error)),
                    )
                }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    private fun appendMessage(message: ChatMessage) {
        _uiState.update { it.copy(messages = it.messages + message) }
    }

    /** Traduce el error tipado del backend a un aviso de chat entendible. */
    private fun systemMessageFor(error: Throwable): String = when (error) {
        is PaquitoBotApiError.NotAuthenticated,
        is PaquitoBotApiError.InvalidSession,
        -> "Tu sesión expiró. Volvé a conectar con Google."

        is PaquitoBotApiError.CanvasNotConnected ->
            "Conectá tu cuenta de Canvas para que pueda responder con tus datos."

        is PaquitoBotApiError.RateLimited ->
            "Estoy respondiendo muchas preguntas, esperá un momento y volvé a intentar."

        is PaquitoBotApiError.ServiceUnavailable ->
            "El servicio no está disponible en este momento. Probá de nuevo en un rato."

        is PaquitoBotApiError.RequestTimeout ->
            "Paquito tardó más de lo normal en responder. El servidor puede estar ocupado; intentá de nuevo."

        is PaquitoBotApiError.NetworkFailure ->
            "Sin conexión. Revisá tu internet e intentá de nuevo."

        else -> "Algo salió mal. Intentá de nuevo."
    }
}
