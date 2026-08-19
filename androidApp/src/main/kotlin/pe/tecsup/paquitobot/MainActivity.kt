package pe.tecsup.paquitobot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.CanvasMockKeyStore
import pe.tecsup.paquitobot.auth.GoogleAuthClient
import pe.tecsup.paquitobot.auth.SecureTokenStore
import pe.tecsup.paquitobot.session.SessionViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicConnectScreen
import pe.tecsup.paquitobot.ui.academic.AcademicConnectViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicScheduleViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicViewModel
import pe.tecsup.paquitobot.ui.auth.AuthGateScreen
import pe.tecsup.paquitobot.ui.canvas.CanvasConnectScreen
import pe.tecsup.paquitobot.ui.canvas.CanvasConnectViewModel
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
 * app pasa por dos gates secuenciales evaluados con [SessionViewModel]:
 *
 *   1. Sin sesion de Google -> [AuthGateScreen].
 *   2. Con sesion pero sin Canvas conectado -> [CanvasConnectScreen].
 *   3. Con ambos -> Home/Chat normal, sin volver a pedir nada.
 *
 * Decision del usuario: ambos gates bloquean TODA la app (no solo el
 * chat), y una vez pasados no se vuelven a mostrar durante la sesion.
 */
@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenStore = remember { SecureTokenStore(context.applicationContext) }
    val canvasMockKeyStore = remember { CanvasMockKeyStore(context.applicationContext) }
    var academicGatePassed by remember {
        mutableStateOf(canvasMockKeyStore.hasApiKey() || canvasMockKeyStore.isSkipped())
    }
    val googleAuthClient = remember {
        GoogleAuthClient(
            context = context,
            webClientId = context.getString(R.string.google_web_client_id),
        )
    }

    val sessionViewModel: SessionViewModel = viewModel(
        factory = remember {
            viewModelFactory {
                initializer { SessionViewModel(tokenStore = tokenStore) }
            }
        },
    )
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    var onboardingStep by remember { mutableStateOf(OnboardingStep.Welcome) }

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

        !sessionUiState.isAuthenticated -> AuthGateScreen(
            isSigningIn = sessionUiState.isSigningIn,
            errorMessage = sessionUiState.authError,
            onSignInClick = {
                sessionViewModel.prepareSignIn()
                coroutineScope.launch {
                    googleAuthClient.signIn()
                        .onSuccess { result ->
                            sessionViewModel.completeSignIn(result.idToken, result.firstName())
                        }
                        .onFailure { error -> sessionViewModel.signInFailed(error) }
                }
            },
        )

        !sessionUiState.isCanvasConnected -> {
            val canvasViewModel: CanvasConnectViewModel = viewModel(
                factory = remember {
                    viewModelFactory {
                        initializer { CanvasConnectViewModel(tokenStore = tokenStore) }
                    }
                },
            )
            val canvasUiState by canvasViewModel.uiState.collectAsStateWithLifecycle()
            CanvasConnectScreen(
                uiState = canvasUiState,
                onConnectClick = canvasViewModel::connect,
                onContinueClick = sessionViewModel::markCanvasConnected,
            )
        }

        // Cuarto gate (2026-08-19), OPCIONAL: canvas-mock (ver skill
        // canvas-mock-backend). A diferencia de los gates de arriba, este
        // SIEMPRE se puede omitir - los datos mock son una demo, no un
        // requisito. Independiente del gate de Canvas real de arriba (dos
        // backends distintos).
        !academicGatePassed -> {
            val academicConnectViewModel: AcademicConnectViewModel = viewModel(
                factory = remember {
                    viewModelFactory {
                        initializer { AcademicConnectViewModel(keyStore = canvasMockKeyStore) }
                    }
                },
            )
            val academicUiState by academicConnectViewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(academicUiState.isConnected) {
                if (academicUiState.isConnected) academicGatePassed = true
            }
            AcademicConnectScreen(
                uiState = academicUiState,
                onConnectClick = academicConnectViewModel::connect,
                onSkipClick = {
                    canvasMockKeyStore.markSkipped()
                    academicGatePassed = true
                },
            )
        }

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
                    )
                    NavTab.Cursos -> {
                        // Iteracion 2026-08-19: si el estudiante conecto una
                        // cuenta mock (ver skill canvas-mock-backend), Cursos
                        // muestra datos REALES de canvas-mock en vez del mock
                        // hardcodeado. Si omitio el gate, sigue con el mock.
                        if (canvasMockKeyStore.hasApiKey()) {
                            val academicViewModel: AcademicViewModel = viewModel(
                                factory = remember {
                                    viewModelFactory {
                                        initializer { AcademicViewModel(keyStore = canvasMockKeyStore) }
                                    }
                                },
                            )
                            val academicData by academicViewModel.uiState.collectAsStateWithLifecycle()
                            CoursesScreen(
                                data = academicData,
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                onPaquitoClick = { rootScreen = RootScreen.Chat },
                                onCourseClick = { course ->
                                    selectedCourse = course
                                    rootScreen = RootScreen.CourseDetail
                                },
                            )
                        } else {
                            CoursesScreen(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                onPaquitoClick = { rootScreen = RootScreen.Chat },
                                onCourseClick = { course ->
                                    selectedCourse = course
                                    rootScreen = RootScreen.CourseDetail
                                },
                            )
                        }
                    }
                    NavTab.Horarios -> {
                        // Mismo criterio que Cursos: real si hay key mock conectada.
                        if (canvasMockKeyStore.hasApiKey()) {
                            val scheduleViewModel: AcademicScheduleViewModel = viewModel(
                                factory = remember {
                                    viewModelFactory {
                                        initializer { AcademicScheduleViewModel(keyStore = canvasMockKeyStore) }
                                    }
                                },
                            )
                            val scheduleData by scheduleViewModel.uiState.collectAsStateWithLifecycle()
                            ScheduleScreen(
                                data = scheduleData,
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                onPaquitoClick = { rootScreen = RootScreen.Chat },
                            )
                        } else {
                            ScheduleScreen(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                onPaquitoClick = { rootScreen = RootScreen.Chat },
                            )
                        }
                    }
                }
                RootScreen.Chat -> {
                    val chatViewModel: ChatViewModel = viewModel(
                        factory = remember {
                            viewModelFactory {
                                initializer { ChatViewModel(tokenStore = tokenStore) }
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
