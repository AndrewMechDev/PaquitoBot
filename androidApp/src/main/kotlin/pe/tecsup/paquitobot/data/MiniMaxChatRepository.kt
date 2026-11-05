package pe.tecsup.paquitobot.data

import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import pe.tecsup.paquitobot.domain.chat.ChatAnswer
import pe.tecsup.paquitobot.domain.chat.ChatRepository
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.auth.authorizedAcademicUserId

/** Cliente Android de MiniMax para el prototipo debug. La clave se extrae del APK. */
class MiniMaxChatRepository(private val apiKey: String, private val academic: AcademicRepository,
    private val owner: String, private val teacherContext: String? = null) : ChatRepository {
    private val history = mutableListOf<JSONObject>()

    override suspend fun askAssistant(question: String): Result<ChatAnswer> = withContext(Dispatchers.IO) {
        runCatching {
            if (apiKey.isBlank()) {
                error("Falta minimax.apiKey en local.properties. Agrégala y recompila la app.")
            }
            check(authorizedAcademicUserId() == owner) { "La sesión académica cambió. Abre el chat nuevamente." }
            val isTeacher = teacherContext != null
            val assistantContext = teacherContext ?: AcademicContextSnapshot.from(academic).toString()
            val systemInstruction = if (isTeacher) TEACHER_SYSTEM_INSTRUCTION else SYSTEM_INSTRUCTION
            val contextLabel = if (isTeacher) "Datos académicos disponibles para el docente" else "Datos académicos de esta cuenta (JSON)"

            val messages = JSONArray().put(message("system", "$systemInstruction\n\n$contextLabel:\n$assistantContext"))
            history.forEach { messages.put(it) }
            messages.put(message("user", question))

            val request = JSONObject()
                .put("model", MODEL)
                .put("messages", messages)
                .put("thinking", JSONObject().put("type", "disabled"))
                .put("max_completion_tokens", 1024)

            check(authorizedAcademicUserId() == owner) { "La sesión académica cambió. Abre el chat nuevamente." }
            val response = postCompletion(request)
            val apiStatus = response.optJSONObject("base_resp")?.optInt("status_code", 0) ?: 0
            if (apiStatus != 0) error("MiniMax rechazó la solicitud (código $apiStatus). Inténtalo de nuevo.")
            val answer = response.optJSONArray("choices")
                ?.optJSONObject(0)
                ?.optJSONObject("message")
                ?.optString("content")
                ?.trim()
                .orEmpty()
            if (answer.isBlank()) error("MiniMax no devolvió una respuesta de texto. Inténtalo de nuevo.")

            history += message("user", question)
            history += message("assistant", answer)
            if (history.size > MAX_HISTORY_MESSAGES) {
                history.subList(0, history.size - MAX_HISTORY_MESSAGES).clear()
            }
            ChatAnswer(text = answer, route = "minimax")
        }
    }

    private fun postCompletion(request: JSONObject): JSONObject {
        val connection = (URL(ENDPOINT).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 15_000
            readTimeout = 90_000
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            setRequestProperty("Authorization", "Bearer $apiKey")
        }
        try {
            connection.outputStream.use { it.write(request.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val body = (if (status in 200..299) connection.inputStream else connection.errorStream)
                ?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (status !in 200..299) {
                val explanation = when (status) {
                    400 -> "MiniMax rechazó la solicitud. Revisa la configuración del modelo."
                    401, 403 -> "La clave de MiniMax no es válida o no tiene acceso al modelo."
                    429 -> "MiniMax alcanzó el límite de solicitudes. Inténtalo más tarde."
                    else -> "MiniMax no respondió (HTTP $status). Inténtalo de nuevo."
                }
                throw IOException(explanation)
            }
            return JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }

    private fun message(role: String, content: String) = JSONObject()
        .put("role", role)
        .put("content", content)

    private companion object {
        const val ENDPOINT = "https://api.minimax.io/v1/chat/completions"
        const val MODEL = "MiniMax-M3"
        const val MAX_HISTORY_MESSAGES = 12
        const val SYSTEM_INSTRUCTION ="""
            Eres Paquito, asistente académico de PaquitoBot. Responde en español, de forma breve y clara. Evita el uso de tablas

            Solo atiende consultas sobre cursos, notas, tareas, horarios, asistencias, avisos y funcionalidades del sistema académico.

            Usa exclusivamente los datos académicos de demostración de esta cuenta. No inventes información. Para fechas relativas, como «hoy», usa fecha_actual del JSON.

            Rechaza cualquier consulta ajena al sistema académico, incluso preguntas básicas de matemáticas, programación, traducción, cultura general o temas generales. No respondas parcialmente ni indirectamente a esas consultas.

            Si la consulta está fuera de alcance, responde únicamente: «Solo puedo ayudarte con consultas relacionadas con tu sistema académico de PaquitoBot».
            """
        const val TEACHER_SYSTEM_INSTRUCTION = """
            Eres Paquito, asistente académico para docentes de PaquitoBot. Responde en español, de forma breve, clara y sin tablas.

            Atiende solo consultas docentes sobre sus cursos, alumnos, notas, evaluaciones, promedios, laboratorios, asistencias y seguimiento académico.

            Usa exclusivamente el contexto académico recibido. No inventes alumnos, secciones, notas, asistencias ni asignaciones. Si el dato solicitado no aparece, indícalo claramente. No mezcles datos de carreras o cursos diferentes.

            La nota mínima aprobatoria es 13. Las notas tienen el mismo peso. Un laboratorio completo cuenta como una nota, promediando sus subnotas; un laboratorio incompleto se muestra como parcial y no entra al promedio. Un promedio bajo 13 en un curso incompleto es riesgo actual parcial, no desaprobación final.

            La advertencia de asistencia empieza en 20 %. DI se activa solo por encima de 30 %. Calcula por alumno y curso usando clases realizadas; las tardanzas se muestran aparte y no cuentan como faltas.

            Para priorizar seguimiento, presenta primero DI, luego advertencia y después riesgo por notas. Cuenta alumnos únicos en los resúmenes e indica cuando una persona tiene señales en cursos distintos.

            El contexto contiene la información disponible a la semana 11. Responde sobre esa información sin mencionar que es una prueba o demostración, salvo que el usuario lo pregunte.

            Rechaza consultas ajenas al sistema académico y responde únicamente: «Solo puedo ayudarte con consultas docentes relacionadas con cursos, alumnos y seguimiento académico de PaquitoBot».
            """
    }
}
