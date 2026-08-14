package pe.tecsup.paquitobot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.timeout
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import kotlinx.io.IOException
import pe.tecsup.paquitobot.data.remote.dto.LoginRequestDto
import pe.tecsup.paquitobot.data.remote.dto.LoginResponseDto
import pe.tecsup.paquitobot.data.remote.dto.QueryRequestDto
import pe.tecsup.paquitobot.data.remote.dto.QueryResponseDto

/**
 * Cliente delgado del backend `paquitobot-rag`: `GET /healthz` (wake-up),
 * `POST /auth/login`, `POST /auth/canvas/connect`, `POST /sync` y
 * `POST /query`.
 */
class PaquitoBotApi(
    private val httpClient: HttpClient,
    private val tokenProvider: TokenProvider = NoOpTokenProvider,
    private val baseUrl: String = PAQUITOBOT_BACKEND_BASE_URL,
) {
    /**
     * `GET /healthz` sin auth. Sirve para despertar el servicio en Render
     * antes del login. Cualquier respuesta HTTP cuenta como "esta despierto";
     * solo timeout/red se reportan como fallo.
     */
    suspend fun wakeUp(): Result<Unit> = runCatchingTransport {
        httpClient.get("$baseUrl/healthz") {
            timeout {
                requestTimeoutMillis = HEALTHZ_REQUEST_TIMEOUT_MILLIS
                socketTimeoutMillis = HEALTHZ_REQUEST_TIMEOUT_MILLIS
            }
        }
        Result.success(Unit)
    }

    /**
     * Llama a `POST /auth/login` con el `id_token` de Google Sign-In. NO
     * requiere `tokenProvider` - es el endpoint que consigue el JWT.
     */
    suspend fun login(idToken: String): Result<LoginResponseDto> = runCatchingTransport {
        val response = httpClient.post("$baseUrl/auth/login") {
            contentType(ContentType.Application.Json)
            timeout {
                requestTimeoutMillis = LOGIN_REQUEST_TIMEOUT_MILLIS
                socketTimeoutMillis = LOGIN_REQUEST_TIMEOUT_MILLIS
            }
            setBody(LoginRequestDto(idToken = idToken))
        }
        when (response.status) {
            HttpStatusCode.OK -> Result.success(response.body())
            HttpStatusCode.Unauthorized -> Result.failure(PaquitoBotApiError.GoogleSignInRejected)
            HttpStatusCode.BadGateway,
            HttpStatusCode.ServiceUnavailable,
            -> Result.failure(PaquitoBotApiError.ServiceUnavailable)
            else -> Result.failure(
                PaquitoBotApiError.Unknown(
                    statusCode = response.status.value,
                    detail = runCatching { response.bodyAsTextOrNull() }.getOrNull(),
                ),
            )
        }
    }

    suspend fun query(question: String, language: String? = null): Result<QueryResponseDto> {
        val token = tokenProvider.getToken()
            ?: return Result.failure(PaquitoBotApiError.NotAuthenticated)

        return runCatchingTransport {
            val response = httpClient.post("$baseUrl/query") {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer $token")
                timeout {
                    requestTimeoutMillis = QUERY_REQUEST_TIMEOUT_MILLIS
                    socketTimeoutMillis = QUERY_REQUEST_TIMEOUT_MILLIS
                }
                setBody(QueryRequestDto(question = question, language = language))
            }
            mapQueryResponse(response)
        }
    }

    suspend fun connectCanvas(canvasToken: String): Result<Unit> {
        val token = tokenProvider.getToken()
            ?: return Result.failure(PaquitoBotApiError.NotAuthenticated)

        return runCatchingTransport {
            val response = httpClient.post("$baseUrl/auth/canvas/connect") {
                header("Authorization", "Bearer $token")
                header("X-Canvas-Token", canvasToken)
                timeout {
                    requestTimeoutMillis = CANVAS_CONNECT_TIMEOUT_MILLIS
                    socketTimeoutMillis = CANVAS_CONNECT_TIMEOUT_MILLIS
                }
            }
            when (response.status) {
                HttpStatusCode.NoContent -> Result.success(Unit)

                HttpStatusCode.Unauthorized -> {
                    val body = runCatching { response.bodyAsTextOrNull() }.getOrNull().orEmpty()
                    if ("canvas_token_invalid" in body) {
                        Result.failure(PaquitoBotApiError.InvalidCanvasToken)
                    } else {
                        Result.failure(PaquitoBotApiError.InvalidSession)
                    }
                }

                HttpStatusCode.BadGateway,
                HttpStatusCode.ServiceUnavailable,
                -> Result.failure(PaquitoBotApiError.ServiceUnavailable)

                else -> Result.failure(
                    PaquitoBotApiError.Unknown(
                        statusCode = response.status.value,
                        detail = runCatching { response.bodyAsTextOrNull() }.getOrNull(),
                    ),
                )
            }
        }
    }

    suspend fun sync(): Result<Unit> {
        val token = tokenProvider.getToken()
            ?: return Result.failure(PaquitoBotApiError.NotAuthenticated)

        return runCatchingTransport {
            val response = httpClient.post("$baseUrl/sync") {
                header("Authorization", "Bearer $token")
                timeout {
                    requestTimeoutMillis = SYNC_REQUEST_TIMEOUT_MILLIS
                    socketTimeoutMillis = SYNC_REQUEST_TIMEOUT_MILLIS
                }
            }
            when (response.status) {
                HttpStatusCode.Accepted -> Result.success(Unit)

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

                else -> Result.failure(
                    PaquitoBotApiError.Unknown(
                        statusCode = response.status.value,
                        detail = runCatching { response.bodyAsTextOrNull() }.getOrNull(),
                    ),
                )
            }
        }
    }

    private suspend fun mapQueryResponse(response: HttpResponse): Result<QueryResponseDto> {
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

    private suspend fun <T> runCatchingTransport(block: suspend () -> Result<T>): Result<T> {
        return try {
            block()
        } catch (_: HttpRequestTimeoutException) {
            Result.failure(PaquitoBotApiError.RequestTimeout)
        } catch (_: ConnectTimeoutException) {
            Result.failure(PaquitoBotApiError.RequestTimeout)
        } catch (_: SocketTimeoutException) {
            Result.failure(PaquitoBotApiError.RequestTimeout)
        } catch (exc: IOException) {
            Result.failure(PaquitoBotApiError.NetworkFailure(exc))
        }
    }

    private suspend fun HttpResponse.bodyAsTextOrNull(): String? =
        runCatching { body<String>() }.getOrNull()
}
