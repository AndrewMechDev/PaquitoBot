package pe.tecsup.paquitobot.data.remote

/**
 * Errores tipados de `paquitobot-rag`, mapeados 1:1 de los codigos HTTP
 * reales que devuelve el backend. El caller (repository/ViewModel) decide
 * el mensaje visible por tipo, no por codigo HTTP crudo.
 */
sealed class PaquitoBotApiError(message: String) : Exception(message) {
    /** No hay JWT disponible ([TokenProvider] devolvio `null`) - ni se llego a mandar la request. */
    data object NotAuthenticated : PaquitoBotApiError("No hay sesion activa")

    /** 401 - el JWT es invalido o expiro. */
    data object InvalidSession : PaquitoBotApiError("La sesion expiro o es invalida")

    /** 401 en `POST /auth/login` - Google rechazo el id_token (expirado, audience incorrecta, etc.). */
    data object GoogleSignInRejected : PaquitoBotApiError("Google rechazo el inicio de sesion")

    /** 403 `tenant_credentials_missing` - el usuario nunca conecto su cuenta de Canvas. */
    data object CanvasNotConnected : PaquitoBotApiError("La cuenta de Canvas no esta conectada")

    /** 401 `canvas_token_invalid` en `POST /auth/canvas/connect` - Canvas rechazo el token. */
    data object InvalidCanvasToken : PaquitoBotApiError("El token de Canvas es invalido")

    /** 429 - rate limit (`sync_throttled` o limite general). */
    data class RateLimited(val retryAfterSeconds: Int?) : PaquitoBotApiError("Demasiadas solicitudes")

    /** 502/503 - Canvas o el backend no responden (`canvas_unavailable`, `rag_routes_disabled`). */
    data object ServiceUnavailable : PaquitoBotApiError("El servicio no esta disponible")

    /**
     * El request excedio el timeout (cold start de Render, RAG lento, sync
     * contra Canvas). Distinto de [NetworkFailure]: hay red, el servidor no
     * termino a tiempo.
     */
    data object RequestTimeout : PaquitoBotApiError("El servidor tardo demasiado en responder")

    /** Sin conexion de red (DNS, host inalcanzable, IO). */
    data class NetworkFailure(val cause2: Throwable) : PaquitoBotApiError("Sin conexion")

    /** Cualquier otro codigo/forma de respuesta no contemplada arriba. */
    data class Unknown(val statusCode: Int?, val detail: String?) :
        PaquitoBotApiError(detail ?: "Error inesperado del backend")
}
