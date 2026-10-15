package pe.tecsup.paquitobot.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clerk.api.Clerk
import com.clerk.api.network.model.verification.Verification
import com.clerk.api.user.User
import com.clerk.api.network.serialization.onFailure
import com.clerk.api.network.serialization.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Estado de Clerk y avance del onboarding durante esta apertura de la app.
 */
data class SessionUiState(
    val isOnboardingComplete: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isUnauthorizedSession: Boolean = false,
    val userFirstName: String? = null,
    val sessionRevision: Int = 0,
    val isSigningOut: Boolean = false,
    val signOutError: String? = null,
)

class SessionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState())
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        Clerk.userFlow.onEach { user ->
            val isAuthorized = user?.hasVerifiedTecsupEmail() == true
            _uiState.update {
                it.copy(
                    isAuthenticated = isAuthorized,
                    isUnauthorizedSession = user != null && !isAuthorized,
                    isOnboardingComplete = if (user == null && it.isAuthenticated) false else it.isOnboardingComplete,
                    userFirstName = user?.firstName?.takeIf { name -> isAuthorized && name.isNotBlank() },
                    sessionRevision = if (it.isAuthenticated && !isAuthorized) it.sessionRevision + 1 else it.sessionRevision,
                    isSigningOut = if (user == null) false else it.isSigningOut,
                )
            }
        }.launchIn(viewModelScope)
    }

    fun completeOnboarding() {
        _uiState.update { it.copy(isOnboardingComplete = true) }
    }

    fun signOut() {
        if (_uiState.value.isSigningOut) return
        _uiState.update { it.copy(isSigningOut = true, signOutError = null) }
        viewModelScope.launch {
            Clerk.auth.signOut()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isAuthenticated = false,
                            isUnauthorizedSession = false,
                            isOnboardingComplete = false,
                            userFirstName = null,
                            sessionRevision = if (it.isAuthenticated) it.sessionRevision + 1 else it.sessionRevision,
                            isSigningOut = false,
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isSigningOut = false,
                            signOutError = "No se pudo cerrar sesión. Inténtalo de nuevo.",
                        )
                    }
                }
        }
    }
}

/** Solo la dirección principal, verificada por Clerk, concede acceso a la app. */
private fun User.hasVerifiedTecsupEmail(): Boolean {
    val primary = emailAddresses.orEmpty().firstOrNull { it.id == primaryEmailAddressId }
        ?: return false
    val email = primary.emailAddress
    val at = email.indexOf('@')
    return primary.verification?.status == Verification.Status.VERIFIED &&
        at > 0 && at == email.lastIndexOf('@') &&
        email.substring(at + 1).equals("tecsup.edu.pe", ignoreCase = true)
}
