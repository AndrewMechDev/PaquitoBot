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

/**
 * URL base de `canvas-mock` (backend FastAPI separado de `paquitobot-rag`,
 * ver skill `canvas-mock-backend`). Mock de la API de Canvas mientras no
 * hay acceso a la API real - integracion intencionalmente TEMPORAL. No hay
 * deploy publico todavia - hay que correr `uvicorn` local (ver skill).
 *
 * IP LAN de la PC donde corre `uvicorn` (192.168.1.113, confirmada
 * 2026-08-19) - funciona para PROBAR EN DISPOSITIVO FISICO en la misma
 * red WiFi que la PC (el celular de prueba de este proyecto es fisico,
 * NO un emulador). Si usas el emulador de Android Studio en cambio, usa
 * `http://10.0.2.2:8811` (alias fijo de Android para el localhost del
 * host). Esta IP cambia si la PC se reconecta a otra red o el router
 * reasigna DHCP - si deja de andar, correr en PowerShell:
 * `(Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias
 * -notmatch 'Loopback|vEthernet|VMware'}).IPAddress` y actualizar aca.
 * El firewall de Windows tiene que permitir conexiones entrantes al
 * puerto 8811 (`uvicorn --port 8811`).
 */
const val CANVAS_MOCK_BASE_URL: String = "http://192.168.1.113:8811"

private const val CANVAS_MOCK_REQUEST_TIMEOUT_MILLIS: Long = 20_000

private object CanvasMockHttpClientHolder {
    val instance: HttpClient by lazy { createCanvasMockHttpClient() }
}

/** Un solo [HttpClient] compartido para todo lo que consulta `canvas-mock`. */
fun sharedCanvasMockHttpClient(): HttpClient = CanvasMockHttpClientHolder.instance

/**
 * Crea el [HttpClient] para consumir `canvas-mock`. Mismo patron que
 * `createPaquitoBotHttpClient()` (engine CIO, sin loggear headers - el
 * header `X-Api-Key` es una credencial y no debe aparecer en Logcat).
 */
fun createCanvasMockHttpClient(): HttpClient = HttpClient(CIO) {
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
        requestTimeoutMillis = CANVAS_MOCK_REQUEST_TIMEOUT_MILLIS
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = CANVAS_MOCK_REQUEST_TIMEOUT_MILLIS
    }

    install(Logging) {
        level = LogLevel.INFO
        logger = Logger.SIMPLE
    }

    defaultRequest {
        header("Content-Type", "application/json")
    }
}
