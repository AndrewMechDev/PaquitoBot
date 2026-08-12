package pe.tecsup.paquitobot.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/** URL base del backend `paquitobot-rag` (FastAPI, deployado en Render). */
const val PAQUITOBOT_BACKEND_BASE_URL: String = "https://paquitobot-rag.onrender.com"

/**
 * Crea el [HttpClient] compartido para consumir `paquitobot-rag`.
 *
 * Engine CIO: multiplataforma (JVM/Android + Kotlin/Native), pensado para
 * que Android e iOS compartan esta misma clase - ver `architecture-paquitobot`
 * skill. NOTA: verificado que compila en Android (JVM); NO se pudo
 * verificar la compilacion real para iOS en esta sesion (se desarrollo en
 * Windows, sin Xcode - Kotlin/Native para iOS no compila fuera de macOS).
 * Si tu compañero de iOS encuentra problemas con CIO en Darwin, la
 * alternativa estandar de Ktor para Apple es el engine `Darwin`
 * (`io.ktor:ktor-client-darwin`, basado en `NSURLSession`) - cambiar el
 * `HttpClient(CIO)` de abajo por un factory `expect/actual` por plataforma
 * si hace falta.
 *
 * IMPORTANTE: el logger NUNCA loguea headers (por eso `LogLevel.INFO`, no
 * `HEADERS` ni `ALL`) - el header `Authorization` lleva el JWT del backend
 * y no debe aparecer en ningun log, ni de este cliente ni de Logcat.
 */
fun createPaquitoBotHttpClient(): HttpClient = HttpClient(CIO) {
    expectSuccess = false

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            },
        )
    }

    install(HttpTimeout) {
        requestTimeoutMillis = 20_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 20_000
    }

    install(Logging) {
        level = LogLevel.INFO
    }

    defaultRequest {
        header("Content-Type", "application/json")
    }
}
