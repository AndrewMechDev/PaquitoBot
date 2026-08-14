package pe.tecsup.paquitobot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** URL base del backend `paquitobot-rag` (FastAPI, deployado en Render). */
const val PAQUITOBOT_BACKEND_BASE_URL: String = "https://paquitobot-rag.onrender.com"

/**
 * Timeouts por operacion (ms). Render free duerme el servicio: un wake-up
 * suele tardar 30-90s. El default anterior (20s) cortaba login/query y la
 * UI lo mostraba como "Google rechazó" / "sin conexion".
 */
const val LOGIN_REQUEST_TIMEOUT_MILLIS: Long = 90_000
const val HEALTHZ_REQUEST_TIMEOUT_MILLIS: Long = 90_000
const val SYNC_REQUEST_TIMEOUT_MILLIS: Long = 90_000
const val QUERY_REQUEST_TIMEOUT_MILLIS: Long = 120_000
const val CANVAS_CONNECT_TIMEOUT_MILLIS: Long = 30_000

private object PaquitoBotHttpClientHolder {
    val instance: HttpClient by lazy { createPaquitoBotHttpClient() }
}

/**
 * Un solo [HttpClient] para Auth, Canvas y Chat. Cada factory creaba el
 * suyo y pagaba un handshake TLS distinto por pantalla.
 */
fun sharedPaquitoBotHttpClient(): HttpClient = PaquitoBotHttpClientHolder.instance

/**
 * Crea el [HttpClient] para consumir `paquitobot-rag`.
 *
 * Engine CIO: multiplataforma (JVM/Android + Kotlin/Native). Verificado
 * en Android; iOS no se pudo compilar en Windows. Si CIO falla en Darwin,
 * pasar a engine `Darwin` via expect/actual.
 *
 * El logger NUNCA loguea headers (`LogLevel.INFO`) - `Authorization` no
 * debe aparecer en Logcat. `Logger.SIMPLE` es obligatorio: sin el, SLF4J
 * deja el logger mudo en Android.
 */
fun createPaquitoBotHttpClient(): HttpClient = HttpClient(CIO) {
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = false
            },
        )
    }

    install(HttpTimeout) {
        requestTimeoutMillis = QUERY_REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = 15_000
        socketTimeoutMillis = QUERY_REQUEST_TIMEOUT_MILLIS
    }

    install(Logging) {
        level = LogLevel.INFO
        logger = Logger.SIMPLE
    }

    defaultRequest {
        header("Content-Type", "application/json")
    }
}
