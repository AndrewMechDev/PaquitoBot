package pe.tecsup.paquitobot

import android.os.Bundle
import android.content.Intent
import android.net.Uri
import pe.tecsup.paquitobot.data.academic.DemoTutorDirectory
import pe.tecsup.paquitobot.data.notifications.AcademicNotificationPublisher
import pe.tecsup.paquitobot.ui.calendar.CalendarScreen
import pe.tecsup.paquitobot.ui.procedures.EnrollmentGuideRoute
import pe.tecsup.paquitobot.ui.procedures.ProcedureGuideScreen
import pe.tecsup.paquitobot.ui.procedures.ProcedureDetailScreen
import pe.tecsup.paquitobot.ui.procedures.ProcedureViewModel
import pe.tecsup.paquitobot.ui.procedures.ProcedureUiState
import pe.tecsup.paquitobot.data.local.NotificationPreferencesStore
import pe.tecsup.paquitobot.data.notifications.AndroidAcademicNotificationScheduler
import pe.tecsup.paquitobot.ui.components.NotificationType
import pe.tecsup.paquitobot.ui.notifications.NotificationPermissionSection
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.ui.academic.loadAllCourseRecords
import pe.tecsup.paquitobot.ui.academic.buildCoursesData
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
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
import pe.tecsup.paquitobot.ui.academic.AcademicRiskScreen
import pe.tecsup.paquitobot.ui.academic.AcademicRiskViewModel
import pe.tecsup.paquitobot.ui.auth.ClerkAuthGate
import pe.tecsup.paquitobot.ui.chat.ChatScreen
import pe.tecsup.paquitobot.ui.chat.ChatViewModel
import pe.tecsup.paquitobot.ui.components.AppTabScaffold
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.courses.CourseCardData
import pe.tecsup.paquitobot.ui.courses.CourseDetailScreen
import pe.tecsup.paquitobot.ui.courses.CoursesTabContent
import pe.tecsup.paquitobot.ui.events.EventDetailScreen
import pe.tecsup.paquitobot.ui.events.EventMockData
import pe.tecsup.paquitobot.ui.events.EventsScreen
import pe.tecsup.paquitobot.ui.home.HomeTabContent
import pe.tecsup.paquitobot.ui.schedule.ScheduleTabContent
import pe.tecsup.paquitobot.ui.notifications.NotificationsInboxScreen
import pe.tecsup.paquitobot.ui.onboarding.OnboardingNotificationsScreen
import pe.tecsup.paquitobot.ui.onboarding.OnboardingTourScreen
import pe.tecsup.paquitobot.ui.onboarding.WelcomeScreen
import pe.tecsup.paquitobot.ui.schedule.ScheduleScreen
import pe.tecsup.paquitobot.ui.teacher.TeacherDashboardScreen
import pe.tecsup.paquitobot.ui.teacher.TeacherDemoAttendanceSignal
import pe.tecsup.paquitobot.ui.teacher.TeacherDemoCourse
import pe.tecsup.paquitobot.ui.teacher.TeacherDemoData
import pe.tecsup.paquitobot.ui.teacher.TeacherDemoStudentCourse
import pe.tecsup.paquitobot.domain.notifications.AcademicNotice
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Mientras no haya navegacion real (Compose Navigation), el boton Paquito
 * del Navbar se usa como toggle hacia Chat; Cursos y Horarios son pestañas
 * reales. El detalle de curso usa flecha atras como el chat.
 */
private enum class RootScreen { Tabs, Chat, CourseDetail, Notifications, AcademicRisk, Events, EventDetail, Procedures, ProcedureDetail, Enrollment, Calendar }

private enum class OnboardingStep { Welcome, Notifications, Tour }

