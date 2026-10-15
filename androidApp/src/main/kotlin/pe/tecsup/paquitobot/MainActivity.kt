package pe.tecsup.paquitobot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pe.tecsup.paquitobot.data.MiniMaxChatRepository
import pe.tecsup.paquitobot.session.SessionViewModel
import pe.tecsup.paquitobot.ui.auth.ClerkAuthGate
import pe.tecsup.paquitobot.ui.chat.ChatScreen
import pe.tecsup.paquitobot.ui.chat.ChatViewModel
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.courses.CourseCardData
import pe.tecsup.paquitobot.ui.courses.CourseDetailData
import pe.tecsup.paquitobot.ui.courses.CourseDetailScreen
import pe.tecsup.paquitobot.ui.courses.CoursesScreen
import pe.tecsup.paquitobot.ui.courses.CoursesScreenData
import pe.tecsup.paquitobot.ui.home.HomeScreen
import pe.tecsup.paquitobot.ui.home.HomeScreenData
import pe.tecsup.paquitobot.ui.notifications.NotificationsInboxScreen
import pe.tecsup.paquitobot.ui.onboarding.OnboardingNotificationsScreen
import pe.tecsup.paquitobot.ui.onboarding.OnboardingTourScreen
import pe.tecsup.paquitobot.ui.onboarding.WelcomeScreen
import pe.tecsup.paquitobot.ui.schedule.ScheduleScreen
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Mientras no haya navegacion real (Compose Navigation), el boton Paquito
 * del Navbar se usa como toggle hacia Chat; Cursos y Horarios son pestañas
 * reales. El detalle de curso usa flecha atras como el chat.
 */
private enum class RootScreen { Tabs, Chat, CourseDetail, Notifications }

private enum class OnboardingStep { Welcome, Notifications, Tour }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            PaquitoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PaquitoColors.Background,
                ) {
                    AppRoot()
                }
            }
        }
    }
}

/**
 * Iteracion 2026-08-13 (flujo de gates): antes de mostrar Home o Chat, la
 * app presenta el onboarding en cada apertura y después evalúa Clerk:
 *
 *   1. Bienvenida, notificaciones y recorrido.
 *   2. Sin sesion de Clerk -> [ClerkAuthGate].
 *   3. Con sesion -> pantallas de la app.
 */
@Composable
private fun AppRoot() {
    val sessionViewModel: SessionViewModel = viewModel()
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    var onboardingStep by remember(sessionUiState.sessionRevision) {
        mutableStateOf(OnboardingStep.Welcome)
    }

    when {
        !sessionUiState.isOnboardingComplete -> when (onboardingStep) {
            OnboardingStep.Welcome -> WelcomeScreen(
                onContinueClick = { onboardingStep = OnboardingStep.Notifications },
            )
            OnboardingStep.Notifications -> OnboardingNotificationsScreen(
                onContinue = { _ -> onboardingStep = OnboardingStep.Tour },
            )
            OnboardingStep.Tour -> OnboardingTourScreen(
                onContinue = { sessionViewModel.completeOnboarding() },
            )
        }

        !sessionUiState.isAuthenticated -> ClerkAuthGate(
            isUnauthorizedSession = sessionUiState.isUnauthorizedSession,
            isSigningOut = sessionUiState.isSigningOut,
            signOutError = sessionUiState.signOutError,
            onSignOutClick = sessionViewModel::signOut,
        )

        else -> {
            var currentTab by remember { mutableStateOf(NavTab.Inicio) }
            var rootScreen by remember { mutableStateOf(RootScreen.Tabs) }
            var selectedCourse by remember { mutableStateOf<CourseCardData?>(null) }
            val userFirstName = sessionUiState.userFirstName ?: "estudiante"

            when (rootScreen) {
                RootScreen.Tabs -> when (currentTab) {
                    NavTab.Inicio -> HomeScreen(
                        data = HomeScreenData.default().copy(
                            greeting = "¡Bienvenido, $userFirstName!",
                        ),
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onPaquitoClick = { rootScreen = RootScreen.Chat },
                        onNotificationsClick = { rootScreen = RootScreen.Notifications },
                        onSignOutClick = sessionViewModel::signOut,
                        isSigningOut = sessionUiState.isSigningOut,
                        signOutError = sessionUiState.signOutError,
                    )
                    NavTab.Cursos -> CoursesScreen(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onPaquitoClick = { rootScreen = RootScreen.Chat },
                        onCourseClick = { course ->
                            selectedCourse = course
                            rootScreen = RootScreen.CourseDetail
                        },
                    )
                    NavTab.Horarios -> ScheduleScreen(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onPaquitoClick = { rootScreen = RootScreen.Chat },
                    )
                }
                RootScreen.Chat -> {
                    val chatViewModel: ChatViewModel = viewModel(
                        key = "chat-${sessionUiState.sessionRevision}",
                        factory = remember {
                            viewModelFactory {
                                initializer { ChatViewModel(MiniMaxChatRepository(BuildConfig.MINIMAX_API_KEY)) }
                            }
                        },
                    )
                    val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()
                    ChatScreen(
                        uiState = chatUiState,
                        onSendMessage = chatViewModel::sendMessage,
                        userName = userFirstName,
                        onBackClick = { rootScreen = RootScreen.Tabs },
                    )
                }
                RootScreen.CourseDetail -> CourseDetailScreen(
                    data = CourseDetailData.forCourse(
                        selectedCourse ?: CoursesScreenData.default().courses.first(),
                    ),
                    onBackClick = { rootScreen = RootScreen.Tabs },
                )
                RootScreen.Notifications -> NotificationsInboxScreen(
                    onBackClick = { rootScreen = RootScreen.Tabs },
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {
    PaquitoTheme {
        AppRoot()
    }
}
