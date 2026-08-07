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
import pe.tecsup.paquitobot.ui.chat.ChatScreen
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.home.HomeScreen
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Mientras no haya navegacion real (Compose Navigation), el boton Paquito
 * del Navbar se usa como toggle entre Home (vista principal) y Chat (vista
 * conversacional, con su propia flecha de "volver" para regresar a Home).
 */
private enum class RootScreen { Home, Chat }

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

@Composable
private fun AppRoot() {
    var currentTab by remember { mutableStateOf(NavTab.Inicio) }
    var rootScreen by remember { mutableStateOf(RootScreen.Home) }

    when (rootScreen) {
        RootScreen.Home -> HomeScreen(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            onPaquitoClick = { rootScreen = RootScreen.Chat },
        )
        RootScreen.Chat -> ChatScreen(
            onBackClick = { rootScreen = RootScreen.Home },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {
    PaquitoTheme {
        AppRoot()
    }
}