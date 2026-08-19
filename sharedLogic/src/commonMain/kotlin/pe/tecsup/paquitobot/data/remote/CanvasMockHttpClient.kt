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
 * IP LAN de la PC donde corre `uvicorn` - funciona para PROBAR EN
 * DISPOSITIVO FISICO en la misma red que la PC (el celular de prueba de
 * este proyecto es fisico, NO un emulador). Si usas el emulador de
 * Android Studio en cambio, usa `http://10.0.2.2:8811` (alias fijo de
 * Android para el localhost del host).
 *
 * ⚠️ ESTA IP CAMBIA cada vez que la PC se reconecta a OTRA red (ej. WiFi
 * de casa vs. hotspot del celular - confirmado 2026-08-19: paso de
 * `192.168.1.113` a `192.168.82.74` al cambiar a hotspot). Si canvas-mock
 * deja de responder ("Sin conexion con canvas-mock"), lo primero a
 * revisar es esto, ANTES de sospechar del firewall. Para obtener la IP
 * actual, correr en PowerShell:
 * `(Get-NetIPAddress -AddressFamily IPv4 | Where-Object {$_.InterfaceAlias
 * -notmatch 'Loopback|vEthernet|VMware'}).IPAddress` y actualizar aca -
 * o mejor, `ipconfig /all` y usar la IPv4 del adaptador que efectivamente
 * tiene "Puerta de enlace predeterminada" (el que esta realmente
 * conectado). El firewall de Windows tambien tiene que permitir
 * conexiones entrantes al puerto 8811 (`uvicorn --port 8811`), pero eso
 * es la SEGUNDA cosa a revisar, no la primera.
 */
const val CANVAS_MOCK_BASE_URL: String = "http://192.168.82.74:8811"

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
