package pe.tecsup.paquitobot.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.domain.chat.ChatRepository
import pe.tecsup.paquitobot.ui.components.ChatMessage
import pe.tecsup.paquitobot.ui.components.MessageRole

/** Estado de la pantalla de Chat, expuesto por [ChatViewModel]. */
data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isSending: Boolean = false,
)

/**
 * Clerk protege la app; MiniMax responde con los mismos datos mock que
 * muestran las pantallas.
 */
class ChatViewModel(
    private val chatRepository: ChatRepository,
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
                        ChatMessage(
                            role = MessageRole.System,
                            body = error.message ?: "No pude responder. Inténtalo de nuevo.",
                        ),
                    )
                }
            _uiState.update { it.copy(isSending = false) }
        }
    }

    private fun appendMessage(message: ChatMessage) {
        _uiState.update { it.copy(messages = it.messages + message) }
    }

}
