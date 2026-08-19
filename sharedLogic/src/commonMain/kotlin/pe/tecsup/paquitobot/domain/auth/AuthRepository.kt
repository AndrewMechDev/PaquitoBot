package pe.tecsup.paquitobot.domain.auth

/** Sesion resultante de un login exitoso, ya mapeada a un modelo de dominio. */
data class AuthSession(
    val accessToken: String,
    val expiresInSeconds: Int,
    val sub: String,
    val email: String?,
)

/** Puerto de dominio para el login con Google. */
interface AuthRepository {
    /**
     * Despierta el backend (`HEAD /healthz`) sin auth. Best-effort: un fallo
     * no bloquea el flujo de Google, solo deja el servicio frio.
     */
    suspend fun wakeBackend(): Result<Unit>

    /** Cambia un `id_token` de Google Sign-In por una [AuthSession] del backend. */
    suspend fun loginWithGoogle(idToken: String): Result<AuthSession>
}
