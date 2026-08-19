package pe.tecsup.paquitobot.data.remote

/**
 * Errores tipados de `canvas-mock`, mapeados 1:1 de los codigos HTTP reales
 * (ver `app/api/dependencies/auth.py` y `app/api/routers/users_self.py` del
 * backend). Sealed class separada de [PaquitoBotApiError] a proposito: son
 * dos backends distintos con dos modelos de auth distintos (X-Api-Key vs
 * JWT de Google) - no comparten semantica de error.
 */
sealed class CanvasMockApiError(message: String) : Exception(message) {
    /** No hay API key guardada ([pe.tecsup.paquitobot.data.remote.TokenProvider] devolvio `null`). */
    data object NotConnected : CanvasMockApiError("No hay una cuenta mock conectada")

    /** 401 - la API key es invalida o no existe. */
    data object InvalidApiKey : CanvasMockApiError("La clave mock no es valida")

    /** 403 - el estudiante no esta enrolado en ese curso. */
    data object NotEnrolled : CanvasMockApiError("No estas inscrito en ese curso")

    /** 404 - el recurso no existe (curso/asignacion). */
    data object NotFound : CanvasMockApiError("No se encontro el recurso")

    /** Sin conexion de red (timeout, servidor local no corriendo, etc.). */
    data class NetworkFailure(val cause2: Throwable) : CanvasMockApiError("Sin conexion con canvas-mock")

    /** Cualquier otro codigo/forma de respuesta no contemplada arriba. */
    data class Unknown(val statusCode: Int?, val detail: String?) :
        CanvasMockApiError(detail ?: "Error inesperado de canvas-mock")
}
