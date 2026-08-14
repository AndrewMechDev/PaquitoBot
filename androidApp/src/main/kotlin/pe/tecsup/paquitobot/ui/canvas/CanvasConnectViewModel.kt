package pe.tecsup.paquitobot.ui.canvas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.SecureTokenStore
import pe.tecsup.paquitobot.data.remote.PaquitoBotApiError
import pe.tecsup.paquitobot.data.repository.createDefaultCanvasRepository
import pe.tecsup.paquitobot.domain.canvas.CanvasRepository

/** Estado del segundo gate de la app: conectar el token de Canvas del estudiante. */
data class CanvasConnectUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val errorMessage: String? = null,
    val syncWarning: String? = null,
)

/**
 * Segundo gate de la app (2026-08-13), despues del login con Google.
 * Solo v1/temporal: hoy no hay OAuth real de Canvas, el estudiante pega su
 * token de acceso manualmente (ver `requerimientos/BACKEND_INTEGRATION.md`).
 *
 * Iteracion 2026-08-13 (sync manual): un `connect()` exitoso NO alcanza
 * para que `/query` tenga datos - el backend sincroniza con Canvas en
 * background por scheduler, pero recien conectado el token no hay
 * garantia de que ya haya corrido (confirmado probando: Postman con datos
 * ya sincronizados de antes respondia bien, la app recien conectada no).
 * Por eso [connect] dispara `POST /sync` inmediatamente despues de un
 * `connect` exitoso, antes de marcar el gate como pasado.
 */
class CanvasConnectViewModel(
    private val tokenStore: SecureTokenStore,
    private val canvasRepository: CanvasRepository = createDefaultCanvasRepository(tokenStore),
) : ViewModel() {

    private val _uiState = MutableStateFlow(CanvasConnectUiState())
    val uiState: StateFlow<CanvasConnectUiState> = _uiState.asStateFlow()

    fun connect(canvasToken: String) {
        val token = canvasToken.trim()
        if (token.isEmpty() || _uiState.value.isConnecting) return
        _uiState.update { it.copy(isConnecting = true, errorMessage = null, syncWarning = null) }

        viewModelScope.launch {
            canvasRepository.connect(token)
                .onSuccess {
                    // El token ya quedo guardado cifrado en el backend en este punto -
                    // si el sync de abajo falla, el gate igual se marca como pasado
                    // (no tiene sentido pedir el token de nuevo por eso).
                    tokenStore.markCanvasConnected()

                    val syncWarning = canvasRepository.sync()
                        .fold(
                            onSuccess = { null },
                            onFailure = { error -> syncWarningFor(error) },
                        )
                    _uiState.update {
                        it.copy(isConnecting = false, isConnected = true, syncWarning = syncWarning)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isConnecting = false, errorMessage = errorMessageFor(error))
                    }
                }
        }
    }

    private fun syncWarningFor(error: Throwable): String = when (error) {
        is PaquitoBotApiError.RateLimited ->
            "Ya se estaba sincronizando tu cuenta - puede tardar unos minutos en aparecer tu información."

        else ->
            "Canvas se conectó, pero la sincronización inicial falló. Puede tardar unos minutos en aparecer tu información."
    }

    private fun errorMessageFor(error: Throwable): String = when (error) {
        is PaquitoBotApiError.InvalidCanvasToken ->
            "Canvas rechazó ese token. Revisá que lo copiaste completo."

        is PaquitoBotApiError.NotAuthenticated,
        is PaquitoBotApiError.InvalidSession,
        -> "Tu sesión expiró. Volvé a conectar con Google."

        is PaquitoBotApiError.ServiceUnavailable ->
            "El servicio no está disponible en este momento. Probá de nuevo en un rato."

        is PaquitoBotApiError.RequestTimeout ->
            "El servidor tardó en responder. Si es la primera vez, esperá un momento y volvé a conectar."

        is PaquitoBotApiError.NetworkFailure ->
            "Sin conexión. Revisá tu internet e intentá de nuevo."

        else -> "Algo salió mal. Intentá de nuevo."
    }
}
