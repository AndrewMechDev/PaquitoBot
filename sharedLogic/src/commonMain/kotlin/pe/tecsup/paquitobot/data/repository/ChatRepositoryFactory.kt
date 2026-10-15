package pe.tecsup.paquitobot.data.repository

import pe.tecsup.paquitobot.domain.chat.ChatRepository

/**
 * Usa datos locales hasta que se implemente el nuevo backend.
 */
fun createDefaultChatRepository(): ChatRepository = MockChatRepository()
