package pe.tecsup.paquitobot.data.remote

/**
 * Fuente del JWT del backend (`Authorization: Bearer <jwt>`).
 *
 * El backend `paquitobot-rag` NO tiene todavia un endpoint que emita este
 * JWT (login/dev-login queda del lado del compañero de backend, en
 * standby a 2026-08-11). Esta interfaz existe justo para que el resto del
 * cliente (API, repository, ViewModel) no dependa de CÓMO se consigue el
 * token - el dia que exista login real, se implementa esta interfaz una
 * sola vez y nada mas cambia.
 *
 * [getToken] devuelve `null` cuando no hay sesion (usuario no conectado
 * o token no disponible todavia) - el caller debe tratar `null` como
 * "no autenticado" y NO intentar la request.
 */
interface TokenProvider {
    suspend fun getToken(): String?
}

/**
 * Implementacion "sin sesion": siempre devuelve `null`.
 *
 * Placeholder mientras no hay login real - permite que `RemoteChatRepository`
 * y `ChatViewModel` ya esten conectados end-to-end (compilando, con manejo
 * de error correcto) sin necesitar ningun secreto ni JWT hardcodeado en el
 * codigo fuente. Reemplazar por la implementacion real cuando el backend
 * tenga login.
 */
object NoOpTokenProvider : TokenProvider {
    override suspend fun getToken(): String? = null
}