private const val DEMO_TEACHER_EMAIL = "yordan.sapacayo@tecsup.edu.pe"
private fun Double.formatTeacherValue(): String = String.format(java.util.Locale.US, "%.1f", this)

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
    private var notificationRequest by mutableIntStateOf(0)
    private var noticeKey by mutableStateOf<String?>(null)
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.action == AcademicNotificationPublisher.OPEN_INBOX) {
            noticeKey = intent.getStringExtra(AcademicNotificationPublisher.NOTICE_KEY)
            notificationRequest++
        }
    }

    override fun onResume() {
        super.onResume()
        AndroidAcademicNotificationScheduler(applicationContext).reconcile()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        if (android.os.Build.VERSION.SDK_INT >= 31) {
            // Retirar la ventana nativa al primer frame, sin otra animación de salida.
            splashScreen.setOnExitAnimationListener { it.remove() }
        }

        if (intent?.action == AcademicNotificationPublisher.OPEN_INBOX) {
            noticeKey = intent.getStringExtra(AcademicNotificationPublisher.NOTICE_KEY)
            notificationRequest++
        }
        setContent {
            var introVisible by rememberSaveable { mutableStateOf(intent?.action != AcademicNotificationPublisher.OPEN_INBOX) }
            LaunchedEffect(notificationRequest) {
                if (notificationRequest > 0) introVisible = false
            }
            PaquitoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PaquitoColors.Background,
                ) {
                    androidx.compose.animation.Crossfade(targetState = introVisible,
                        animationSpec = tween(240), label = "launch-intro") { showIntro ->
                        if (showIntro) pe.tecsup.paquitobot.ui.onboarding.LaunchIntroScreen(onFinished = { introVisible = false })
                        else AppRoot(notificationRequest, noticeKey)
                    }
                }
            }
        }
    }
}

/**
 * Iteracion 2026-08-13 (flujo de gates): antes de mostrar Home o Chat, la
 * app presenta el onboarding solo si falta completar la configuración inicial:
 *
 *   1. Bienvenida, notificaciones y recorrido.
 *   2. Sin sesion de Clerk -> [ClerkAuthGate].
 *   3. Con sesion -> pantallas de la app.
 */
