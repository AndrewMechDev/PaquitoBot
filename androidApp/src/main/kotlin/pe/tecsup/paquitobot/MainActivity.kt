package pe.tecsup.paquitobot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.components.Day
import pe.tecsup.paquitobot.ui.components.DayState
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.components.Navbar
import pe.tecsup.paquitobot.ui.components.TaskBadge
import pe.tecsup.paquitobot.ui.components.TaskList
import pe.tecsup.paquitobot.ui.components.TaskListHeader
import pe.tecsup.paquitobot.ui.components.TaskListItem
import pe.tecsup.paquitobot.ui.onboarding.WelcomeScreen
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

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
 * Root temporal de la app. Reune todos los componentes extraídos de Figma en
 * una vista de validación visual, mientras no hay navegación real ni backend.
 *
 * TODO (PASO 4): reemplazar por navegación real con Home B + Chat + Cursos.
 */
@Composable
private fun AppRoot() {
    var currentTab by remember { mutableStateOf(NavTab.Inicio) }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PaquitoColors.Background),
            verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.lg),
        ) {
            // Welcome screen (texto + botón "Continuar").
            WelcomeScreen(
                onContinueClick = { /* TODO: navegar a Onboarding.Notificaciones */ },
                modifier = Modifier.weight(1f),
            )

            // Demo del semáforo de días (Home B).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaquitoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.sm),
            ) {
                Day(
                    dayOfWeek = "L", dayNumber = "3", state = DayState.WithTask,
                    selected = true, onClick = {},
                )
            }

            // Demo de TaskList (Home B).
            TaskList(
                header = TaskListHeader(title = "Paquito te avisó", actionLabel = "Ver todo"),
                items = listOf(
                    TaskListItem("NT", TaskBadge.Info,    "Foro de novedades",       "Curso de Narrativa",                  "hace 1 h"),
                    TaskListItem("FO", TaskBadge.Warning, "Foro de Ética por vencer", "Martes 20:00 · falta tu respuesta",   "2 d"),
                    TaskListItem("EX", TaskBadge.Neutral, "Parcial de Álgebra Lineal","Jueves 08:00 · aula B-204",          "4 d"),
                ),
                modifier = Modifier.padding(horizontal = PaquitoSpacing.lg),
            )
        }

        // Navbar anclado abajo con badge "3" (3 tareas pendientes).
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        ) {
            Navbar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onPaquitoClick = { /* TODO: navegar a Chat */ },
                notificationCount = 3,
            )
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