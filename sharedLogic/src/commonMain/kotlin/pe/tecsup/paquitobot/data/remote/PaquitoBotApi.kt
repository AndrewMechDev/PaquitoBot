package pe.tecsup.paquitobot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.ContentType
import kotlinx.io.IOException
import pe.tecsup.paquitobot.data.remote.dto.QueryRequestDto
import pe.tecsup.paquitobot.data.remote.dto.QueryResponseDto

/**
 * Cliente delgado del backend `paquitobot-rag`: solo `POST /query` por
 * ahora - es el unico endpoint que la vista de Chat necesita (alcance
 * confirmado con el usuario: por el momento solo esa vista consume datos
 * reales; `/sync` y `/auth/canvas/connect` quedan para cuando exista el
 * login real del backend).
 */
class PaquitoBotApi(
    private val httpClient: HttpClient,
    private val tokenProvider: TokenProvider,
    private val baseUrl: String = PAQUITOBOT_BACKEND_BASE_URL,
) {
    /**
     * Llama a `POST /query`. Devuelve [Result] en vez de lanzar directo:
     * el caller (repository) decide como convertir un [PaquitoBotApiError]
     * en un mensaje de chat, sin try/catch anidados.
     */
    suspend fun query(question: String, language: String? = null): Result<QueryResponseDto> {
        val token = tokenProvider.getToken()
            ?: return Result.failure(PaquitoBotApiError.NotAuthenticated)

        return try {
            val response = httpClient.post("$baseUrl/query") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                setBody(QueryRequestDto(question = question, language = language))
            }
            mapResponse(response)
        } catch (exc: HttpRequestTimeoutException) {
            Result.failure(PaquitoBotApiError.NetworkFailure(exc))
        } catch (exc: IOException) {
            Result.failure(PaquitoBotApiError.NetworkFailure(exc))
        }
    }

    private suspend fun mapResponse(response: HttpResponse): Result<QueryResponseDto> {
        return when (response.status) {
            HttpStatusCode.OK ->
                Result.success(response.body())

            HttpStatusCode.Unauthorized ->
                Result.failure(PaquitoBotApiError.InvalidSession)

            HttpStatusCode.Forbidden ->
                Result.failure(PaquitoBotApiError.CanvasNotConnected)

            HttpStatusCode.TooManyRequests -> {
                val retryAfter = response.headers["Retry-After"]?.toIntOrNull()
                Result.failure(PaquitoBotApiError.RateLimited(retryAfter))
            }

            HttpStatusCode.BadGateway,
            HttpStatusCode.ServiceUnavailable,
            -> Result.failure(PaquitoBotApiError.ServiceUnavailable)

            else ->
                Result.failure(
                    PaquitoBotApiError.Unknown(
                        statusCode = response.status.value,
                        detail = runCatching { response.bodyAsTextOrNull() }.getOrNull(),
                    ),
                )
        }
    }

    private suspend fun HttpResponse.bodyAsTextOrNull(): String? =
        runCatching { body<String>() }.getOrNull()
}
