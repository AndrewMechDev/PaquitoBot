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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.CanvasMockKeyStore
import pe.tecsup.paquitobot.auth.GoogleAuthClient
import pe.tecsup.paquitobot.auth.SecureTokenStore
import pe.tecsup.paquitobot.data.local.NotificationPreferencesStore
import pe.tecsup.paquitobot.session.SessionViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicConnectScreen
import pe.tecsup.paquitobot.ui.academic.AcademicConnectViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicCourseDetailViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicHomeViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicScheduleViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicViewModel
import pe.tecsup.paquitobot.ui.auth.AuthGateScreen
import pe.tecsup.paquitobot.ui.chat.ChatScreen
import pe.tecsup.paquitobot.ui.chat.ChatViewModel
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.components.NotificationType
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

/** [CourseCardData] no es Parcelable: se guarda como lista de primitivos para sobrevivir a la rotacion. */
private val SelectedCourseSaver = listSaver<CourseCardData?, Any>(
    save = { course ->
        if (course == null) {
            emptyList()
        } else {
            listOf(
                course.id,
                course.code,
                course.name,
                course.projectedGrade,
                course.trackHint,
                course.absences,
                course.absenceLimit,
                course.nextDueLabel,
            )
        }
    },
    restore = { saved ->
        if (saved.isEmpty()) {
            null
        } else {
            CourseCardData(
                id = saved[0] as String,
                code = saved[1] as String,
                name = saved[2] as String,
                projectedGrade = saved[3] as String,
                trackHint = saved[4] as String,
                absences = saved[5] as Int,
                absenceLimit = saved[6] as Int,
                nextDueLabel = saved[7] as String,
            )
        }
    },
)

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
 * Flujo de gates antes de mostrar Home o Chat (actualizado 2026-08-19):
 *
 *   1. Sin sesion de Google -> [AuthGateScreen] (unico gate OBLIGATORIO).
 *   2. Con sesion, sin canvas-mock conectado -> gate OPCIONAL de
 *      canvas-mock (siempre se puede omitir - ver skill
 *      canvas-mock-backend).
 *   3. Con Google + (mock conectado u omitido) -> Home/Chat normal.
 *
 * El gate de Canvas REAL (paquitobot-rag) esta retirado del flujo
 * mientras no haya API real de Canvas disponible.
 */
