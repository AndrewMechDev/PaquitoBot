package pe.tecsup.paquitobot.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clerk.api.Clerk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.SecureTokenStore
import pe.tecsup.paquitobot.data.repository.createDefaultAuthRepository
import pe.tecsup.paquitobot.data.repository.createDefaultCanvasRepository
import pe.tecsup.paquitobot.domain.auth.AuthRepository
import pe.tecsup.paquitobot.domain.canvas.CanvasRepository

/**
 * Estado de sesion a nivel de toda la app: dos gates secuenciales antes de
 * llegar a Home (login con Clerk, despues conexion con Canvas).
 * [isAuthLoading] es `true` mientras el SDK de Clerk se inicializa.
 */
data class SessionUiState(
    val isOnboardingComplete: Boolean = false,
    val isAuthLoading: Boolean = true,
    val isAuthenticated: Boolean = false,
    val isCanvasConnected: Boolean = false,
    val userFirstName: String? = null,
)

class SessionViewModel(
    private val tokenStore: SecureTokenStore,
    private val authRepository: AuthRepository = createDefaultAuthRepository(),
    private val canvasRepository: CanvasRepository = createDefaultCanvasRepository(tokenStore),
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SessionUiState(
            isOnboardingComplete = tokenStore.hasCompletedOnboarding(),
            isCanvasConnected = tokenStore.hasCanvasConnected(),
        ),
    )
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    private var hasSyncedCanvas = false

    init {
        // Despierta Render en paralelo mientras el usuario inicia sesion.
        viewModelScope.launch {
            authRepository.wakeBackend()
        }

        combine(Clerk.isInitialized, Clerk.userFlow) { isInitialized, user ->
            _uiState.update {
                it.copy(
                    isAuthLoading = !isInitialized,
                    isAuthenticated = user != null,
                    userFirstName = user?.firstName?.takeIf { name -> name.isNotBlank() },
                )
            }
            syncCanvasIfReady()
        }.launchIn(viewModelScope)
    }

    /**
     * Re-sync en cada cold start si ambos gates ya pasaron (flag local
     * sobrevive updates; sin esto /query puede responder sin datos).
     */
    private fun syncCanvasIfReady() {
        val state = _uiState.value
        if (hasSyncedCanvas || !state.isAuthenticated || !state.isCanvasConnected) return
        hasSyncedCanvas = true
        viewModelScope.launch {
            canvasRepository.sync()
        }
    }

    fun completeOnboarding() {
        tokenStore.markOnboardingComplete()
        _uiState.update { it.copy(isOnboardingComplete = true) }
    }

    fun markCanvasConnected() {
        _uiState.update { it.copy(isCanvasConnected = true) }
    }
}
