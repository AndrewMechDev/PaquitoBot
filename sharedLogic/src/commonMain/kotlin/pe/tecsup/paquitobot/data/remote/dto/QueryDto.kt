package pe.tecsup.paquitobot.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payload de `POST /query` (backend `paquitobot-rag`).
 *
 * `language` es opcional - el backend auto-detecta el idioma de la
 * pregunta si se omite. El backend usa `extra="forbid"` en su Pydantic
 * schema: cualquier campo fuera de estos dos hace que rechace la request
 * con 422, así que este DTO nunca debe crecer con campos extra (ej.
 * `tenant_id`) - el tenant sale del JWT, no del body.
 */
@Serializable
data class QueryRequestDto(
    val question: String,
    val language: String? = null,
)

/**
 * Respuesta de `POST /query`. Contrato estable documentado en
 * `app/controllers/query.py` del backend: `{answer, lang, route, correlation_id}`.
 */
@Serializable
data class QueryResponseDto(
    val answer: String,
    val lang: String,
    val route: String,
    @SerialName("correlation_id")
    val correlationId: String,
)
