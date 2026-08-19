package pe.tecsup.paquitobot.ui.academic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.CanvasMockKeyStore
import pe.tecsup.paquitobot.data.remote.CanvasMockApiError
import pe.tecsup.paquitobot.data.remote.TokenProvider
import pe.tecsup.paquitobot.data.repository.createDefaultAcademicRepository
import pe.tecsup.paquitobot.domain.academic.AcademicRepository

/** Estado del gate opcional de `canvas-mock` - ver skill `canvas-mock-backend`. */
data class AcademicConnectUiState(
    val isConnecting: Boolean = false,
    val isConnected: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * Tercer gate de la app (2026-08-19), OPCIONAL: conecta con `canvas-mock`
 * (datos academicos ficticios - cursos, notas, asistencia). Distinto del
 * gate de Canvas real (`CanvasConnectViewModel`, sigue existiendo para el
 * Chat via `paquitobot-rag`) - son dos backends independientes.
 *
 * [connect] valida la key llamando `profile()` de una - si el backend la
 * acepta, la persiste. No hay endpoint de "solo validar sin usar datos":
 * pedir el perfil es la forma mas barata de confirmar que la key es
 * valida antes de guardarla.
 */
class AcademicConnectViewModel(
    private val keyStore: CanvasMockKeyStore,
    private val academicRepository: AcademicRepository = createDefaultAcademicRepository(keyStore),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        AcademicConnectUiState(isConnected = keyStore.hasApiKey()),
    )
    val uiState: StateFlow<AcademicConnectUiState> = _uiState.asStateFlow()

    fun connect(apiKey: String) {
        val key = apiKey.trim()
        if (key.isEmpty() || _uiState.value.isConnecting) return
        _uiState.update { it.copy(isConnecting = true, errorMessage = null) }

        viewModelScope.launch {
            // La key todavia no esta guardada - se prueba pasandola directo
            // al repository con un TokenProvider temporal, para no persistir
            // una key invalida.
            val probe = createDefaultAcademicRepository(FixedTokenProvider(key))
            probe.profile()
                .onSuccess {
                    keyStore.saveApiKey(key)
                    _uiState.update { it.copy(isConnecting = false, isConnected = true) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isConnecting = false, errorMessage = errorMessageFor(error))
                    }
                }
        }
    }

    private fun errorMessageFor(error: Throwable): String = when (error) {
        is CanvasMockApiError.InvalidApiKey ->
            "Esa clave no existe. Probá con stu_001, stu_002... o adm_001."

        is CanvasMockApiError.NetworkFailure ->
            "No se pudo conectar con el servidor de pruebas. ¿Está corriendo uvicorn y estás en la misma red WiFi?"

        else -> "Algo salió mal. Intentá de nuevo."
    }
}

/** [TokenProvider] de un solo valor fijo - usado solo para probar una key antes de persistirla. */
private class FixedTokenProvider(private val value: String) : TokenProvider {
    override suspend fun getToken(): String = value
}
