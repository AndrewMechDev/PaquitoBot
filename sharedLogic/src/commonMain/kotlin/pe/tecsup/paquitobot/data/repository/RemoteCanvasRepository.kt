package pe.tecsup.paquitobot.data.repository

import pe.tecsup.paquitobot.data.remote.PaquitoBotApi
import pe.tecsup.paquitobot.data.remote.TokenProvider
import pe.tecsup.paquitobot.data.remote.sharedPaquitoBotHttpClient
import pe.tecsup.paquitobot.domain.canvas.CanvasRepository

/** Implementacion real de [CanvasRepository]: consume `POST /auth/canvas/connect` via [PaquitoBotApi]. */
class RemoteCanvasRepository(
    private val api: PaquitoBotApi,
) : CanvasRepository {
    override suspend fun connect(canvasToken: String): Result<Unit> =
        api.connectCanvas(canvasToken)

    override suspend fun sync(): Result<Unit> =
        api.sync()
}

/**
 * Arma el [CanvasRepository] real. A diferencia de `createDefaultAuthRepository()`,
 * este endpoint SI requiere el JWT del backend (recien conseguido via login) -
 * por eso [tokenProvider] es obligatorio, no tiene default `NoOpTokenProvider`.
 */
fun createDefaultCanvasRepository(tokenProvider: TokenProvider): CanvasRepository =
    RemoteCanvasRepository(
        api = PaquitoBotApi(
            httpClient = sharedPaquitoBotHttpClient(),
            tokenProvider = tokenProvider,
        ),
    )
