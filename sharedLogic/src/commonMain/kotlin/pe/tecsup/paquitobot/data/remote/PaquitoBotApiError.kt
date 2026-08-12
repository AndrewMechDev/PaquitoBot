package pe.tecsup.paquitobot.data.remote

/**
 * Errores tipados de `paquitobot-rag`, mapeados 1:1 de los codigos HTTP
 * reales que devuelve el backend (ver `app/controllers/query.py`,
 * `app/controllers/sync.py`, `app/core/deps.py`). El caller (repository/
 * ViewModel) decide el mensaje visible por tipo, no por codigo HTTP crudo.
 */
sealed class PaquitoBotApiError(message: String) : Exception(message) {
    /** No hay JWT disponible ([TokenProvider] devolvio `null`) - ni se llego a mandar la request. */
    data object NotAuthenticated : PaquitoBotApiError("No hay sesion activa")

    /** 401 - el JWT es invalido o expiro. */
    data object InvalidSession : PaquitoBotApiError("La sesion expiro o es invalida")

    /** 403 `tenant_credentials_missing` - el usuario nunca conecto su cuenta de Canvas. */
    data object CanvasNotConnected : PaquitoBotApiError("La cuenta de Canvas no esta conectada")

    /** 429 - rate limit (`sync_throttled` o limite general). */
    data class RateLimited(val retryAfterSeconds: Int?) : PaquitoBotApiError("Demasiadas solicitudes")

    /** 502/503 - Canvas o el backend no responden (`canvas_unavailable`, `rag_routes_disabled`). */
    data object ServiceUnavailable : PaquitoBotApiError("El servicio no esta disponible")

    /** Sin conexion de red (timeout, DNS, host inalcanzable). */
    data class NetworkFailure(val cause2: Throwable) : PaquitoBotApiError("Sin conexion")

    /** Cualquier otro codigo/forma de respuesta no contemplada arriba. */
    data class Unknown(val statusCode: Int?, val detail: String?) :
        PaquitoBotApiError(detail ?: "Error inesperado del backend")
}
