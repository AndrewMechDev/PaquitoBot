package pe.tecsup.paquitobot.domain.chat

/** Respuesta del asistente, ya mapeada a un modelo de dominio (sin nada de Ktor/DTOs). */
data class ChatAnswer(
    val text: String,
    val route: String,
)

/**
 * Puerto de dominio para el chat con el asistente. La UI (ViewModel)
 * depende de esta interfaz, nunca de Ktor ni de los DTOs directamente -
 * asi el dia que haya una implementacion con cache/offline, o un fake
 * para tests, no hace falta tocar el ViewModel.
 */
interface ChatRepository {
    /**
     * Manda una pregunta al asistente y devuelve la respuesta.
     * Nunca lanza: los errores vienen encapsulados en [Result].
     */
    suspend fun askAssistant(question: String): Result<ChatAnswer>
}
