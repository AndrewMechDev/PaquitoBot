package pe.tecsup.paquitobot.data.repository

import pe.tecsup.paquitobot.data.remote.PaquitoBotApi
import pe.tecsup.paquitobot.data.remote.sharedPaquitoBotHttpClient
import pe.tecsup.paquitobot.domain.auth.AuthRepository
import pe.tecsup.paquitobot.domain.auth.AuthSession

/** Implementacion real de [AuthRepository]: consume `POST /auth/login` via [PaquitoBotApi]. */
class RemoteAuthRepository(
    private val api: PaquitoBotApi,
) : AuthRepository {
    override suspend fun wakeBackend(): Result<Unit> = api.wakeUp()

    override suspend fun loginWithGoogle(idToken: String): Result<AuthSession> =
        api.login(idToken).map { response ->
            AuthSession(
                accessToken = response.accessToken,
                expiresInSeconds = response.expiresIn,
                sub = response.sub,
                email = response.email,
            )
        }
}

/**
 * Arma el [AuthRepository] real. Como `POST /auth/login` no requiere JWT
 * (es el endpoint que lo consigue), no hace falta pasarle ningun
 * [pe.tecsup.paquitobot.data.remote.TokenProvider].
 */
fun createDefaultAuthRepository(): AuthRepository =
    RemoteAuthRepository(api = PaquitoBotApi(httpClient = sharedPaquitoBotHttpClient()))
