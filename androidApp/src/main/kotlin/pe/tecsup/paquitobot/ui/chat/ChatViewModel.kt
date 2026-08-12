package pe.tecsup.paquitobot.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.data.remote.NoOpTokenProvider
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
 * ViewModel de la pantalla de Chat - primera vez que se introduce
 * ViewModel en el proyecto (hasta ahora toda pantalla era UI con datos
 * hardcoded, siguiendo la skill `architecture-paquitobot`: "ViewModel se
 * introduce cuando se conecte el backend"). Justo ese momento es este.
 *
 * Alcance confirmado con el usuario (2026-08-11): SOLO esta pantalla
 * consume datos reales por ahora; Home sigue con mocks.
 *
 * El login del backend esta en standby (lo va a crear el compañero de
 * backend). Mientras tanto, [NoOpTokenProvider] hace que
 * [ChatRepository.askAssistant] devuelva siempre
 * [PaquitoBotApiError.NotAuthenticated] sin llegar a pegarle a la red -
 * el usuario ve un aviso de sistema claro en vez de un error crudo. El
 * dia que exista login real, solo hay que cambiar el [TokenProvider]
 * inyectado aca abajo; nada mas de este archivo cambia.
 */
class ChatViewModel(
    private val repository: ChatRepository = createDefaultChatRepository(NoOpTokenProvider),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    fun sendMessage(text: String) {
        val question = text.trim()
        if (question.isEmpty() || _uiState.value.isSending) return

        appendMessage(ChatMessage(role = MessageRole.User, body = question))
        _uiState.update { it.copy(isSending = true) }

        viewModelScope.launch {
            repository.askAssistant(question)
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
        -> "Todavía no iniciaste sesión. Conectá tu cuenta para que pueda ayudarte."

        is PaquitoBotApiError.CanvasNotConnected ->
            "Conectá tu cuenta de Canvas para que pueda responder con tus datos."

        is PaquitoBotApiError.RateLimited ->
            "Estoy respondiendo muchas preguntas, esperá un momento y volvé a intentar."

        is PaquitoBotApiError.ServiceUnavailable ->
            "El servicio no está disponible en este momento. Probá de nuevo en un rato."

        is PaquitoBotApiError.NetworkFailure ->
            "Sin conexión. Revisá tu internet e intentá de nuevo."

        else -> "Algo salió mal. Intentá de nuevo."
    }
}