@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenStore = remember { SecureTokenStore(context.applicationContext) }
    val notificationPreferences = remember { NotificationPreferencesStore(context.applicationContext) }
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
                initializer {
                    SessionViewModel(tokenStore = tokenStore, notificationPreferences = notificationPreferences)
                }
            }
        },
    )
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()
    var onboardingStep by rememberSaveable { mutableStateOf(OnboardingStep.Welcome) }

    when {
        !sessionUiState.isOnboardingComplete -> when (onboardingStep) {
            OnboardingStep.Welcome -> WelcomeScreen(
                onContinueClick = { onboardingStep = OnboardingStep.Notifications },
            )
            OnboardingStep.Notifications -> OnboardingNotificationsScreen(
                initialEnabled = NotificationType.entries.filterTo(mutableSetOf()) {
                    it.category in sessionUiState.enabledNotifications
                },
                onContinue = { enabled ->
                    sessionViewModel.saveNotificationPreferences(enabled.map { it.category }.toSet())
                    onboardingStep = OnboardingStep.Tour
                },
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

        // Gate del Canvas REAL (paquitobot-rag) RETIRADO del flujo
        // (2026-08-19): mientras no haya API real de Canvas, la unica
        // fuente de datos academicos es canvas-mock (gate de abajo,
        // opcional). Se mantiene solo el gate de Google como obligatorio.
        // CanvasConnectScreen/CanvasConnectViewModel quedan sin uso aca a
        // proposito - no se borran porque son necesarios el dia que haya
        // integracion Canvas real.

        // Tercer gate (2026-08-19), OPCIONAL: canvas-mock (ver skill
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
            // rememberSaveable: sobrevive a la rotacion (Android recrea la Activity)
            // - con remember simple, girar el telefono mandaba siempre a Inicio.
            var currentTab by rememberSaveable { mutableStateOf(NavTab.Inicio) }
            var rootScreen by rememberSaveable { mutableStateOf(RootScreen.Tabs) }
            var selectedCourse by rememberSaveable(stateSaver = SelectedCourseSaver) {
                mutableStateOf<CourseCardData?>(null)
            }
            val userFirstName = sessionUiState.userFirstName ?: "estudiante"

            when (rootScreen) {
                RootScreen.Tabs -> when (currentTab) {
                    NavTab.Inicio -> {
                        // Mismo criterio que Cursos/Horarios: real si hay key mock conectada.
                        if (canvasMockKeyStore.hasApiKey()) {
                            val homeViewModel: AcademicHomeViewModel = viewModel(
                                factory = remember {
                                    viewModelFactory {
                                        initializer { AcademicHomeViewModel(keyStore = canvasMockKeyStore) }
                                    }
                                },
                            )
                            val homeData by homeViewModel.uiState.collectAsStateWithLifecycle()
                            val homeError by homeViewModel.errorMessage.collectAsStateWithLifecycle()
                            HomeScreen(
                                data = homeData.copy(greeting = "¡Bienvenido, $userFirstName!"),
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                onPaquitoClick = { rootScreen = RootScreen.Chat },
                                onNotificationsClick = { rootScreen = RootScreen.Notifications },
                                errorMessage = homeError,
                                onRetry = homeViewModel::load,
                            )
                        } else {
                            HomeScreen(
                                data = HomeScreenData.default().copy(
                                    greeting = "¡Bienvenido, $userFirstName!",
                                ),
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                onPaquitoClick = { rootScreen = RootScreen.Chat },
                                onNotificationsClick = { rootScreen = RootScreen.Notifications },
                            )
                        }
                    }
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
                            val academicError by academicViewModel.errorMessage.collectAsStateWithLifecycle()
                            CoursesScreen(
                                data = academicData,
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                onPaquitoClick = { rootScreen = RootScreen.Chat },
                                onCourseClick = { course ->
                                    selectedCourse = course
                                    rootScreen = RootScreen.CourseDetail
                                },
                                errorMessage = academicError,
                                onRetry = academicViewModel::load,
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
                            val scheduleError by scheduleViewModel.errorMessage.collectAsStateWithLifecycle()
                            ScheduleScreen(
                                data = scheduleData,
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                onPaquitoClick = { rootScreen = RootScreen.Chat },
                                errorMessage = scheduleError,
                                onRetry = scheduleViewModel::load,
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
                RootScreen.CourseDetail -> {
                    val course = selectedCourse ?: CoursesScreenData.default().courses.first()
                    // Real solo si hay key mock conectada Y el id del curso es
                    // numerico (viene de canvas-mock) - el mock hardcodeado usa
                    // ids tipo "calculo-ii", que no son un curso real ahi.
                    if (canvasMockKeyStore.hasApiKey() && course.id.toIntOrNull() != null) {
                        val detailViewModel: AcademicCourseDetailViewModel = viewModel(
                            key = course.id,
                            factory = remember(course.id) {
                                viewModelFactory {
                                    initializer {
                                        AcademicCourseDetailViewModel(keyStore = canvasMockKeyStore, course = course)
                                    }
                                }
                            },
                        )
                        val detailData by detailViewModel.uiState.collectAsStateWithLifecycle()
                        val detailError by detailViewModel.errorMessage.collectAsStateWithLifecycle()
                        CourseDetailScreen(
                            data = detailData,
                            errorMessage = detailError,
                            onRetry = detailViewModel::load,
                            onBackClick = { rootScreen = RootScreen.Tabs },
                        )
                    } else {
                        CourseDetailScreen(
                            data = CourseDetailData.forCourse(course),
                            onBackClick = { rootScreen = RootScreen.Tabs },
                        )
                    }
                }
                RootScreen.Notifications -> {
                    var preferencesOpen by rememberSaveable { mutableStateOf(false) }
                    NotificationsInboxScreen(
                        onBackClick = { rootScreen = RootScreen.Tabs },
                        onPreferencesClick = { preferencesOpen = true },
                    )
                    if (preferencesOpen) {
                        // La edición ocupa toda la pantalla y mantiene la bandeja compuesta
                        // para conservar sus elementos y selección de vista al regresar.
                        Dialog(
                            onDismissRequest = { preferencesOpen = false },
                            properties = DialogProperties(
                                usePlatformDefaultWidth = false,
                                decorFitsSystemWindows = false,
                            ),
                        ) {
                            PaquitoTheme {
                                OnboardingNotificationsScreen(
                                    initialEnabled = NotificationType.entries.filterTo(mutableSetOf()) {
                                        it.category in sessionUiState.enabledNotifications
                                    },
                                    onContinue = { enabled ->
                                        sessionViewModel.saveNotificationPreferences(enabled.map { it.category }.toSet())
                                        preferencesOpen = false
                                    },
                                    onCancel = { preferencesOpen = false },
                                )
                            }
                        }
                    }
                }
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
