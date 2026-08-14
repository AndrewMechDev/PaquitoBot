package pe.tecsup.paquitobot.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.GoogleSignInFailure
import pe.tecsup.paquitobot.auth.SecureTokenStore
import pe.tecsup.paquitobot.data.remote.PaquitoBotApiError
import pe.tecsup.paquitobot.data.repository.createDefaultAuthRepository
import pe.tecsup.paquitobot.data.repository.createDefaultCanvasRepository
import pe.tecsup.paquitobot.domain.auth.AuthRepository
import pe.tecsup.paquitobot.domain.canvas.CanvasRepository

/**
 * Estado de sesion a nivel de toda la app: dos gates secuenciales antes de
 * llegar a Home (login con Google, despues conexion con Canvas).
 */
data class SessionUiState(
    val isAuthenticated: Boolean = false,
    val isSigningIn: Boolean = false,
    val authError: String? = null,
    val isCanvasConnected: Boolean = false,
)

class SessionViewModel(
    private val tokenStore: SecureTokenStore,
    private val authRepository: AuthRepository = createDefaultAuthRepository(),
    private val canvasRepository: CanvasRepository = createDefaultCanvasRepository(tokenStore),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SessionUiState(
            isAuthenticated = tokenStore.hasValidSession(),
            isCanvasConnected = tokenStore.hasCanvasConnected(),
        ),
    )
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var loginJob: Job? = null

    init {
        if (!_uiState.value.isAuthenticated) {
            viewModelScope.launch {
                authRepository.wakeBackend()
            }
        }
        // Re-sync en cada cold start si ambos gates ya pasaron (flag local
        // sobrevive updates; sin esto /query puede responder sin datos).
        if (_uiState.value.isAuthenticated && _uiState.value.isCanvasConnected) {
            viewModelScope.launch {
                canvasRepository.sync()
            }
        }
    }

    /** Despierta Render en paralelo mientras el usuario abre Google. */
    fun prepareSignIn() {
        _uiState.update { it.copy(isSigningIn = true, authError = null) }
        viewModelScope.launch {
            authRepository.wakeBackend()
        }
    }

    /** Completa el login: [idToken] ya se obtuvo de Google en Compose. */
    fun completeSignIn(idToken: String) {
        if (loginJob?.isActive == true) return
        _uiState.update { it.copy(isSigningIn = true, authError = null) }

        loginJob = viewModelScope.launch {
            authRepository.loginWithGoogle(idToken)
                .onSuccess { session ->
                    tokenStore.saveSession(session.accessToken, session.expiresInSeconds)
                    _uiState.update { it.copy(isAuthenticated = true, isSigningIn = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isSigningIn = false, authError = backendSignInMessage(error))
                    }
                }
        }
    }

    fun signInFailed(error: Throwable? = null) {
        _uiState.update {
            it.copy(isSigningIn = false, authError = googleSheetMessage(error))
        }
    }

    fun markCanvasConnected() {
        _uiState.update { it.copy(isCanvasConnected = true) }
    }

    private fun backendSignInMessage(error: Throwable): String = when (error) {
        is PaquitoBotApiError.GoogleSignInRejected ->
            "Google rechazó el inicio de sesión. Probá de nuevo."

        is PaquitoBotApiError.RequestTimeout ->
            "El servidor está despertando y tardó más de lo normal. Esperá un momento y volvé a intentar."

        is PaquitoBotApiError.NetworkFailure ->
            "Sin conexión. Revisá tu internet e intentá de nuevo."

        is PaquitoBotApiError.ServiceUnavailable ->
            "El servicio no está disponible en este momento. Probá de nuevo en un rato."

        else -> "No se pudo iniciar sesión. Probá de nuevo."
    }

    private fun googleSheetMessage(error: Throwable?): String = when (error) {
        is GoogleSignInFailure.Cancelled ->
            "Cancelaste el inicio de sesión."

        is GoogleSignInFailure.NoAccount ->
            "No encontramos una cuenta de Google en este dispositivo."

        else -> "No se pudo abrir el inicio de sesión de Google. Probá de nuevo."
    }
}
