package pe.tecsup.paquitobot.data.repository

import pe.tecsup.paquitobot.domain.chat.ChatAnswer
import pe.tecsup.paquitobot.domain.chat.ChatRepository

/** Respuestas locales de demostración hasta que exista el nuevo backend. */
class MockChatRepository : ChatRepository {
    override suspend fun askAssistant(question: String): Result<ChatAnswer> {
        val normalized = question.trim().lowercase()
        val answer = when {
            listOf("curso", "nota", "promedio", "calificación").any(normalized::contains) ->
                "Por ahora ves cursos y notas de ejemplo en la pestaña Cursos. Cuando conectemos tus datos reales, podré ayudarte con tu progreso."
            listOf("horario", "clase", "falta", "asistencia").any(normalized::contains) ->
                "Puedes consultar el horario de ejemplo en la pestaña Horarios. Todavía no tengo acceso a tu horario real."
            listOf("tarea", "entrega", "plazo", "pendiente").any(normalized::contains) ->
                "En Inicio encontrarás entregas de ejemplo. Cuando conectemos el nuevo backend, aparecerán tus pendientes reales."
            else ->
                "Soy Paquito en modo demostración. Puedo orientarte sobre cursos, horarios y entregas usando los datos de ejemplo de la app."
        }
        return Result.success(ChatAnswer(text = answer, route = "mock"))
    }
}
