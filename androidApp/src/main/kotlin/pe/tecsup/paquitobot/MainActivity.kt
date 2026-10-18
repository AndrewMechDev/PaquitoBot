package pe.tecsup.paquitobot

import android.os.Bundle
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import pe.tecsup.paquitobot.data.local.NotificationPreferencesStore
import pe.tecsup.paquitobot.data.notifications.AndroidAcademicNotificationScheduler
import pe.tecsup.paquitobot.ui.components.NotificationType
import pe.tecsup.paquitobot.ui.notifications.NotificationPermissionSection
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
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pe.tecsup.paquitobot.data.MiniMaxChatRepository
import pe.tecsup.paquitobot.session.SessionViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicCourseDetailViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicHomeViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicScheduleViewModel
import pe.tecsup.paquitobot.ui.academic.AcademicViewModel
import pe.tecsup.paquitobot.ui.auth.ClerkAuthGate
import pe.tecsup.paquitobot.ui.chat.ChatScreen
import pe.tecsup.paquitobot.ui.chat.ChatViewModel
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.courses.CourseCardData
import pe.tecsup.paquitobot.ui.courses.CourseDetailScreen
import pe.tecsup.paquitobot.ui.courses.CoursesScreen
import pe.tecsup.paquitobot.ui.home.HomeScreen
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

/**
 * Fecha "de hoy" que usan las pantallas academicas. El JSON de demo
 * (`assets/academic.json`) tiene sus eventos en la semana del 2026-08-03,
 * que casi nunca coincide con la fecha del dispositivo; se fija para que la
 * demo se vea con datos vigentes. Cuando haya datos reales, cambiar por
 * `::deviceToday` (es el valor por defecto de los ViewModels academicos).
 */
private const val DEMO_TODAY = "2026-08-03"

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

/** Factory de un solo ViewModel; sin libreria de DI (ver skill architecture-paquitobot). */
private inline fun <reified VM : ViewModel> factoryOf(noinline create: () -> VM): ViewModelProvider.Factory =
    viewModelFactory { initializer { create() } }

class MainActivity : ComponentActivity() {
    override fun onResume() {
        super.onResume()
        AndroidAcademicNotificationScheduler(applicationContext).reconcile()
    }

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
    val context = LocalContext.current.applicationContext
    val notificationPreferences = remember(context) { NotificationPreferencesStore(context) }
    val notificationScheduler = remember(context) { AndroidAcademicNotificationScheduler(context) }
    val sessionViewModel: SessionViewModel = viewModel(factory = remember {
        factoryOf { SessionViewModel(notificationPreferences, notificationScheduler) }
    })
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
                initialEnabled = NotificationType.entries.filterTo(mutableSetOf()) { it.category in sessionUiState.enabledNotifications },
                initialSchedule = sessionUiState.notificationSchedule,
                permissionContent = { NotificationPermissionSection(onPermissionChanged = notificationScheduler::reconcile) },
                onContinue = { enabled, schedule ->
                    sessionViewModel.saveNotificationPreferences(enabled.map { it.category }.toSet(), schedule)
                    onboardingStep = OnboardingStep.Tour
                },
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
            // rememberSaveable: sobrevive a la rotacion (Android recrea la Activity)
            // - con remember simple, girar el telefono mandaba siempre a Inicio.
            var currentTab by rememberSaveable { mutableStateOf(NavTab.Inicio) }
            var rootScreen by rememberSaveable { mutableStateOf(RootScreen.Tabs) }
            var selectedCourse by rememberSaveable(stateSaver = SelectedCourseSaver) {
                mutableStateOf<CourseCardData?>(null)
            }
            val userFirstName = sessionUiState.userFirstName ?: "estudiante"
            val academicRepository = (LocalContext.current.applicationContext as PaquitoBotApp).academicRepository

            when (rootScreen) {
                RootScreen.Tabs -> when (currentTab) {
                    NavTab.Inicio -> {
                        val homeViewModel: AcademicHomeViewModel = viewModel(
                            factory = remember {
                                factoryOf { AcademicHomeViewModel(academicRepository, today = { DEMO_TODAY }) }
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
                            onSignOutClick = sessionViewModel::signOut,
                            isSigningOut = sessionUiState.isSigningOut,
                            signOutError = sessionUiState.signOutError,
                            errorMessage = homeError,
                            onRetry = homeViewModel::load,
                        )
                    }
                    NavTab.Cursos -> {
                        val coursesViewModel: AcademicViewModel = viewModel(
                            factory = remember {
                                factoryOf { AcademicViewModel(academicRepository, today = { DEMO_TODAY }) }
                            },
                        )
                        val coursesData by coursesViewModel.uiState.collectAsStateWithLifecycle()
                        val coursesError by coursesViewModel.errorMessage.collectAsStateWithLifecycle()
                        CoursesScreen(
                            data = coursesData,
                            currentTab = currentTab,
                            onTabSelected = { currentTab = it },
                            onPaquitoClick = { rootScreen = RootScreen.Chat },
                            onCourseClick = { course ->
                                selectedCourse = course
                                rootScreen = RootScreen.CourseDetail
                            },
                            errorMessage = coursesError,
                            onRetry = coursesViewModel::load,
                        )
                    }
                    NavTab.Horarios -> {
                        val scheduleViewModel: AcademicScheduleViewModel = viewModel(
                            factory = remember {
                                factoryOf { AcademicScheduleViewModel(academicRepository, today = { DEMO_TODAY }) }
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
                    }
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
                RootScreen.CourseDetail -> {
                    val course = selectedCourse
                    if (course == null) {
                        // No deberia pasar (se elige un curso antes de navegar), pero si el
                        // estado guardado se pierde, volver a las pestanas en vez de quedar vacio.
                        LaunchedEffect(Unit) { rootScreen = RootScreen.Tabs }
                    } else {
                        val detailViewModel: AcademicCourseDetailViewModel = viewModel(
                            key = course.id,
                            factory = remember(course.id) {
                                factoryOf {
                                    AcademicCourseDetailViewModel(academicRepository, course, today = { DEMO_TODAY })
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
                                    initialSchedule = sessionUiState.notificationSchedule,
                                    permissionContent = { NotificationPermissionSection(onPermissionChanged = notificationScheduler::reconcile) },
                                    onContinue = { enabled, schedule ->
                                        sessionViewModel.saveNotificationPreferences(enabled.map { it.category }.toSet(), schedule)
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
