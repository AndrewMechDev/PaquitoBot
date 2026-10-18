package pe.tecsup.paquitobot.session

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clerk.api.Clerk
import com.clerk.api.network.serialization.onFailure
import com.clerk.api.network.serialization.onSuccess
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.authorizedAcademicUserId
import pe.tecsup.paquitobot.auth.hasVerifiedTecsupEmail
import pe.tecsup.paquitobot.data.local.NotificationPreferencesStore
import pe.tecsup.paquitobot.domain.notifications.AcademicNotificationScheduler
import pe.tecsup.paquitobot.domain.notifications.NotificationCategory
import pe.tecsup.paquitobot.domain.notifications.NotificationSchedule

/**
 * Estado de Clerk y configuración inicial persistida en esta instalación.
 */
data class SessionUiState(
    val isOnboardingComplete: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isUnauthorizedSession: Boolean = false,
    val userFirstName: String? = null,
    val userFullName: String? = null,
    val userEmail: String? = null,
    val sessionRevision: Int = 0,
    val isSigningOut: Boolean = false,
    val signOutError: String? = null,
    val enabledNotifications: Set<NotificationCategory> = NotificationCategory.defaults,
    val notificationSchedule: NotificationSchedule = NotificationSchedule(),
)

class SessionViewModel(
    private val notificationPreferences: NotificationPreferencesStore,
    private val notificationScheduler: AcademicNotificationScheduler,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SessionUiState(
        isOnboardingComplete = notificationPreferences.hasCompletedSetup(),
        enabledNotifications = notificationPreferences.enabledCategories(),
        notificationSchedule = notificationPreferences.schedule(),
    ))
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()

    init {
        combine(Clerk.isInitialized, Clerk.userFlow, Clerk.sessionFlow) { initialized, user, _ ->
            initialized to user
        }.onEach { (initialized, user) ->
            val isAuthorized = initialized && authorizedAcademicUserId() != null
            _uiState.update {
                it.copy(
                    isAuthenticated = isAuthorized,
                    isUnauthorizedSession = user != null && !user.hasVerifiedTecsupEmail(),
                    userFirstName = user?.firstName?.takeIf { name -> isAuthorized && name.isNotBlank() },
                    userFullName = if (isAuthorized) listOfNotNull(user?.firstName, user?.lastName).filter { name -> name.isNotBlank() }.joinToString(" ").ifBlank { "Estudiante" } else null,
                    userEmail = if (isAuthorized) user?.emailAddresses?.firstOrNull { address -> address.id == user.primaryEmailAddressId }?.emailAddress else null,
                    sessionRevision = if (it.isAuthenticated && !isAuthorized) it.sessionRevision + 1 else it.sessionRevision,
                    isSigningOut = if (user == null) false else it.isSigningOut,
                )
            }
            notificationScheduler.reconcile()
        }.launchIn(viewModelScope)
    }

    fun completeOnboarding() {
        notificationPreferences.markSetupCompleted()
        notificationScheduler.reconcile()
        _uiState.update { it.copy(isOnboardingComplete = true) }
    }

    fun saveNotificationPreferences(categories: Set<NotificationCategory>, schedule: NotificationSchedule) {
        notificationPreferences.savePreferences(categories, schedule)
        notificationScheduler.reconcile()
        _uiState.update { it.copy(enabledNotifications = categories.toSet(), notificationSchedule = schedule) }
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
                            userFirstName = null,
                            userFullName = null,
                            userEmail = null,
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
