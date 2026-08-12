package pe.tecsup.paquitobot.data.repository

import pe.tecsup.paquitobot.data.remote.PaquitoBotApi
import pe.tecsup.paquitobot.domain.chat.ChatAnswer
import pe.tecsup.paquitobot.domain.chat.ChatRepository

/** Implementacion real de [ChatRepository]: consume `POST /query` via [PaquitoBotApi]. */
class RemoteChatRepository(
    private val api: PaquitoBotApi,
) : ChatRepository {
    override suspend fun askAssistant(question: String): Result<ChatAnswer> =
        api.query(question).map { response ->
            ChatAnswer(text = response.answer, route = response.route)
        }
}