@Composable
private fun AppRoot(notificationRequest: Int = 0, noticeKey: String? = null) {
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

    var publicEnrollmentOpen by rememberSaveable { mutableStateOf(false) }
    when {
        publicEnrollmentOpen -> EnrollmentGuideRoute(onBackClick = { publicEnrollmentOpen = false })
        !sessionUiState.isOnboardingComplete -> when (onboardingStep) {
            OnboardingStep.Welcome -> WelcomeScreen(
                onContinueClick = { onboardingStep = OnboardingStep.Notifications },
                onEnrollmentGuideClick = { publicEnrollmentOpen = true },
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

        !sessionUiState.isAuthenticated || sessionUiState.userId == null -> ClerkAuthGate(
            isUnauthorizedSession = sessionUiState.isUnauthorizedSession,
            isSigningOut = sessionUiState.isSigningOut,
            signOutError = sessionUiState.signOutError,
            onSignOutClick = sessionViewModel::signOut,
        )

        else -> {
            val academicOwner = requireNotNull(sessionUiState.userId)
            // shortcut: el correo habilita datos de demostración hasta que el backend entregue el rol docente.
            val isTeacherDemoAccount = sessionUiState.userEmail?.equals(DEMO_TEACHER_EMAIL, ignoreCase = true) == true
            // rememberSaveable: sobrevive a la rotacion (Android recrea la Activity)
            // - con remember simple, girar el telefono mandaba siempre a Inicio.
            var currentTab by rememberSaveable(academicOwner) { mutableStateOf(NavTab.Inicio) }
            var rootScreen by rememberSaveable(academicOwner) { mutableStateOf(RootScreen.Tabs) }
            var pendingCount by remember(academicOwner) { mutableIntStateOf(0) }
            var selectedCourse by rememberSaveable(academicOwner, stateSaver = SelectedCourseSaver) {
                mutableStateOf<CourseCardData?>(null)
            }
            val guideStateHolder = rememberSaveableStateHolder()
            var selectedProcedureId by rememberSaveable { mutableStateOf<String?>(null) }
            var selectedEventId by rememberSaveable { mutableStateOf<String?>(null) }
            val userFirstName = sessionUiState.userFirstName ?: "estudiante"
            val academicRepository = remember(academicOwner) { (context as PaquitoBotApp).academicRepositoryFor(academicOwner) }
            val inboxOwner = AcademicNotificationPublisher.ownerKey(academicOwner)
            val inboxModel: pe.tecsup.paquitobot.ui.notifications.NotificationsInboxViewModel = viewModel(
                key = "inbox-$inboxOwner", factory = remember(inboxOwner) {
                    factoryOf { pe.tecsup.paquitobot.ui.notifications.NotificationsInboxViewModel(context, inboxOwner) }
                })
            val inboxData by inboxModel.data.collectAsStateWithLifecycle()
            var riskFocusCourseId by rememberSaveable { mutableStateOf<Int?>(null) }
            var riskAttendance by rememberSaveable { mutableStateOf(false) }
            var detailAttendance by rememberSaveable { mutableStateOf(false) }
            var detailReturn by rememberSaveable { mutableStateOf(RootScreen.Tabs) }
            val routeScope = rememberCoroutineScope()
            val openCourse: (Int, Boolean) -> Unit = { id, attendance ->
                routeScope.launch {
                    academicRepository.loadAllCourseRecords().getOrNull()?.let { records ->
                        buildCoursesData(records, java.time.LocalDate.now().toString()).courses.find { it.id == id.toString() }?.let { course ->
                            selectedCourse = course
                            detailAttendance = attendance
                            detailReturn = rootScreen
                            rootScreen = RootScreen.CourseDetail
                        }
                    }
                }
            }
            val openNotice: (String) -> Unit = { key ->
                val parts = key.split(':')
                if (parts.firstOrNull() == "risk") {
                    riskFocusCourseId = parts.getOrNull(2)?.toIntOrNull()
                    riskAttendance = parts.getOrNull(1) == "attendance"
                    rootScreen = RootScreen.AcademicRisk
                } else {
                    val id = parts.getOrNull(1)?.toIntOrNull()
                    if (id != null) openCourse(id, parts.first() == "absence")
                    else rootScreen = RootScreen.Notifications
                }
            }
            LaunchedEffect(notificationRequest) {
                if (notificationRequest > 0) {
                    if (noticeKey != null) openNotice(noticeKey) else rootScreen = RootScreen.Notifications
                }
            }

            AnimatedContent(
                targetState = rootScreen,
                transitionSpec = {
                    (scaleIn(tween(240), initialScale = 0.94f) + fadeIn(tween(240))) togetherWith
                        (scaleOut(tween(200), targetScale = 1.03f) + fadeOut(tween(200)))
                },
                label = "root-screen-transition",
            ) { screen ->
            when (screen) {
                RootScreen.Tabs -> Box(Modifier.fillMaxSize()) {
                    AppTabScaffold(
                        currentTab = currentTab,
                        onTabSelected = { currentTab = it },
                        onPaquitoClick = { rootScreen = RootScreen.Chat },
                        notificationCount = pendingCount,
                    ) {
                    AnimatedContent(
                        targetState = currentTab,
                        transitionSpec = {
                            val movingForward = targetState.ordinal > initialState.ordinal
                            (slideInHorizontally(tween(260)) { fullWidth ->
                                if (movingForward) fullWidth else -fullWidth
                            } + fadeIn(tween(220))) togetherWith
                                (slideOutHorizontally(tween(220)) { fullWidth ->
                                    if (movingForward) -fullWidth else fullWidth
                                } + fadeOut(tween(180)))
                        },
                        label = "main-tab-transition",
                        modifier = Modifier.fillMaxWidth(),
                    ) { tab ->
                    when (tab) {
                    NavTab.Inicio -> {
                        if (isTeacherDemoAccount) {
                            TeacherDashboardScreen(
                                teacherName = sessionUiState.userFirstName
                                    ?: sessionUiState.userFullName?.takeUnless { it == "Estudiante" },
                                notificationCount = inboxData.items.size,
                                onNotificationsClick = { rootScreen = RootScreen.Notifications },
                                onEventsClick = { rootScreen = RootScreen.Events },
                                onSendNotification = { course: TeacherDemoCourse, student: TeacherDemoStudentCourse ->
                                    val messageDetails = buildList {
                                        if (student.gradeRisk) {
                                            add(
                                                if (student.courseComplete) {
                                                    "Has desaprobado ${course.name} con promedio ${student.average?.formatTeacherValue()}."
                                                } else {
                                                    "Estás en zona de riesgo en ${course.name}, con promedio actual ${student.average?.formatTeacherValue()}."
                                                },
                                            )
                                        }
                                        when (student.attendanceSignal) {
                                            TeacherDemoAttendanceSignal.Di -> add(
                                                "Superaste el 30 % de inasistencias en ${course.name} (${(student.absenceRate * 100).formatTeacherValue()} %).",
                                            )
                                            TeacherDemoAttendanceSignal.Warning -> add(
                                                "Tienes una advertencia de asistencia en ${course.name} (${(student.absenceRate * 100).formatTeacherValue()} % de inasistencias).",
                                            )
                                            TeacherDemoAttendanceSignal.None -> Unit
                                        }
                                    }
                                    val studentMessage =
                                        "Hola ${student.displayName}. ${messageDetails.joinToString(" Además, ")} " +
                                            "Acércate a conversar con tu docente para revisar tu situación."
                                    AcademicNotificationPublisher(context).publish(
                                        owner = AcademicNotificationPublisher.ownerKey(academicOwner),
                                        notice = AcademicNotice(
                                            key = "teacher-follow-up:${student.studentId}:${course.id}",
                                            title = "Mensaje enviado correctamente",
                                            body = "Para ${student.displayName}: $studentMessage",
                                        ),
                                        time = System.currentTimeMillis(),
                                        demoPresentation = true,
                                    )
                                },
                            )
                        } else {
                            val homeViewModel: AcademicHomeViewModel = viewModel(
                                key = "home-$academicOwner",
                                factory = remember(academicOwner) {
                                    factoryOf { AcademicHomeViewModel(academicRepository) }
                                },
                            )
                            val homeData by homeViewModel.uiState.collectAsStateWithLifecycle()
                            val homeError by homeViewModel.errorMessage.collectAsStateWithLifecycle()
                            LaunchedEffect(homeData.pendingCount) { pendingCount = homeData.pendingCount }
                            HomeTabContent(
                                data = homeData.copy(greeting = "¡Bienvenido, $userFirstName!", scenarioId = null, unreadNotificationsCount = inboxData.items.size),
                                onNotificationsClick = { rootScreen = RootScreen.Notifications },
                                onEventsClick = { rootScreen = RootScreen.Events },
                                onAcademicRiskClick = {
                                    riskFocusCourseId = null
                                    riskAttendance = false
                                    rootScreen = RootScreen.AcademicRisk
                                },
                                errorMessage = homeError,
                                onRetry = homeViewModel::load,
                            )
                        }
                    }
                    NavTab.Perfil -> pe.tecsup.paquitobot.ui.profile.ProfileTabContent(
                        name = sessionUiState.userFullName ?: "Estudiante",
                        email = sessionUiState.userEmail.orEmpty(),
                        isSigningOut = sessionUiState.isSigningOut,
                        signOutError = sessionUiState.signOutError,
                        onProceduresClick = { rootScreen = RootScreen.Procedures },
                        onEnrollmentClick = { rootScreen = RootScreen.Enrollment },
                        onSignOutClick = sessionViewModel::signOut,
                        onAcademicClick = { riskFocusCourseId = null; riskAttendance = false; rootScreen = RootScreen.AcademicRisk },
                        onCoursesClick = { currentTab = NavTab.Cursos },
                    )
                    NavTab.Cursos -> {
                        val coursesViewModel: AcademicViewModel = viewModel(
                            key = "courses-$academicOwner",
                            factory = remember(academicOwner) {
                                factoryOf { AcademicViewModel(academicRepository) }
                            },
                        )
                        val coursesData by coursesViewModel.uiState.collectAsStateWithLifecycle()
                        val coursesError by coursesViewModel.errorMessage.collectAsStateWithLifecycle()
                        LaunchedEffect(coursesData.pendingCount) { pendingCount = coursesData.pendingCount }
                        CoursesTabContent(
                            data = coursesData,
                            onCourseClick = { course ->
                                selectedCourse = course
                                detailAttendance = false
                                detailReturn = RootScreen.Tabs
                                rootScreen = RootScreen.CourseDetail
                            },
                            errorMessage = coursesError,
                            onRetry = coursesViewModel::load,
                        )
                    }
                    NavTab.Horarios -> {
                        val scheduleViewModel: AcademicScheduleViewModel = viewModel(
                            key = "schedule-$academicOwner",
                            factory = remember(academicOwner) {
                                factoryOf { AcademicScheduleViewModel(academicRepository) }
                            },
                        )
                        val scheduleData by scheduleViewModel.uiState.collectAsStateWithLifecycle()
                        val scheduleError by scheduleViewModel.errorMessage.collectAsStateWithLifecycle()
                        LaunchedEffect(scheduleData.pendingCount) { pendingCount = scheduleData.pendingCount }
                        ScheduleTabContent(
                            data = scheduleData,
                            errorMessage = scheduleError,
                            onRetry = scheduleViewModel::load,
                            onCalendarClick = { rootScreen = RootScreen.Calendar },
                        )
                    }
                    }
                }
                }
                }
                RootScreen.Chat -> {
                    val chatViewModel: ChatViewModel = viewModel(
                        key = "chat-$academicOwner-${sessionUiState.sessionRevision}-${if (isTeacherDemoAccount) "teacher" else "student"}",
                        factory = remember(academicOwner, isTeacherDemoAccount) {
                            viewModelFactory {
                                initializer {
                                    ChatViewModel(
                                        MiniMaxChatRepository(
                                            BuildConfig.MINIMAX_API_KEY,
                                            academicRepository,
                                            academicOwner,
                                            teacherContext = if (isTeacherDemoAccount) TeacherDemoData.chatContext else null,
                                        ),
                                    )
                                }
                            }
                        },
                    )
                    val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()
                    ChatScreen(
                        uiState = chatUiState,
                        onSendMessage = chatViewModel::sendMessage,
                        userName = userFirstName,
                        teacherMode = isTeacherDemoAccount,
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
                            key = "$academicOwner-${course.id}",
                            factory = remember(academicOwner, course.id) {
                                factoryOf {
                                    AcademicCourseDetailViewModel(academicRepository, course)
                                }
                            },
                        )
                        val detailData by detailViewModel.uiState.collectAsStateWithLifecycle()
                        val detailError by detailViewModel.errorMessage.collectAsStateWithLifecycle()
                        CourseDetailScreen(
                            data = detailData,
                            errorMessage = detailError,
                            onRetry = detailViewModel::load,
                            onBackClick = { rootScreen = detailReturn },
                            expandAttendance = detailAttendance,
                        )
                    }
                }
                RootScreen.Notifications -> {
                    var preferencesOpen by rememberSaveable { mutableStateOf(false) }
                    NotificationsInboxScreen(
                        data = inboxData,
                        onDeleteItem = inboxModel::delete,
                        onRestoreItem = inboxModel::restore,
                        onDeleteAll = inboxModel::deleteAll,
                        onBackClick = { rootScreen = RootScreen.Tabs },
                        onPreferencesClick = { preferencesOpen = !preferencesOpen },
                        preferencesContent = {
                            if (preferencesOpen) NotificationPermissionSection(compactDemo = true, onPermissionChanged = notificationScheduler::reconcile)
                        },
                        onRiskAction = openNotice,
                    )
                }
                RootScreen.AcademicRisk -> {
                    val riskViewModel: AcademicRiskViewModel = viewModel(
                        key = "academic-risk-$academicOwner",
                        factory = remember(academicOwner) {
                            factoryOf { AcademicRiskViewModel(academicRepository) }
                        },
                    )
                    AcademicRiskScreen(
                        viewModel = riskViewModel,
                        onBackClick = {
                            rootScreen = RootScreen.Tabs
                        },
                        onContactTutor = { alert ->
                            val tutor = DemoTutorDirectory.forCourse(alert.courseId)
                            val subject = Uri.encode("PaquitoBot · ${alert.courseCode}: necesito orientación")
                            val body = Uri.encode(
                                "Hola, ${tutor.name}. Soy estudiante de ${alert.courseName}. " +
                                    "PaquitoBot me mostró este aviso: ${alert.title}. ¿Podemos revisarlo?",
                            )
                            val contactIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:${tutor.email}?subject=$subject&body=$body")
                            }
                            runCatching { context.startActivity(Intent.createChooser(contactIntent, "Contactar a mi tutora")) }
                        },
                        focusCourseId = riskFocusCourseId,
                        initialKind = if (riskAttendance) pe.tecsup.paquitobot.domain.academic.AcademicRiskKind.Attendance else pe.tecsup.paquitobot.domain.academic.AcademicRiskKind.Grade,
                        onOpenCourse = { alert -> openCourse(alert.courseId, alert.kind == pe.tecsup.paquitobot.domain.academic.AcademicRiskKind.Attendance) },
                    )
                }
                RootScreen.Calendar -> CalendarScreen(onBackClick = { rootScreen = RootScreen.Tabs })
                RootScreen.Enrollment -> EnrollmentGuideRoute(onBackClick = {
                    currentTab = NavTab.Inicio
                    rootScreen = RootScreen.Tabs
                })
                RootScreen.Procedures, RootScreen.ProcedureDetail -> {
                    val repository = (context as PaquitoBotApp).procedureRepository
                    val guideViewModel: ProcedureViewModel = viewModel(factory = remember {
                        factoryOf { ProcedureViewModel(repository) }
                    })
                    val guideState by guideViewModel.uiState.collectAsStateWithLifecycle()
                    val ready = guideState as? ProcedureUiState.Ready
                    val procedure = ready?.catalog?.find(selectedProcedureId)
                    if (screen == RootScreen.ProcedureDetail && procedure != null) {
                        ProcedureDetailScreen(procedure, ready.catalog.source,
                            onBackClick = { rootScreen = RootScreen.Procedures })
                    } else {
                        guideStateHolder.SaveableStateProvider("procedure-catalog") {
                            ProcedureGuideScreen(guideState, onRetry = guideViewModel::load,
                                onProcedureClick = { selectedProcedureId = it; rootScreen = RootScreen.ProcedureDetail },
                                onBackClick = { rootScreen = RootScreen.Tabs })
                        }
                    }
                }
                RootScreen.Events -> EventsScreen(
                    onBackClick = { rootScreen = RootScreen.Tabs },
                    onEventClick = { event ->
                        selectedEventId = event.id
                        rootScreen = RootScreen.EventDetail
                    },
                )
                RootScreen.EventDetail -> {
                    val event = EventMockData.find(selectedEventId)
                    if (event == null) {
                        LaunchedEffect(Unit) { rootScreen = RootScreen.Events }
                    } else {
                        EventDetailScreen(
                            event = event,
                            onBackClick = { rootScreen = RootScreen.Events },
                        )
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
