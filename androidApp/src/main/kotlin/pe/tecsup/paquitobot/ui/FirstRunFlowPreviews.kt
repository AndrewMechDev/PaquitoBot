package pe.tecsup.paquitobot.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import pe.tecsup.paquitobot.ui.auth.AuthGateScreen
import pe.tecsup.paquitobot.ui.canvas.CanvasConnectScreen
import pe.tecsup.paquitobot.ui.canvas.CanvasConnectUiState
import pe.tecsup.paquitobot.ui.chat.ChatScreen
import pe.tecsup.paquitobot.ui.chat.ChatUiState
import pe.tecsup.paquitobot.ui.courses.CourseDetailScreen
import pe.tecsup.paquitobot.ui.courses.CoursesScreen
import pe.tecsup.paquitobot.ui.home.HomeScreen
import pe.tecsup.paquitobot.ui.home.HomeScreenData
import pe.tecsup.paquitobot.ui.onboarding.OnboardingNotificationsScreen
import pe.tecsup.paquitobot.ui.onboarding.OnboardingTourScreen
import pe.tecsup.paquitobot.ui.onboarding.WelcomeScreen
import pe.tecsup.paquitobot.ui.schedule.ScheduleScreen
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Previews del primer uso (sin reinstalar). En Android Studio: Split /
 * Design sobre este archivo y recorre los previews 1–8.
 *
 * Flujo real en dispositivo: Welcome → Notificaciones → Tour → Google → Canvas → pestañas.
 * Para repetirlo: Ajustes del sistema → Apps → PaquitoBot → Borrar datos.
 */
@Preview(showBackground = true, name = "1 Welcome")
@Composable
private fun Preview01Welcome() {
    PaquitoTheme { WelcomeScreen(onContinueClick = {}) }
}

@Preview(showBackground = true, name = "2 Notificaciones")
@Composable
private fun Preview02Notifications() {
    PaquitoTheme { OnboardingNotificationsScreen() }
}

@Preview(showBackground = true, name = "2b Tour dolores")
@Composable
private fun Preview02bTour() {
    PaquitoTheme { OnboardingTourScreen(onContinue = {}) }
}

@Preview(showBackground = true, name = "3 Google")
@Composable
private fun Preview03Google() {
    PaquitoTheme {
        AuthGateScreen(isSigningIn = false, errorMessage = null, onSignInClick = {})
    }
}

@Preview(showBackground = true, name = "4 Canvas")
@Composable
private fun Preview04Canvas() {
    PaquitoTheme {
        CanvasConnectScreen(
            uiState = CanvasConnectUiState(),
            onConnectClick = {},
            onContinueClick = {},
        )
    }
}

@Preview(showBackground = true, name = "5 Home con nombre")
@Composable
private fun Preview05Home() {
    PaquitoTheme {
        HomeScreen(
            data = HomeScreenData.default().copy(greeting = "¡Bienvenido, Andrea!"),
        )
    }
}

@Preview(showBackground = true, name = "6 Chat con nombre")
@Composable
private fun Preview06Chat() {
    PaquitoTheme {
        ChatScreen(uiState = ChatUiState(), onSendMessage = {}, userName = "Andrea")
    }
}

@Preview(showBackground = true, name = "7 Cursos")
@Composable
private fun Preview07Courses() {
    PaquitoTheme { CoursesScreen() }
}

@Preview(showBackground = true, name = "7b Detalle curso")
@Composable
private fun Preview07bCourseDetail() {
    PaquitoTheme { CourseDetailScreen(onBackClick = {}) }
}

@Preview(showBackground = true, name = "8 Horarios")
@Composable
private fun Preview08Schedule() {
    PaquitoTheme { ScheduleScreen() }
}
